const asyncHandler = require('express-async-handler');
const Sales = require('../sales/salesModel');
const Inventory = require('../inventory/inventoryModel');
const Attendance = require('../attendance/attendanceModel');

// @desc    Get Sales Report
// @route   GET /api/reports/sales
// @access  Private/Owner
const getSalesReport = asyncHandler(async (req, res) => {
    const { startDate, endDate, outletId } = req.query;

    const query = {};
    if (outletId) query.outletId = outletId;
    if (startDate && endDate) {
        query.createdAt = {
            $gte: new Date(startDate),
            $lte: new Date(endDate)
        };
    }

    const report = await Sales.aggregate([
        { $match: query },
        {
            $group: {
                _id: "$outletId",
                totalSales: { $sum: "$amount" },
                avgFraudScore: { $avg: "$fraudScore" },
                transactionCount: { $sum: 1 }
            }
        }
    ]);

    res.json(report);
});

module.exports = { getSalesReport };
