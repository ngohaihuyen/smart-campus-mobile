const pool = require('../config/db');
const { withTransaction } = require('../config/db');
const { ok, AppError, badRequest } = require('../utils/http');
const { readBody, parseId, requiredText, oneOf, readPaging, queryText } = require('../utils/validate');
const { toMyBooking, toAdminBooking, canBook } = require('../utils/mappers');
const {
  CAMPUS_TIME_ZONE, DAY_MS, parseInstant, parseCampusDate, toSql, toApi, sqlToMs, sqlToApi,
  campusParts, parseClock, formatClock, formatCampus,
} = require('../utils/time');

// "09:00–10:00 15/01/2030" in campus time, for messages shown to users
const rangeText = (startSql, endSql) => `${formatCampus(sqlToMs(startSql)).slice(0, 5)}–${formatCampus(sqlToMs(endSql))}`;

// Business rules (docs/REST_API.md mục 5). Campus clock times, not UTC.
const OPENING_SECONDS = parseClock(process.env.OPENING_TIME || '07:00');
const CLOSING_SECONDS = parseClock(process.env.CLOSING_TIME || '21:00');
const MAX_DURATION_MINUTES = Number(process.env.MAX_BOOKING_MINUTES || 120);

const STATUSES = ['PENDING', 'APPROVED', 'REJECTED', 'CANCELLED'];

const invalidTime = (message) => new AppError(400, 'INVALID_BOOKING_TIME', message);
const facilityNotFound = () => new AppError(404, 'FACILITY_NOT_FOUND', 'Không tìm thấy phòng');
const bookingNotFound = () => new AppError(404, 'BOOKING_NOT_FOUND', 'Không tìm thấy lịch đặt');
const facilityUnavailable = () => new AppError(409, 'FACILITY_UNAVAILABLE', 'Phòng đang ngừng hoạt động');

// Booking + names, used by every endpoint that returns bookings
const BOOKING_SELECT = `
  SELECT b.*, f.name AS facility_name, u.full_name AS user_full_name
  FROM bookings b
  JOIN facilities f ON f.facility_id = b.facility_id
  JOIN users u ON u.user_id = b.user_id`;

async function loadBooking(conn, bookingId) {
  const [rows] = await conn.query(`${BOOKING_SELECT} WHERE b.booking_id = ?`, [bookingId]);
  return rows[0];
}

// Lock order for every write: facility row first, then booking row (docs mục 6).
// Returns { facility, booking } both locked FOR UPDATE until the transaction ends.
async function lockFacilityAndBooking(conn, bookingId, facilityId) {
  const [facilities] = await conn.query('SELECT * FROM facilities WHERE facility_id = ? FOR UPDATE', [facilityId]);
  const [bookings] = await conn.query('SELECT * FROM bookings WHERE booking_id = ? FOR UPDATE', [bookingId]);
  return { facility: facilities[0], booking: bookings[0] };
}

// B01 GET /api/bookings/availability?facilityId=1&date=2030-01-15
// date = day on the campus calendar (Asia/Ho_Chi_Minh). No fixed slots: the app gets
// occupiedIntervals (PENDING/APPROVED, no owner info) and freeIntervals (gaps inside opening hours).
async function getAvailability(req, res) {
  const facilityId = parseId(queryText(req.query, 'facilityId'), 'facilityId');
  const date = queryText(req.query, 'date');
  const dayStart = parseCampusDate(date);
  if (dayStart === null) throw badRequest('date phải là ngày có thật dạng YYYY-MM-DD');

  const [facilities] = await pool.query('SELECT * FROM facilities WHERE facility_id = ?', [facilityId]);
  const facility = facilities[0];
  if (!facility) throw facilityNotFound();

  // Campus day 2030-01-15 = UTC [2030-01-14 17:00, 2030-01-15 17:00)
  const [rows] = await pool.query(
    `SELECT start_time, end_time, status FROM bookings
     WHERE facility_id = ? AND status IN ('PENDING', 'APPROVED') AND start_time < ? AND end_time > ?
     ORDER BY start_time ASC, end_time ASC`,
    [facilityId, toSql(dayStart + DAY_MS), toSql(dayStart)]
  );
  const occupied = rows.map((r) => ({ start: sqlToMs(r.start_time), end: sqlToMs(r.end_time) }));

  // Free = opening hours minus occupied, starting no earlier than "now" (past time is not bookable)
  const freeIntervals = [];
  if (facility.operational_status === 'ACTIVE') {
    const closeMs = dayStart + CLOSING_SECONDS * 1000;
    let cursor = Math.max(dayStart + OPENING_SECONDS * 1000, Math.ceil(Date.now() / 1000) * 1000);
    for (const o of occupied) {
      if (o.start > cursor) freeIntervals.push({ startTime: toApi(cursor), endTime: toApi(Math.min(o.start, closeMs)) });
      cursor = Math.max(cursor, o.end);
      if (cursor >= closeMs) break;
    }
    if (cursor < closeMs) freeIntervals.push({ startTime: toApi(cursor), endTime: toApi(closeMs) });
  }

  return ok(res, {
    facilityId,
    date,
    timeZone: CAMPUS_TIME_ZONE,
    operationalStatus: facility.operational_status,
    unavailableReason: facility.unavailable_reason,
    unavailableNote: facility.unavailable_note,
    canBook: canBook(req.user.role, facility),
    openingTime: formatClock(OPENING_SECONDS),
    closingTime: formatClock(CLOSING_SECONDS),
    maxDurationMinutes: MAX_DURATION_MINUTES,
    // status = workflow's "Đã đặt" (APPROVED) / "Đợi duyệt" (PENDING); freeIntervals = "Chưa đặt"
    occupiedIntervals: rows.map((r) => ({ startTime: sqlToApi(r.start_time), endTime: sqlToApi(r.end_time), status: r.status })),
    freeIntervals: freeIntervals.filter((f) => f.startTime < f.endTime),
  });
}

