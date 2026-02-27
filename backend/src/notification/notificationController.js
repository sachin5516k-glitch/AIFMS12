const asyncHandler = require('express-async-handler');
const Notification = require('./notificationModel');

// @desc    Get user notifications (Admin gets all global, Manager gets branch-specific)
// @route   GET /api/notifications
// @access  Private
const getNotifications = asyncHandler(async (req, res) => {
    let query = {};
    if (req.user.role === 'admin') {
        // Admin sees global notifications (branchId: null) or all? 
        // For now, let's say Admin sees all global and maybe all branch, but spec says "Admin: branchId: null".
        query = { branchId: null };
    } else {
        query = { branchId: req.user.branchId };
    }

    const notifications = await Notification.find(query).sort({ createdAt: -1 });

    const unreadCount = await Notification.countDocuments({ ...query, isRead: false });

    res.status(200).json({
        success: true,
        data: {
            notifications,
            unreadCount
        }
    });
});

// @desc    Mark a notification as read
// @route   PUT /api/notifications/:id/read
// @access  Private
const markAsRead = asyncHandler(async (req, res) => {
    const notification = await Notification.findById(req.params.id);

    if (!notification) {
        res.status(404);
        throw new Error('Notification not found');
    }

    // Verify ownership
    if (req.user.role !== 'admin' && notification.branchId && notification.branchId.toString() !== req.user.branchId.toString()) {
        res.status(403);
        throw new Error('Not authorized to update this notification');
    }

    notification.isRead = true;
    const updatedNotification = await notification.save();

    res.status(200).json({
        success: true,
        data: updatedNotification
    });
});

module.exports = {
    getNotifications,
    markAsRead
};
