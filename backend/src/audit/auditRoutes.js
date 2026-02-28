const express = require('express');
const { getAuditLogs } = require('./auditController');
const { protect } = require('../middleware/authMiddleware');

const router = express.Router();

router.route('/')
    .get(protect, getAuditLogs);

module.exports = router;
