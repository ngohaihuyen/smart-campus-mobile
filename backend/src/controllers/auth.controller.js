const bcrypt = require('bcryptjs');
const jwt = require('jsonwebtoken');
const pool = require('../config/db');
const { ok, AppError, badRequest } = require('../utils/http');
const { readBody } = require('../utils/validate');
const { toUser } = require('../utils/mappers');

// No public registration: the team decided to drop POST /api/auth/register.
// Accounts are provided by the school (database/seed.sql for local testing).

const TOKEN_TTL_SECONDS = Number(process.env.JWT_EXPIRES_IN_SECONDS || 3600);

// Workflow "Xác thực tài khoản: có phải tài khoản của trường?"
// ALLOWED_EMAIL_DOMAINS=university.edu.vn,st.university.edu.vn  -> only these domains may log in.
// Empty = allow any domain (the seed accounts use example.test).
const ALLOWED_DOMAINS = (process.env.ALLOWED_EMAIL_DOMAINS || '')
  .split(',').map((d) => d.trim().toLowerCase()).filter(Boolean);

function isSchoolEmail(email) {
  return ALLOWED_DOMAINS.length === 0 || ALLOWED_DOMAINS.includes(email.slice(email.lastIndexOf('@') + 1));
}
const notSchoolMessage = () => `Chỉ chấp nhận tài khoản email của trường (@${ALLOWED_DOMAINS.join(', @')})`;

// A02 POST /api/auth/login  { email, password }
async function login(req, res) {
  const body = readBody(req.body, ['email', 'password']);
  if (typeof body.email !== 'string' || !body.email.trim() || typeof body.password !== 'string' || !body.password) {
    throw badRequest('Vui lòng nhập email và mật khẩu');
  }

  const email = body.email.trim().toLowerCase();
  // Checked before touching the database, so it reveals nothing about which accounts exist
  if (!isSchoolEmail(email)) throw new AppError(403, 'FORBIDDEN', notSchoolMessage());

  const [rows] = await pool.query('SELECT * FROM users WHERE email = ?', [email]);
  const user = rows[0];
  // Same message for "wrong email" and "wrong password" so nobody can probe which emails exist
  if (!user || !(await bcrypt.compare(body.password, user.password_hash))) {
    throw new AppError(401, 'INVALID_CREDENTIALS', 'Email hoặc mật khẩu không đúng');
  }

  const accessToken = jwt.sign({ sub: String(user.user_id), role: user.role }, process.env.JWT_SECRET, {
    algorithm: 'HS256',
    expiresIn: TOKEN_TTL_SECONDS,
  });
  return ok(res, { accessToken, tokenType: 'Bearer', expiresIn: TOKEN_TTL_SECONDS, user: toUser(user) }, 'Đăng nhập thành công');
}

// GET /api/auth/me  (extra, not in the catalogue) - lets the app check a saved token on startup
async function me(req, res) {
  const [rows] = await pool.query('SELECT * FROM users WHERE user_id = ?', [req.user.userId]);
  return ok(res, toUser(rows[0]));
}

module.exports = { login, me };
