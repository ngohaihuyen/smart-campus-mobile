const jwt = require('jsonwebtoken');
const pool = require('../config/db');
const { AppError, forbidden } = require('../utils/http');

// XỬ LÝ JWT (docs/REST_API.md mục 2, mục 7)
// Header bắt buộc: "Authorization: Bearer <accessToken>".
// Mọi lỗi token -> HTTP 401, error.code = "UNAUTHORIZED", header "WWW-Authenticate: Bearer".
// Chỉ message khác nhau để hiển thị:
//   thiếu token       -> "Bạn chưa đăng nhập"
//   token hết hạn     -> "Phiên đăng nhập đã hết hạn, vui lòng đăng nhập lại"
//   token sai/bị sửa  -> "Phiên đăng nhập không hợp lệ, vui lòng đăng nhập lại"
//   user đã bị xóa    -> "Tài khoản không còn tồn tại"
// Android: gặp 401 ở bất kỳ API nào (trừ login) -> xóa token đã lưu, chuyển về màn hình Login.
const unauthorized = (message) => new AppError(401, 'UNAUTHORIZED', message);

async function requireAuth(req, res, next) {
  const header = req.headers.authorization || '';
  const [scheme, token] = header.split(' ');
  if (scheme !== 'Bearer' || !token) throw unauthorized('Bạn chưa đăng nhập');

  let payload;
  try {
    payload = jwt.verify(token, process.env.JWT_SECRET, { algorithms: ['HS256'] });
  } catch (err) {
    if (err.name === 'TokenExpiredError') throw unauthorized('Phiên đăng nhập đã hết hạn, vui lòng đăng nhập lại');
    throw unauthorized('Phiên đăng nhập không hợp lệ, vui lòng đăng nhập lại');
  }

  // Read the CURRENT user from the database (role may have changed, user may be gone)
  const [rows] = await pool.query('SELECT user_id, full_name, role FROM users WHERE user_id = ?', [String(payload.sub)]);
  if (rows.length === 0) throw unauthorized('Tài khoản không còn tồn tại');
  req.user = { userId: String(rows[0].user_id), fullName: rows[0].full_name, role: rows[0].role };
  next();
}

// Usage: requireRole('ADMIN') or requireRole('STUDENT', 'TEACHER')
function requireRole(...roles) {
  return (req, res, next) => {
    if (!roles.includes(req.user.role)) throw forbidden();
    next();
  };
}

module.exports = { requireAuth, requireRole };
