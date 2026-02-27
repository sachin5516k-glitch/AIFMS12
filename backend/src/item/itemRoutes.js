const express = require('express');
const router = express.Router();
const {
    createItem,
    getItems,
} = require('./itemController');
const { protect, authorize } = require('../middleware/authMiddleware');

router.route('/')
    .post(protect, authorize('admin'), createItem)
    .get(protect, getItems);

module.exports = router;
