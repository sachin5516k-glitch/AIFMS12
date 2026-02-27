const mongoose = require('mongoose');

const notificationSchema = new mongoose.Schema({
    branchId: {
        type: mongoose.Schema.Types.ObjectId,
        ref: 'Branch',
        default: null // null indicates it's a global notification or for an admin
    },
    title: {
        type: String,
        required: true
    },
    message: {
        type: String,
        required: true
    },
    type: {
        type: String,
        enum: ['INFO', 'WARNING', 'TRANSFER_RECOMMENDATION', 'TRANSFER_REQUEST', 'TRANSFER_APPROVED', 'TRANSFER_REJECTED'],
        default: 'INFO'
    },
    isRead: {
        type: Boolean,
        default: false
    }
}, {
    timestamps: true
});

notificationSchema.index({ branchId: 1, isRead: 1 });

module.exports = mongoose.model('Notification', notificationSchema);
