require('dotenv').config({ quiet: true });
const os = require('os');
const app = require('./app');
const pool = require('./config/db');

const PORT = Number(process.env.PORT || 3000);

// IPv4 addresses of this laptop on Wi-Fi/LAN, so the Android team knows which base URL to use
function lanAddresses() {
  return Object.values(os.networkInterfaces())
    .flat()
    .filter((a) => a && a.family === 'IPv4' && !a.internal)
    .map((a) => a.address);
}

async function start() {
  if (!process.env.JWT_SECRET) {
    console.error('Missing JWT_SECRET in .env');
    process.exit(1);
  }
  try {
    await pool.query('SELECT 1'); // fail fast if MySQL is not reachable
    console.log('Connected to MySQL');
  } catch (err) {
    console.error('Cannot connect to MySQL:', err.message);
    process.exit(1);
  }
  // 0.0.0.0 = accept connections from other devices on the same Wi-Fi (real phone)
  app.listen(PORT, '0.0.0.0', () => {
    console.log(`Smart Campus API running:`);
    console.log(`  This laptop:       http://localhost:${PORT}/api`);
    console.log(`  Android emulator:  http://10.0.2.2:${PORT}/api`);
    for (const ip of lanAddresses()) console.log(`  Phone on same Wi-Fi: http://${ip}:${PORT}/api`);
  });
}

start();
