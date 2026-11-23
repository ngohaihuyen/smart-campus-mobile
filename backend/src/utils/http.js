// Khuôn phản hồi chung (docs/REST_API.md mục 2) - Android parse bằng một class duy nhất:
//   { "success": true,  "message": "...", "data": {...}, "error": null }
//   { "success": false, "message": "...", "data": null,  "error": { "code": "BOOKING_CONFLICT" } }
// Android rẽ nhánh theo error.code (ổn định); message chỉ để hiển thị cho người dùng.

function ok(res, data, message = 'Thành công', status = 200) {
  return res.status(status).json({ success: true, message, data, error: null });
}

// Throw this anywhere in a request to send a clean error response.
// code must be one of the codes in docs/REST_API.md mục 7.
// extra (optional) is merged into the "error" object, e.g. { conflicts: [...] } for BOOKING_CONFLICT.
class AppError extends Error {
  constructor(status, code, message, extra) {
    super(message);
    this.status = status;
    this.code = code;
    this.extra = extra;
  }
}

// Shortcuts for the most common errors
const badRequest = (message) => new AppError(400, 'VALIDATION_ERROR', message);
const forbidden = (message = 'Bạn không có quyền thực hiện thao tác này') => new AppError(403, 'FORBIDDEN', message);

module.exports = { ok, AppError, badRequest, forbidden };
