const express = require('express');
const router = express.Router();
const { submitInventory, getInventoryItems } = require('./inventoryController');
const { protect, authorize } = require('../middleware/authMiddleware');

router.post('/submit', protect, authorize('manager', 'admin'), submitInventory);
router.get('/items', protect, getInventoryItems);

module.exports = router;
