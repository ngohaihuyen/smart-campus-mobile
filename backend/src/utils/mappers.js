// Convert MySQL rows (snake_case, UTC DATETIME strings, BIGINT as strings)
// into API objects (camelCase, RFC 3339 UTC, IDs as strings).
// These shapes ARE the contract in docs/REST_API.md mục 3 - change both together.
const { sqlToApi } = require('./time');

const idOrNull = (v) => (v === null || v === undefined ? null : String(v));

function toUser(row) {
  return {
    userId: String(row.user_id),
    fullName: row.full_name,
    email: row.email,
    role: row.role,
    createdAt: sqlToApi(row.created_at),
  };
}

function toFacility(row) {
  return {
    facilityId: String(row.facility_id),
    name: row.name,
    type: row.type,
    location: row.location,
    capacity: Number(row.capacity),
    description: row.description,
    accessPolicy: row.access_policy,
    operationalStatus: row.operational_status,
    unavailableReason: row.unavailable_reason,
    unavailableNote: row.unavailable_note,
    createdAt: sqlToApi(row.created_at),
    updatedAt: sqlToApi(row.updated_at),
  };
}

function toBooking(row) {
  return {
    bookingId: String(row.booking_id),
    userId: String(row.user_id),
    facilityId: String(row.facility_id),
    startTime: sqlToApi(row.start_time),
    endTime: sqlToApi(row.end_time),
    reason: row.reason,
    status: row.status,
    rejectionReason: row.rejection_reason,
    reviewedBy: idOrNull(row.reviewed_by),
    reviewedAt: sqlToApi(row.reviewed_at),
    cancelledAt: sqlToApi(row.cancelled_at),
    createdAt: sqlToApi(row.created_at),
    updatedAt: sqlToApi(row.updated_at),
  };
}

// Booking + facilityName (B03 "lịch của tôi")
function toMyBooking(row) {
  return { ...toBooking(row), facilityName: row.facility_name };
}

// Booking + userFullName + facilityName (AD01 and admin warnings)
function toAdminBooking(row) {
  return { ...toBooking(row), userFullName: row.user_full_name, facilityName: row.facility_name };
}

// Can this user create a booking in this facility right now? (ignores time slots)
// ADMIN never books; TEACHER_ONLY rooms are for TEACHER only.
function canBook(role, facilityRow) {
  if (facilityRow.operational_status !== 'ACTIVE') return false;
  if (role === 'TEACHER') return true;
  if (role === 'STUDENT') return facilityRow.access_policy === 'ALL';
  return false;
}

module.exports = { toUser, toFacility, toBooking, toMyBooking, toAdminBooking, canBook };