// Time rules for a new booking. Throws INVALID_BOOKING_TIME.
function checkBookingTime(start, end, now) {
  if (start >= end) throw invalidTime('Giờ kết thúc phải sau giờ bắt đầu');
  if (start <= now) throw invalidTime('Giờ bắt đầu phải ở tương lai');
  const s = campusParts(start);
  const e = campusParts(end);
  if (s.date !== e.date) throw invalidTime('Lịch đặt phải nằm trong cùng một ngày (giờ Việt Nam)');
  if (s.secondsOfDay < OPENING_SECONDS || e.secondsOfDay > CLOSING_SECONDS) {
    throw invalidTime(`Chỉ được đặt trong giờ hoạt động ${formatClock(OPENING_SECONDS).slice(0, 5)}–${formatClock(CLOSING_SECONDS).slice(0, 5)}`);
  }
  if (end - start > MAX_DURATION_MINUTES * 60000) throw invalidTime(`Mỗi lần đặt tối đa ${MAX_DURATION_MINUTES} phút`);
}

// B02 POST /api/bookings  { facilityId, startTime, endTime, reason }   (STUDENT, TEACHER)
async function createBooking(req, res) {
  const body = readBody(req.body, ['facilityId', 'startTime', 'endTime', 'reason']);
  const facilityId = parseId(body.facilityId, 'facilityId');
  const start = parseInstant(body.startTime);
  const end = parseInstant(body.endTime);
  if (start === null || end === null) {
    throw badRequest('startTime và endTime phải theo RFC 3339, có giây và múi giờ, ví dụ "2030-01-15T09:00:00+07:00"');
  }
  if (typeof body.reason !== 'string' || !body.reason.trim()) throw badRequest('Lý do / mục đích là bắt buộc');
  const reason = requiredText(body.reason, 'reason', 500);
  checkBookingTime(start, end, Date.now());

  const bookingId = await withTransaction(async (conn) => {
    // 1. Lock the facility row: all bookings of this room are now serialized
    const [facilities] = await conn.query('SELECT * FROM facilities WHERE facility_id = ? FOR UPDATE', [facilityId]);
    const facility = facilities[0];
    if (!facility) throw facilityNotFound();
    if (req.user.role === 'STUDENT' && facility.access_policy !== 'ALL') {
      throw new AppError(403, 'FACILITY_ACCESS_DENIED', 'Phòng này chỉ dành cho giảng viên');
    }
    if (facility.operational_status !== 'ACTIVE') throw facilityUnavailable();
    checkBookingTime(start, end, Date.now()); // time may have passed while waiting for the lock

    // 2. Overlap: existing.start < new.end AND existing.end > new.start (touching edges are fine)
    const [conflicts] = await conn.query(
      `SELECT start_time, end_time, status FROM bookings
       WHERE facility_id = ? AND status IN ('PENDING', 'APPROVED') AND start_time < ? AND end_time > ?
       ORDER BY start_time FOR UPDATE`,
      [facilityId, toSql(end), toSql(start)]
    );
    if (conflicts.length > 0) {
      // Workflow "Hiển thị thời gian bị trùng": message + error.conflicts (no owner info)
      const ranges = conflicts.map((c) => rangeText(c.start_time, c.end_time)).join(', ');
      throw new AppError(409, 'BOOKING_CONFLICT', `Trùng thời gian với lịch đã có: ${ranges}. Vui lòng chọn thời gian khác`, {
        conflicts: conflicts.map((c) => ({ startTime: sqlToApi(c.start_time), endTime: sqlToApi(c.end_time), status: c.status })),
      });
    }

    const [result] = await conn.query(
      "INSERT INTO bookings (user_id, facility_id, start_time, end_time, reason, status) VALUES (?, ?, ?, ?, ?, 'PENDING')",
      [req.user.userId, facilityId, toSql(start), toSql(end), reason]
    );
    return String(result.insertId);
  });

  return ok(res, toMyBooking(await loadBooking(pool, bookingId)), 'Gửi yêu cầu đặt phòng thành công, đang chờ Admin xử lý', 201);
}

