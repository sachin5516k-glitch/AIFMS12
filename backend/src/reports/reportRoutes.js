const express = require('express');
const router = express.Router();
const { getSalesReport, exportSalesCsv } = require('../reports/report.controller');
const { raiseDispute } = require('../dispute/dispute.controller');
const { protect } = require('../middleware/authMiddleware');
const { seedDatabase } = require('../admin/seedController');

router.get('/reports/sales', protect, getSalesReport);
router.get('/reports/export/sales', protect, exportSalesCsv);
router.post('/dispute', protect, raiseDispute);
router.post('/seedDataNow', seedDatabase);

module.exports = router;
