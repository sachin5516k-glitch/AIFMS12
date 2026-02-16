const asyncHandler = require('express-async-handler');
const Attendance = require('./attendanceModel');

// @desc    Check In
// @route   POST /api/attendance/checkin
// @access  Private
const checkIn = asyncHandler(async (req, res) => {
    const { outletId, latitude, longitude, imageUrl } = req.body;

    if (!outletId || !latitude || !longitude || !imageUrl) {
        res.status(400);
        throw new Error('Please provide all check-in details');
    }

    // Check if already checked in today without checkout
    const existingAttendance = await Attendance.findOne({
        userId: req.user._id,
        checkOutTime: null,
    });

    if (existingAttendance) {
        res.status(400);
        throw new Error('You are already checked in');
    }

    const attendance = await Attendance.create({
        userId: req.user._id,
        outletId,
        checkInTime: new Date(),
        location: { latitude, longitude },
        photoUrl: imageUrl,
    });

    res.status(201).json(attendance);
});

// @desc    Check Out
// @route   POST /api/attendance/checkout
// @access  Private
const checkOut = asyncHandler(async (req, res) => {
    const { outletId } = req.body;

    const attendance = await Attendance.findOne({
        userId: req.user._id,
        outletId,
        checkOutTime: null,
    });

    if (!attendance) {
        res.status(400);
        throw new Error('You are not checked in');
    }

    attendance.checkOutTime = new Date();
    await attendance.save();

    res.status(200).json(attendance);
});

module.exports = { checkIn, checkOut };
