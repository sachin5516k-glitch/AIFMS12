const express = require('express');
const router = express.Router();
const { createFranchise, getFranchises } = require('./franchiseController');
const { protect, authorize } = require('../middleware/authMiddleware');

router.route('/')
    .post(protect, authorize('owner'), createFranchise)
    .get(protect, getFranchises);

module.exports = router;
