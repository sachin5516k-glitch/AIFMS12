const Sales = require('../sales/salesModel');
const Inventory = require('../inventory/inventoryModel');
const Attendance = require('../attendance/attendanceModel');

/**
 * Calculates health score for an outlet.
 * @param {String} outletId
 * @returns {Object} { healthScore: Number, factors: Array }
 */
const calculateHealthScore = async (outletId) => {
    let healthScore = 100; // Start perfect
    const factors = [];

    // 1. Sales Consistency (Check if sales reported yesterday)
    const yesterday = new Date(Date.now() - 24 * 60 * 60 * 1000);
    const query = outletId === 'global' ? { createdAt: { $gt: yesterday } } : { outletId, createdAt: { $gt: yesterday } };
    const salesYesterday = await Sales.countDocuments(query);

    if (salesYesterday === 0) {
        healthScore -= 20;
        factors.push('No sales reported in last 24h');
    }

    // 2. Attendance Compliance
    const attendanceCount = await Attendance.countDocuments(query);

    if (attendanceCount === 0) {
        healthScore -= 20;
        factors.push('No attendance reported in last 24h');
    }

    // 3. Inventory Variance Impact
    // (Simplified: check if any high variance reported recently)
    // To implement proper variance logic here requires aggregation, simplified for now.

    // Cap minimum at 0
    healthScore = Math.max(0, healthScore);

    return { healthScore, factors };
};

module.exports = { calculateHealthScore };
