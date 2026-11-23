// QUY ƯỚC THỜI GIAN (docs/REST_API.md mục 2)
//
// - HTTP nhận RFC 3339, BẮT BUỘC có giây và múi giờ ("Z" hoặc "+07:00"), không phần lẻ giây:
//       "2030-01-15T09:00:00+07:00"  hoặc  "2030-01-15T02:00:00Z"
//   Chuỗi thiếu múi giờ ("2030-01-15T09:00:00") bị từ chối 400.
// - HTTP luôn TRẢ VỀ UTC dạng "2030-01-15T02:00:00Z".
//   Android: Instant.parse(text) rồi .atZone(ZoneId.of("Asia/Ho_Chi_Minh")) để hiển thị.
// - Database DATETIME lưu UTC (mỗi kết nối MySQL đặt time_zone = '+00:00', xem config/db.js).
// - "Ngày campus" (vd: ?date=2030-01-15) là ngày theo giờ Asia/Ho_Chi_Minh.
//   Việt Nam không có giờ mùa hè nên luôn là UTC+7.
//
// Inside the backend an instant is a plain number: milliseconds since epoch (UTC).

const CAMPUS_TIME_ZONE = 'Asia/Ho_Chi_Minh';
const CAMPUS_OFFSET_MS = 7 * 3600 * 1000;
const DAY_MS = 24 * 3600 * 1000;

const RFC3339_RE = /^(\d{4})-(\d{2})-(\d{2})[Tt](\d{2}):(\d{2}):(\d{2})([Zz]|([+-])(\d{2}):(\d{2}))$/;
const DATE_RE = /^(\d{4})-(\d{2})-(\d{2})$/;
const TIME_RE = /^(\d{2}):(\d{2})$/;

// true if y-m-d is a real calendar day (rejects 2030-02-30)
function isRealDay(y, m, d) {
  const t = new Date(Date.UTC(y, m - 1, d));
  return t.getUTCFullYear() === y && t.getUTCMonth() === m - 1 && t.getUTCDate() === d;
}

// "2030-01-15T09:00:00+07:00" -> ms (UTC), or null if the text is not valid RFC 3339
function parseInstant(text) {
  if (typeof text !== 'string') return null;
  const m = RFC3339_RE.exec(text);
  if (!m) return null;
  const [y, mo, d, hh, mi, ss] = m.slice(1, 7).map(Number);
  if (!isRealDay(y, mo, d) || hh > 23 || mi > 59 || ss > 59) return null;
  let offsetMs = 0;
  if (m[8]) {
    const oh = Number(m[9]);
    const om = Number(m[10]);
    if (oh > 23 || om > 59) return null;
    offsetMs = (m[8] === '-' ? -1 : 1) * (oh * 60 + om) * 60000;
  }
  return Date.UTC(y, mo - 1, d, hh, mi, ss) - offsetMs;
}

// "2030-01-15" (campus day) -> ms of 00:00 that day in campus time, or null if invalid
function parseCampusDate(text) {
  if (typeof text !== 'string') return null;
  const m = DATE_RE.exec(text);
  if (!m) return null;
  const [y, mo, d] = m.slice(1).map(Number);
  if (!isRealDay(y, mo, d)) return null;
  return Date.UTC(y, mo - 1, d) - CAMPUS_OFFSET_MS;
}

const pad = (n) => String(n).padStart(2, '0');

// ms -> "2030-01-15 02:00:00" (UTC) for SQL parameters
function toSql(ms) {
  return new Date(ms).toISOString().slice(0, 19).replace('T', ' ');
}

// ms -> "2030-01-15T02:00:00Z" for JSON
function toApi(ms) {
  return new Date(ms).toISOString().slice(0, 19) + 'Z';
}

// MySQL "2030-01-15 02:00:00" (UTC) -> ms
function sqlToMs(text) {
  return Date.parse(String(text).replace(' ', 'T') + 'Z');
}

// MySQL "2030-01-15 02:00:00" (UTC) -> "2030-01-15T02:00:00Z"; null stays null
function sqlToApi(text) {
  return text ? String(text).replace(' ', 'T') + 'Z' : null;
}

// ms -> { date: "2030-01-15", secondsOfDay } in campus time
function campusParts(ms) {
  const local = new Date(ms + CAMPUS_OFFSET_MS);
  return {
    date: local.toISOString().slice(0, 10),
    secondsOfDay: local.getUTCHours() * 3600 + local.getUTCMinutes() * 60 + local.getUTCSeconds(),
  };
}

// ms -> "09:00 15/01/2030" in campus time, for messages shown to users
function formatCampus(ms) {
  const l = new Date(ms + CAMPUS_OFFSET_MS);
  return `${pad(l.getUTCHours())}:${pad(l.getUTCMinutes())} ${pad(l.getUTCDate())}/${pad(l.getUTCMonth() + 1)}/${l.getUTCFullYear()}`;
}

// "07:00" -> 25200 (seconds after midnight)
function parseClock(text) {
  const m = TIME_RE.exec(text || '');
  if (!m) throw new Error(`Invalid clock time "${text}", expected HH:mm`);
  return Number(m[1]) * 3600 + Number(m[2]) * 60;
}

// 25200 -> "07:00:00"
function formatClock(seconds) {
  return `${pad(Math.floor(seconds / 3600))}:${pad(Math.floor((seconds % 3600) / 60))}:${pad(seconds % 60)}`;
}

module.exports = {
  CAMPUS_TIME_ZONE, DAY_MS,
  parseInstant, parseCampusDate, toSql, toApi, sqlToMs, sqlToApi, campusParts, parseClock, formatClock, formatCampus,
};
