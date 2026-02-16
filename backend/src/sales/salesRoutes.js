const express = require('express');
const router = express.Router();
const { submitSales, getSales } = require('./salesController');
const { protect } = require('../middleware/authMiddleware');

router.post('/submit', protect, submitSales);
router.get('/:outletId', protect, getSales);

module.exports = router;
