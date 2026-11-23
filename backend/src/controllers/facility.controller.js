const pool = require('../config/db');
const { withTransaction } = require('../config/db');
const { ok, AppError, badRequest } = require('../utils/http');
const { readBody, parseId, requiredText, optionalText, oneOf, positiveInt, readPaging, queryText } = require('../utils/validate');
const { toFacility, toAdminBooking } = require('../utils/mappers');
const { toSql } = require('../utils/time');

const ACCESS_POLICIES = ['ALL', 'TEACHER_ONLY'];
const OPERATIONAL_STATUSES = ['ACTIVE', 'UNAVAILABLE'];
const UNAVAILABLE_REASONS = ['CLEANING', 'MAINTENANCE', 'OTHER'];
const FACILITY_FIELDS = [
  'name', 'type', 'location', 'capacity', 'description',
  'accessPolicy', 'operationalStatus', 'unavailableReason', 'unavailableNote',
];

const facilityNotFound = () => new AppError(404, 'FACILITY_NOT_FOUND', 'Không tìm thấy phòng');

// Allowed ?sort= values (never put user text directly into ORDER BY). facility_id keeps the order stable.
const SORTS = {
  name: 'name ASC, facility_id ASC', // default, as in REST_API.md
  type: 'type ASC, name ASC, facility_id ASC',
  capacity_asc: 'capacity ASC, name ASC, facility_id ASC',
  capacity_desc: 'capacity DESC, name ASC, facility_id ASC',
};

// F01 GET /api/facilities?type=STUDY_ROOM&operationalStatus=ACTIVE&sort=type&page=1&limit=20
// Returns ACTIVE and UNAVAILABLE by default so the app can show the status.
async function listFacilities(req, res) {
  const { page, limit, offset } = readPaging(req.query);
  const type = queryText(req.query, 'type');
  const status = queryText(req.query, 'operationalStatus');
  const sort = queryText(req.query, 'sort') ?? 'name';
  oneOf(sort, 'sort', Object.keys(SORTS));

  const where = [];
  const params = [];
  if (type !== undefined) {
    where.push('type = ?');
    params.push(requiredText(type, 'type', 50));
  }
  if (status !== undefined) {
    where.push('operational_status = ?');
    params.push(oneOf(status, 'operationalStatus', OPERATIONAL_STATUSES));
  }
  const whereSql = where.length ? `WHERE ${where.join(' AND ')}` : '';

  const [[{ total }]] = await pool.query(`SELECT COUNT(*) AS total FROM facilities ${whereSql}`, params);
  const [rows] = await pool.query(
    `SELECT * FROM facilities ${whereSql} ORDER BY ${SORTS[sort]} LIMIT ? OFFSET ?`,
    [...params, limit, offset]
  );
  return ok(res, { items: rows.map(toFacility), page, limit, total: Number(total) });
}

// F02 GET /api/facilities/:facilityId  (UNAVAILABLE rooms are still returned, with the reason)
async function getFacility(req, res) {
  const facilityId = parseId(req.params.facilityId, 'facilityId');
  const [rows] = await pool.query('SELECT * FROM facilities WHERE facility_id = ?', [facilityId]);
  if (rows.length === 0) throw facilityNotFound();
  return ok(res, toFacility(rows[0]));
}

// ---------- Admin ----------

// Validates the fields that are PRESENT in body and returns them as { column: value }.
function readFacilityFields(body) {
  const has = (k) => Object.prototype.hasOwnProperty.call(body, k);
  const out = {};
  if (has('name')) out.name = requiredText(body.name, 'name', 150);
  if (has('type')) out.type = requiredText(body.type, 'type', 50);
  if (has('location')) out.location = requiredText(body.location, 'location', 255);
  if (has('capacity')) out.capacity = positiveInt(body.capacity, 'capacity');
  if (has('description')) out.description = optionalText(body.description, 'description', { maxBytes: 65535 });
  if (has('accessPolicy')) out.access_policy = oneOf(body.accessPolicy, 'accessPolicy', ACCESS_POLICIES);
  if (has('operationalStatus')) out.operational_status = oneOf(body.operationalStatus, 'operationalStatus', OPERATIONAL_STATUSES);
  if (has('unavailableReason')) {
    out.unavailable_reason = body.unavailableReason === null ? null : oneOf(body.unavailableReason, 'unavailableReason', UNAVAILABLE_REASONS);
  }
  if (has('unavailableNote')) out.unavailable_note = optionalText(body.unavailableNote, 'unavailableNote', { maxChars: 255 });
  return out;
}

