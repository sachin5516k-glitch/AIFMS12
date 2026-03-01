const mongoose = require('mongoose');

const attendanceSchema = mongoose.Schema(
    {
        userId: {
            type: mongoose.Schema.Types.ObjectId,
            ref: 'User',
            required: true,
        },
        branchId: {
            type: mongoose.Schema.Types.ObjectId,
            ref: 'Branch',
            required: true,
        },
        checkInTime: {
            type: Date,
            required: true,
        },
        checkOutTime: {
            type: Date,
        },
        location: {
            lat: { type: Number, required: true },
            lng: { type: Number, required: true }
        }
    },
    {
        timestamps: true,
    }
);

// Indexes for query performance
attendanceSchema.index({ branchId: 1, userId: 1 });

module.exports = mongoose.model('Attendance', attendanceSchema);
