const Sales = require('../sales/salesModel');
const Inventory = require('../inventory/inventoryModel');
const Attendance = require('../attendance/attendanceModel');
const Branch = require('../branch/branchModel');

/**
 * Calculates health score for an outlet.
 * @param {String} outletId
 * @returns {Object} { healthScore: Number, factors: Array }
 */
const calculateHealthScore = async (outletId) => {
    let healthScore = 100; // Start perfect
    const factors = [];

    const query = outletId === 'global' ? {} : { branchId: outletId };
    const yesterday = new Date(Date.now() - 24 * 60 * 60 * 1000);
    const recentQuery = { ...query, createdAt: { $gt: yesterday } };

    // 1. Sales Consistency
    const salesYesterday = await Sales.countDocuments(recentQuery);
    if (salesYesterday === 0) {
        healthScore -= 20;
        factors.push('No sales reported in last 24h');
    }

    // Identify Sales Spikes
    // Compare last 24h against average of last 7 days
    const lastWeek = new Date(Date.now() - 7 * 24 * 60 * 60 * 1000);
    const last7DaysQuery = { ...query, createdAt: { $gt: lastWeek } };
    const salesLast7Days = await Sales.countDocuments(last7DaysQuery);
    const dailyAvg = (salesLast7Days - salesYesterday) / 6;

    if (dailyAvg > 1 && salesYesterday > (dailyAvg * 2)) {
        healthScore -= 10;
        factors.push('Unusual spike in sales detected relative to daily average');
    }

    // 2. Attendance Compliance
    const attendanceCount = await Attendance.countDocuments(recentQuery);

    // Anomaly: Sales occurred but no attendance registered
    if (attendanceCount === 0) {
        if (salesYesterday > 0) {
            healthScore -= 30; // Critical Red Flag
            factors.push('CRITICAL: Sales registered but no staff attendance reported in 24h');
        } else {
            healthScore -= 15;
            factors.push('No attendance reported in last 24h');
        }
    }

    // 3. Low Stock Check
    const inventoryItems = await Inventory.find(query);
    const totalItems = inventoryItems.length;
    const lowStockItems = inventoryItems.filter(i => i.quantity < 10).length;

    if (totalItems > 0) {
        const lowStockPercentage = (lowStockItems / totalItems) * 100;
        if (lowStockPercentage > 20) {
            healthScore -= 30;
            factors.push(`High percentage (${Math.round(lowStockPercentage)}%) of items are critically low in stock`);
        } else if (lowStockPercentage > 0) {
            healthScore -= 15;
            factors.push(`${lowStockItems} items are running low on stock`);
        }
    } else {
        healthScore -= 10;
        factors.push('No inventory data available for health assessment');
    }

    // Cap minimum at 0
    healthScore = Math.max(0, healthScore);

    return { healthScore, factors };
};

module.exports = { calculateHealthScore };