// Checks the FINAL facility state (after merging a PATCH with the current row):
//   ACTIVE      -> unavailableReason and unavailableNote must both be null
//   UNAVAILABLE -> unavailableReason required; OTHER also needs a non-empty note
function checkOperationalState(f) {
  if (f.operational_status === 'ACTIVE') {
    if (f.unavailable_reason !== null || f.unavailable_note !== null) {
      throw badRequest('Phòng ACTIVE phải có unavailableReason và unavailableNote là null');
    }
  } else {
    if (!f.unavailable_reason) throw badRequest('Phòng UNAVAILABLE phải có unavailableReason');
    if (f.unavailable_reason === 'OTHER' && !f.unavailable_note) throw badRequest('unavailableReason OTHER cần unavailableNote không rỗng');
  }
}

// AD03 POST /api/admin/facilities
async function createFacility(req, res) {
  const body = readBody(req.body, FACILITY_FIELDS);
  for (const k of ['name', 'type', 'location', 'capacity', 'accessPolicy']) {
    if (body[k] === undefined) throw badRequest(`${k} là bắt buộc`);
  }
  const input = {
    description: null,
    operational_status: 'ACTIVE',
    unavailable_reason: null,
    unavailable_note: null,
    ...readFacilityFields(body),
  };
  checkOperationalState(input);

  const [result] = await pool.query('INSERT INTO facilities SET ?', [input]);
  const [rows] = await pool.query('SELECT * FROM facilities WHERE facility_id = ?', [String(result.insertId)]);
  return ok(res, toFacility(rows[0]), 'Đã thêm phòng', 201);
}

// AD04 PATCH /api/admin/facilities/:facilityId
// Edit fields and/or stop/reopen the room. No DELETE: stopping a room = operationalStatus UNAVAILABLE.
// Existing bookings are never changed. When the room goes ACTIVE -> UNAVAILABLE, the response lists
// the PENDING/APPROVED bookings that have not ended yet (affectedBookings) so the admin can review them.
async function updateFacility(req, res) {
  const facilityId = parseId(req.params.facilityId, 'facilityId');
  const body = readBody(req.body, FACILITY_FIELDS);
  const changes = readFacilityFields(body);
  if (Object.keys(changes).length === 0) throw badRequest('Cần ít nhất một trường để cập nhật');

  const data = await applyFacilityChanges(facilityId, () => changes);
  return ok(res, data, 'Đã cập nhật phòng');
}

// DELETE /api/admin/facilities/:facilityId  (from the team's API catalogue: "ẩn hoặc ngừng sử dụng")
// Never deletes the row: it is a shortcut for AD04 with
//   { operationalStatus: "UNAVAILABLE", unavailableReason: "OTHER", unavailableNote: "Ngừng sử dụng" }
// so booking history is kept. Already UNAVAILABLE -> returned unchanged (200).
// Reopen with AD04: { "operationalStatus": "ACTIVE", "unavailableReason": null, "unavailableNote": null }
async function deactivateFacility(req, res) {
  const facilityId = parseId(req.params.facilityId, 'facilityId');
  const data = await applyFacilityChanges(facilityId, (current) => (current.operational_status === 'ACTIVE'
    ? { operational_status: 'UNAVAILABLE', unavailable_reason: 'OTHER', unavailable_note: 'Ngừng sử dụng' }
    : {}));
  return ok(res, data, 'Đã ngừng sử dụng phòng');
}

// Shared by AD04 and DELETE. getChanges(currentRow) returns { column: value } to apply.
// Returns { facility, affectedBookings }.
async function applyFacilityChanges(facilityId, getChanges) {
  return withTransaction(async (conn) => {
    // Same lock as booking creation, so "stop room" and "create booking" never interleave
    const [rows] = await conn.query('SELECT * FROM facilities WHERE facility_id = ? FOR UPDATE', [facilityId]);
    const current = rows[0];
    if (!current) throw facilityNotFound();

    const changes = getChanges(current);
    checkOperationalState({ ...current, ...changes });
    if (Object.keys(changes).length > 0) {
      await conn.query('UPDATE facilities SET ? WHERE facility_id = ?', [changes, facilityId]);
    }

    let affectedBookings = [];
    if (current.operational_status === 'ACTIVE' && changes.operational_status === 'UNAVAILABLE') {
      const [booked] = await conn.query(
        `SELECT b.*, u.full_name AS user_full_name, f.name AS facility_name
         FROM bookings b
         JOIN users u ON u.user_id = b.user_id
         JOIN facilities f ON f.facility_id = b.facility_id
         WHERE b.facility_id = ? AND b.status IN ('PENDING', 'APPROVED') AND b.end_time > ?
         ORDER BY b.start_time ASC, b.booking_id ASC`,
        [facilityId, toSql(Date.now())]
      );
      affectedBookings = booked.map(toAdminBooking);
    }

    const [updated] = await conn.query('SELECT * FROM facilities WHERE facility_id = ?', [facilityId]);
    return { facility: toFacility(updated[0]), affectedBookings };
  });
}

module.exports = { listFacilities, getFacility, createFacility, updateFacility, deactivateFacility };
