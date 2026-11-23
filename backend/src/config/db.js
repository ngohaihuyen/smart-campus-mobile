const mysql = require('mysql2/promise');

// One shared connection pool for the whole app
const pool = mysql.createPool({
  host: process.env.DB_HOST,
  port: Number(process.env.DB_PORT || 3306),
  user: process.env.DB_USER,
  password: process.env.DB_PASSWORD,
  database: process.env.DB_NAME,
  waitForConnections: true,
  connectionLimit: 10,
  // DATETIME comes back as the raw string "2030-01-15 02:00:00" (UTC); utils/time.js converts it
  dateStrings: true,
  // BIGINT UNSIGNED ids come back as exact strings ("12"), never rounded JS numbers
  supportBigNumbers: true,
  bigNumberStrings: true,
});

// Database stores UTC: make CURRENT_TIMESTAMP / NOW() use UTC on every connection,
// whatever time zone the laptop running MySQL is set to.
pool.pool.on('connection', (conn) => {
  conn.query("SET time_zone = '+00:00'");
});

// Runs fn(conn) inside a transaction on ONE connection.
// Commits if fn resolves, rolls back if it throws, always returns the connection to the pool.
async function withTransaction(fn) {
  const conn = await pool.getConnection();
  try {
    await conn.beginTransaction();
    const result = await fn(conn);
    await conn.commit();
    return result;
  } catch (err) {
    await conn.rollback().catch(() => {});
    throw err;
  } finally {
    conn.release();
  }
}

module.exports = pool;
module.exports.withTransaction = withTransaction;
