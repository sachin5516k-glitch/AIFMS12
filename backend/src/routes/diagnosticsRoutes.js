const express = require('express');
const router = express.Router();
const mongoose = require('mongoose');
const { protect, authorize } = require('../middleware/authMiddleware');
const os = require('os');

// @desc    Get system diagnostics
// @route   GET /api/admin/diagnostics
// @access  Private/Admin
router.get('/diagnostics', protect, authorize('admin'), (req, res) => {

    const dbStatus = mongoose.connection.readyState === 1 ? 'Connected' : 'Disconnected';
    const usedMemory = process.memoryUsage().heapUsed / 1024 / 1024;

    res.json({
        system: {
            uptime: process.uptime(),
            platform: os.platform(),
            totalMemory: `${(os.totalmem() / 1024 / 1024).toFixed(2)} MB`,
            freeMemory: `${(os.freemem() / 1024 / 1024).toFixed(2)} MB`,
            appMemoryUsage: `${usedMemory.toFixed(2)} MB`
        },
        database: {
            status: dbStatus,
            host: mongoose.connection.host,
            name: mongoose.connection.name
        },
        environment: {
            nodeEnv: process.env.NODE_ENV,
            version: process.env.npm_package_version || '1.0.0'
        },
        timestamp: new Date().toISOString()
    });
});

module.exports = router;
