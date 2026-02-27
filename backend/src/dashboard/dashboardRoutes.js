const express = require('express');
const { getManagerDashboard, getAdminDashboard } = require('./dashboardController');
const { protect } = require('../middleware/authMiddleware');

const router = express.Router();

router.use(protect);

router.get('/manager', getManagerDashboard);
router.get('/admin', getAdminDashboard);

module.exports = router;