// Shared by B03 and AD01. Optional filters:
//   ?status=PENDING      one of the 4 statuses
//   ?date=2030-01-15     bookings that START on this campus day (Asia/Ho_Chi_Minh)
//   ?facilityId=1, ?userId=2   only when allowFacilityFilter (admin list)
async function listBookings(req, { userId, order, allowFacilityFilter = false }) {
  const { page, limit, offset } = readPaging(req.query);
  const status = queryText(req.query, 'status');
  const date = queryText(req.query, 'date');
  const facilityId = allowFacilityFilter ? queryText(req.query, 'facilityId') : undefined;
  const filterUserId = allowFacilityFilter ? queryText(req.query, 'userId') : undefined;
  const where = [];
  const params = [];
  if (userId) { where.push('b.user_id = ?'); params.push(userId); }
  if (status !== undefined) { where.push('b.status = ?'); params.push(oneOf(status, 'status', STATUSES)); }
  if (date !== undefined) {
    const dayStart = parseCampusDate(date);
    if (dayStart === null) throw badRequest('date phải là ngày có thật dạng YYYY-MM-DD');
    where.push('b.start_time >= ? AND b.start_time < ?');
    params.push(toSql(dayStart), toSql(dayStart + DAY_MS));
  }
  if (facilityId !== undefined) { where.push('b.facility_id = ?'); params.push(parseId(facilityId, 'facilityId')); }
  if (filterUserId !== undefined) { where.push('b.user_id = ?'); params.push(parseId(filterUserId, 'userId')); }
  const whereSql = where.length ? `WHERE ${where.join(' AND ')}` : '';

  const [[{ total }]] = await pool.query(`SELECT COUNT(*) AS total FROM bookings b ${whereSql}`, params);
  const [rows] = await pool.query(
    `${BOOKING_SELECT} ${whereSql} ORDER BY b.created_at ${order}, b.booking_id ${order} LIMIT ? OFFSET ?`,
    [...params, limit, offset]
  );
  return { rows, page, limit, total: Number(total) };
}

// B03 GET /api/bookings/my?status=PENDING&date=2030-01-15&page=1&limit=20   (newest first)
async function getMyBookings(req, res) {
  const { rows, ...paging } = await listBookings(req, { userId: req.user.userId, order: 'DESC' });
  return ok(res, { items: rows.map(toMyBooking), ...paging });
}

// B04 DELETE /api/bookings/:bookingId  -> status CANCELLED (row is never deleted)
// PENDING: always cancellable. APPROVED: only before start. REJECTED: never.
// Cancelling an already CANCELLED booking of your own returns it unchanged (200).
async function cancelBooking(req, res) {
  const bookingId = parseId(req.params.bookingId, 'bookingId');
  const [found] = await pool.query('SELECT user_id, facility_id FROM bookings WHERE booking_id = ?', [bookingId]);
  if (found.length === 0) throw bookingNotFound();
  if (String(found[0].user_id) !== req.user.userId) {
    throw new AppError(403, 'NOT_BOOKING_OWNER', 'Bạn chỉ được hủy lịch đặt của mình');
  }

  const message = await withTransaction(async (conn) => {
    const { booking } = await lockFacilityAndBooking(conn, bookingId, found[0].facility_id);
    if (booking.status === 'CANCELLED') return 'Lịch đặt đã được hủy trước đó';
    if (booking.status === 'REJECTED') throw new AppError(409, 'BOOKING_NOT_CANCELLABLE', 'Không thể hủy lịch đã bị từ chối');
    const now = Date.now();
    if (booking.status === 'APPROVED' && sqlToMs(booking.start_time) <= now) {
      throw new AppError(409, 'BOOKING_NOT_CANCELLABLE', 'Lịch đã duyệt chỉ hủy được trước giờ bắt đầu');
    }
    // reviewed_by / reviewed_at are kept when cancelling an APPROVED booking
    await conn.query("UPDATE bookings SET status = 'CANCELLED', cancelled_at = ? WHERE booking_id = ?", [toSql(now), bookingId]);
    return 'Đã hủy lịch đặt';
  });

  return ok(res, toMyBooking(await loadBooking(pool, bookingId)), message);
}

