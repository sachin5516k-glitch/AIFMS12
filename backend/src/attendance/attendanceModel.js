const mongoose = require('mongoose');

const attendanceSchema = mongoose.Schema(
    {
        userId: {
            type: mongoose.Schema.Types.ObjectId,
            ref: 'User',
            required: true,
        },
        outletId: {
            type: String,
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
            latitude: Number,
            longitude: Number,
            // In prod: GeoJSON
        },
        photoUrl: {
            type: String,
            required: true,
        },
        status: {
            type: String,
            enum: ['present', 'absent', 'late'], // logic for 'late' to be added
            default: 'present',
        },
    },
    {
        timestamps: true,
    }
);

module.exports = mongoose.model('Attendance', attendanceSchema);
