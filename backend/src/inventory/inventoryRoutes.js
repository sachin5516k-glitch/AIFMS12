const express = require('express');
const router = express.Router();
const { submitInventory, getInventoryItems } = require('./inventoryController');
const { protect } = require('../middleware/authMiddleware');

router.post('/submit', protect, submitInventory);
router.get('/items', protect, getInventoryItems);

module.exports = router;
