const express = require('express');
const router = express.Router();
const logger = require('../utils/logger');
const { protect } = require('../middleware/authMiddleware');

// @desc    Log frontend crash report
// @route   POST /api/logs/crash
// @access  Private (or Public with API Key for unauth crashes)
router.post('/crash', protect, (req, res) => {
    const { stackTrace, device, exception, userNote } = req.body;

    logger.error(`FRONTEND CRASH - ${exception}`, {
        service: 'android-frontend',
        device: device,
        stack: stackTrace,
        userNote: userNote,
        userId: req.user ? req.user._id : 'unknown'
    });

    res.status(200).json({ message: 'Crash report logged' });
});

module.exports = router;