// GET /api/notifications?since=2030-01-14T00:00:00Z&page=1&limit=20   (STUDENT, TEACHER)
// Workflow: "thông báo cho người dùng là yêu cầu đã được phê duyệt / bị từ chối".
// No push service in scope (no Firebase): the app polls this (on open, on refresh, or with WorkManager),
// shows a local notification per item, and next time sends the newest createdAt as ?since=.
// Built from bookings.reviewed_at - no extra table.
async function getNotifications(req, res) {
  const { page, limit, offset } = readPaging(req.query);
  const sinceText = queryText(req.query, 'since');
  const where = ['b.user_id = ?', 'b.reviewed_at IS NOT NULL'];
  const params = [req.user.userId];
  if (sinceText !== undefined) {
    const since = parseInstant(sinceText);
    if (since === null) throw badRequest('since phải theo RFC 3339 có múi giờ, ví dụ "2030-01-14T00:00:00Z"');
    where.push('b.reviewed_at > ?');
    params.push(toSql(since));
  }
  const whereSql = `WHERE ${where.join(' AND ')}`;

  const [[{ total }]] = await pool.query(`SELECT COUNT(*) AS total FROM bookings b ${whereSql}`, params);
  const [rows] = await pool.query(
    `${BOOKING_SELECT} ${whereSql} ORDER BY b.reviewed_at DESC, b.booking_id DESC LIMIT ? OFFSET ?`,
    [...params, limit, offset]
  );
  const items = rows.map((r) => {
    // A booking approved and later cancelled by its owner still had an "approved" notification
    const approved = r.rejection_reason === null;
    const when = rangeText(r.start_time, r.end_time);
    return {
      type: approved ? 'BOOKING_APPROVED' : 'BOOKING_REJECTED',
      message: approved
        ? `Yêu cầu đặt ${r.facility_name} lúc ${when} đã được phê duyệt`
        : `Yêu cầu đặt ${r.facility_name} lúc ${when} đã bị từ chối. Lý do: ${r.rejection_reason}`,
      createdAt: sqlToApi(r.reviewed_at),
      booking: toMyBooking(r),
    };
  });
  return ok(res, { items, page, limit, total: Number(total) });
}

// ---------- Admin ----------

// AD01 GET /api/admin/bookings?status=PENDING&date=2030-01-15&facilityId=1&userId=2&page=1&limit=20   (oldest first)
async function adminListBookings(req, res) {
  const { rows, ...paging } = await listBookings(req, { order: 'ASC', allowFacilityFilter: true });
  return ok(res, { items: rows.map(toAdminBooking), ...paging });
}

// AD02 PATCH /api/admin/bookings/:bookingId/status
//   { "status": "APPROVED" }
//   { "status": "REJECTED", "rejectionReason": "Phòng cần dùng cho kỳ thi" }
async function updateBookingStatus(req, res) {
  const bookingId = parseId(req.params.bookingId, 'bookingId');
  const body = readBody(req.body, ['status', 'rejectionReason']);
  const status = oneOf(body.status, 'status', ['APPROVED', 'REJECTED']);
  let rejectionReason = null;
  if (status === 'APPROVED' && body.rejectionReason !== undefined) throw badRequest('APPROVED không nhận rejectionReason');
  if (status === 'REJECTED') rejectionReason = requiredText(body.rejectionReason, 'rejectionReason', 500);

  const [found] = await pool.query('SELECT facility_id FROM bookings WHERE booking_id = ?', [bookingId]);
  if (found.length === 0) throw bookingNotFound();

  await withTransaction(async (conn) => {
    const { facility, booking } = await lockFacilityAndBooking(conn, bookingId, found[0].facility_id);
    if (booking.status !== 'PENDING') {
      throw new AppError(409, 'BOOKING_ALREADY_PROCESSED', `Lịch đặt không còn ở trạng thái chờ duyệt (hiện tại: ${booking.status})`);
    }
    const now = Date.now();
    if (status === 'APPROVED') {
      if (sqlToMs(booking.start_time) <= now) throw new AppError(409, 'BOOKING_STARTED', 'Đã đến giờ bắt đầu, không thể duyệt; hãy từ chối');
      if (facility.operational_status !== 'ACTIVE') throw facilityUnavailable();
    }
    await conn.query(
      'UPDATE bookings SET status = ?, rejection_reason = ?, reviewed_by = ?, reviewed_at = ? WHERE booking_id = ?',
      [status, rejectionReason, req.user.userId, toSql(now), bookingId]
    );
  });

  const message = status === 'APPROVED' ? 'Đã duyệt lịch đặt' : 'Đã từ chối lịch đặt';
  return ok(res, toAdminBooking(await loadBooking(pool, bookingId)), message);
}

module.exports = {
  getAvailability, createBooking, getMyBookings, cancelBooking, getNotifications,
  adminListBookings, updateBookingStatus,
};
