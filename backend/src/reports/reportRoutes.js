const express = require('express');
const router = express.Router();
const { getSalesReport } = require('../reports/report.controller');
const { raiseDispute } = require('../dispute/dispute.controller');
const { protect } = require('../middleware/authMiddleware');

router.get('/reports/sales', protect, getSalesReport);
router.post('/dispute', protect, raiseDispute);

module.exports = router;
