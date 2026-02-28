const mongoose = require('mongoose');

const auditLogSchema = new mongoose.Schema({
    userId: {
        type: mongoose.Schema.Types.ObjectId,
        ref: 'User',
        required: false, // Can be null for anonymous actions like failed logins before user is found
        index: true
    },
    action: {
        type: String,
        required: true,
        index: true
    },
    details: {
        type: String,
        required: true
    },
    ipAddress: {
        type: String,
        required: false
    },
    userAgent: {
        type: String,
        required: false
    }
}, {
    timestamps: true // createdAt serves as the timestamp of the event
});

// Index to quickly fetch logs ordered by time
auditLogSchema.index({ createdAt: -1 });

module.exports = mongoose.model('AuditLog', auditLogSchema);
