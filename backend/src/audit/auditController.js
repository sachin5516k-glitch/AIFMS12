const asyncHandler = require('express-async-handler');
const AuditLog = require('./auditLogModel');

// @desc    Get system audit logs
// @route   GET /api/audit
// @access  Private (Admin only)
const getAuditLogs = asyncHandler(async (req, res) => {
    // Only Admin can view audit logs
    if (req.user.role !== 'admin') {
        res.status(403);
        throw new Error('Not authorized to view audit logs');
    }

    const { action, startDate, endDate, limit } = req.query;
    let query = {};

    if (action) query.action = action;
    if (startDate && endDate) {
        query.createdAt = {
            $gte: new Date(startDate),
            $lte: new Date(endDate)
        };
    }

    const maxLimit = limit ? parseInt(limit) : 50;

    const logs = await AuditLog.find(query)
        .populate('userId', 'name email role')
        .sort({ createdAt: -1 })
        .limit(maxLimit);

    res.status(200).json({
        success: true,
        data: logs
    });
});

module.exports = {
    getAuditLogs
};
