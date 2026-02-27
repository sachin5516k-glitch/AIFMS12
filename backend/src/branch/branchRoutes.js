const express = require('express');
const router = express.Router();
const {
    createBranch,
    getBranches,
} = require('./branchController');
const { protect, authorize } = require('../middleware/authMiddleware');

router.route('/')
    .post(protect, authorize('admin'), createBranch)
    .get(protect, authorize('admin', 'manager', 'employee'), getBranches);

module.exports = router;
