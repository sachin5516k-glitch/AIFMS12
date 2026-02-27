const asyncHandler = require('express-async-handler');
const Attendance = require('./attendanceModel');
const Branch = require('../branch/branchModel');

function getDistanceFromLatLonInM(lat1, lon1, lat2, lon2) {
    const R = 6371e3; // Radius of the earth in m
    const dLat = (lat2 - lat1) * Math.PI / 180;
    const dLon = (lon2 - lon1) * Math.PI / 180;
    const a =
        Math.sin(dLat / 2) * Math.sin(dLat / 2) +
        Math.cos(lat1 * Math.PI / 180) * Math.cos(lat2 * Math.PI / 180) *
        Math.sin(dLon / 2) * Math.sin(dLon / 2);
    const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    const d = R * c; // Distance in m
    return d;
}

const checkIn = asyncHandler(async (req, res) => {
    const branchId = req.user.role === 'admin' ? req.body.branchId : req.user.branchId;
    const lat = req.body.lat || req.body.latitude;
    const lng = req.body.lng || req.body.longitude;

    if (!branchId || !lat || !lng) {
        res.status(400);
        throw new Error('Please provide branchId, lat, and lng');
    }

    const branch = await Branch.findById(branchId);
    if (!branch) {
        res.status(404);
        throw new Error('Branch not found');
    }

    const distance = getDistanceFromLatLonInM(lat, lng, branch.location.lat, branch.location.lng);
    if (distance > 200) {
        res.status(403);
        throw new Error(`You are too far from the branch. Distance: ${Math.round(distance)}m. Max allowed: 200m.`);
    }

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
        branchId,
        checkInTime: new Date(),
        location: { lat, lng }
    });

    res.status(201).json({
        success: true,
        message: 'Checked in successfully',
        data: attendance
    });
});

const checkOut = asyncHandler(async (req, res) => {
    const branchId = req.user.role === 'admin' ? req.body.branchId : req.user.branchId;

    const attendance = await Attendance.findOne({
        userId: req.user._id,
        branchId,
        checkOutTime: null,
    });

    if (!attendance) {
        res.status(400);
        throw new Error('You are not checked in');
    }

    attendance.checkOutTime = new Date();
    await attendance.save();

    res.status(200).json({
        success: true,
        message: 'Checked out successfully',
        data: attendance
    });
});

module.exports = { checkIn, checkOut };
