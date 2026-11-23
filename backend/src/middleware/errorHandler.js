// Last middlewares: turn anything thrown into the standard error JSON.
const { AppError } = require('../utils/http');

function sendError(res, status, code, message, extra) {
  if (status === 401) res.set('WWW-Authenticate', 'Bearer');
  res.status(status).json({ success: false, message, data: null, error: { code, ...extra } });
}

function notFound(req, res) {
  sendError(res, 404, 'NOT_FOUND', `Không tìm thấy ${req.method} ${req.path}`);
}

// MySQL errors that mean "lock contention, try again" -> 503 DATABASE_BUSY
const BUSY_ERRNOS = [1205 /* lock wait timeout */, 1213 /* deadlock */];

function errorHandler(err, req, res, next) {
  if (err.type === 'entity.parse.failed') return sendError(res, 400, 'VALIDATION_ERROR', 'Body JSON không hợp lệ');
  if (err.type === 'entity.too.large') return sendError(res, 400, 'VALIDATION_ERROR', 'Body quá lớn');
  if (err instanceof AppError) return sendError(res, err.status, err.code, err.message, err.extra);
  if (BUSY_ERRNOS.includes(err.errno)) {
    return sendError(res, 503, 'DATABASE_BUSY', 'Hệ thống đang bận, vui lòng tải lại và thử lại');
  }
  console.error(err); // real bug: print it in the terminal, never send details to the client
  sendError(res, 500, 'INTERNAL_ERROR', 'Lỗi máy chủ');
}

module.exports = { notFound, errorHandler };
