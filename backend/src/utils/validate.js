// Input checks shared by all controllers. Every failure is 400 VALIDATION_ERROR.
const { badRequest } = require('./http');

const MAX_BIGINT_UNSIGNED = 18446744073709551615n;
const MAX_INT_UNSIGNED = 4294967295;

// Body must be a JSON object and may only contain the allowed keys.
// Unknown keys (e.g. "role", "userId", "reviewedBy") are rejected, never silently ignored.
function readBody(body, allowed) {
  if (body === undefined) return {};
  if (body === null || typeof body !== 'object' || Array.isArray(body)) throw badRequest('Body phải là một JSON object');
  const unknown = Object.keys(body).filter((k) => !allowed.includes(k));
  if (unknown.length > 0) throw badRequest(`Trường không được phép: ${unknown.join(', ')}`);
  return body;
}

// IDs travel as strings of a positive integer ("12") to keep BIGINT UNSIGNED exact.
function parseId(value, field) {
  if (typeof value !== 'string' || !/^[1-9]\d{0,19}$/.test(value) || BigInt(value) > MAX_BIGINT_UNSIGNED) {
    throw badRequest(`${field} phải là chuỗi số nguyên dương, ví dụ "1"`);
  }
  return value;
}

// Required string: trimmed, 1..max characters
function requiredText(value, field, max) {
  if (typeof value !== 'string' || !value.trim()) throw badRequest(`${field} là bắt buộc và không được rỗng`);
  const text = value.trim();
  if ([...text].length > max) throw badRequest(`${field} tối đa ${max} ký tự`);
  return text;
}

// Optional string or null: trimmed; empty string becomes null
function optionalText(value, field, { maxChars, maxBytes } = {}) {
  if (value === null || value === undefined) return null;
  if (typeof value !== 'string') throw badRequest(`${field} phải là chuỗi hoặc null`);
  const text = value.trim();
  if (!text) return null;
  if (maxChars && [...text].length > maxChars) throw badRequest(`${field} tối đa ${maxChars} ký tự`);
  if (maxBytes && Buffer.byteLength(text, 'utf8') > maxBytes) throw badRequest(`${field} quá dài`);
  return text;
}

function oneOf(value, field, allowed) {
  if (!allowed.includes(value)) throw badRequest(`${field} phải là một trong: ${allowed.join(', ')}`);
  return value;
}

function positiveInt(value, field, max = MAX_INT_UNSIGNED) {
  if (!Number.isInteger(value) || value <= 0 || value > max) throw badRequest(`${field} phải là số nguyên dương`);
  return value;
}

// ?page=1&limit=20 -> { page, limit, offset }. Out-of-range values are 400, never clamped.
function readPaging(query) {
  const read = (name, fallback, max) => {
    const raw = query[name];
    if (raw === undefined) return fallback;
    if (typeof raw !== 'string' || !/^[1-9]\d*$/.test(raw) || Number(raw) > max) {
      throw badRequest(`${name} phải là số nguyên dương${max < Infinity ? ` và không vượt quá ${max}` : ''}`);
    }
    return Number(raw);
  };
  const page = read('page', 1, 1000000);
  const limit = read('limit', 20, 100);
  return { page, limit, offset: (page - 1) * limit };
}

// Optional single-value query parameter (rejects ?status=A&status=B)
function queryText(query, name) {
  const raw = query[name];
  if (raw === undefined) return undefined;
  if (typeof raw !== 'string') throw badRequest(`${name} chỉ được truyền một lần`);
  return raw;
}

module.exports = { readBody, parseId, requiredText, optionalText, oneOf, positiveInt, readPaging, queryText };
