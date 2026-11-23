const express = require('express');
const { requireAuth, requireRole } = require('../middleware/auth');
const auth = require('../controllers/auth.controller');
const facility = require('../controllers/facility.controller');
const booking = require('../controllers/booking.controller');
const { toApi, CAMPUS_TIME_ZONE } = require('../utils/time');

// Every endpoint lives here. Contract: docs/REST_API.md (copied from the mobile repo)
// Codes in comments (A02, B02...) match that document.
const router = express.Router();
const adminOnly = requireRole('ADMIN');
const bookers = requireRole('STUDENT', 'TEACHER'); // ADMIN does not book

// Public
router.get('/health', (req, res) => res.json({
  success: true,
  message: 'Smart Campus API is running',
  data: { serverTime: toApi(Date.now()), timeZone: CAMPUS_TIME_ZONE },
  error: null,
}));
// A01 register was dropped by the team: accounts are provided by the school.
// Answer it clearly instead of letting it fall through to "Bạn chưa đăng nhập" (401).
router.all('/auth/register', (req, res) => res.status(404).json({
  success: false,
  message: 'Không có chức năng đăng ký. Tài khoản do nhà trường cấp',
  data: null,
  error: { code: 'NOT_FOUND' },
}));
router.post('/auth/login', auth.login); // A02

// Everything below needs "Authorization: Bearer <accessToken>"
router.use(requireAuth);

router.get('/auth/me', auth.me); // extra: check a saved token

router.get('/facilities', facility.listFacilities);           // F01
router.get('/facilities/:facilityId', facility.getFacility);  // F02

router.get('/bookings/availability', booking.getAvailability);           // B01 (before /:bookingId)
router.post('/bookings', bookers, booking.createBooking);                // B02
router.get('/bookings/my', bookers, booking.getMyBookings);              // B03
router.delete('/bookings/:bookingId', bookers, booking.cancelBooking);   // B04
router.get('/notifications', bookers, booking.getNotifications);        // workflow: approved/rejected notices

router.get('/admin/bookings', adminOnly, booking.adminListBookings);                       // AD01
router.patch('/admin/bookings/:bookingId/status', adminOnly, booking.updateBookingStatus); // AD02
router.post('/admin/facilities', adminOnly, facility.createFacility);                      // AD03
router.patch('/admin/facilities/:facilityId', adminOnly, facility.updateFacility);         // AD04
router.delete('/admin/facilities/:facilityId', adminOnly, facility.deactivateFacility);    // catalogue: soft stop, keeps history

module.exports = router;
