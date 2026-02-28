const asyncHandler = require('express-async-handler');
const Inventory = require('../inventory/inventoryModel');
const Sales = require('../sales/salesModel');
const Branch = require('../branch/branchModel');
const Item = require('../item/itemModel');
const Attendance = require('../attendance/attendanceModel');
const BranchFinancials = require('../analytics/branchFinancialsModel');
const StockTransferRecommendation = require('../transfer/stockTransferRecommendationModel');
const StockTransferRequest = require('../transfer/stockTransferRequestModel');
const ProfitLossService = require('../services/profitLossService');
const ForecastService = require('../services/forecastService');

// Simple In-Memory Cache for Dashboard
const dashboardCache = {
    manager: {},
    admin: {}
};
const CACHE_TTL_MS = 60 * 1000; // 60 seconds

// @desc    Get Manager Dashboard Data
// @route   GET /api/dashboard/manager
// @access  Private (Manager)
const getManagerDashboard = asyncHandler(async (req, res) => {
    const branchId = req.user.branchId;
    if (!branchId) return res.status(400).json({ success: false, message: 'User does not belong to a branch' });

    // Check Cache
    const cachedData = dashboardCache.manager[branchId];
    if (cachedData && (Date.now() - cachedData.timestamp < CACHE_TTL_MS)) {
        return res.status(200).json({ success: true, cached: true, data: cachedData.data });
    }

    // Determine default dates
    const today = new Date();
    const thirtyDaysAgo = new Date();
    thirtyDaysAgo.setDate(today.getDate() - 30);
    const sevenDaysAgo = new Date();
    sevenDaysAgo.setDate(today.getDate() - 7);

    // 1. Profit & Loss (Trigger calculation for up-to-date daily P&L if not found)
    let financials = await ProfitLossService.calculateBranchFinancials(branchId, thirtyDaysAgo, today, 'MONTHLY');

    // 2. Revenue Trend (last 30 days) list
    const revenueTrendDocs = await BranchFinancials.find({ branchId, period: 'DAILY', date: { $gte: thirtyDaysAgo } }).sort({ date: 1 });
    const revenueTrend = revenueTrendDocs.map(f => ({ date: f.date, revenue: f.revenue }));

    // 3. Forecasting (Next 7 days & Top demands)
    const forecast = await ForecastService.getBranchForecast(branchId);

    // 4. Low margin items
    const allBranchInventories = await Inventory.find({ branchId }).populate('itemId');
    const lowMarginItems = allBranchInventories
        .filter(inv => inv.itemId.marginPercentage < 20) // assumed <20% is low
        .map(inv => ({ item: inv.itemId.name, margin: inv.itemId.marginPercentage }));

    // 5. Employee productivity % (Attendance vs Sales actions)
    // For simplicity, productivity = sum(sales entries) / sum(attendance checkins) over last 30 days
    const salesCount = await Sales.countDocuments({ branchId, createdAt: { $gte: thirtyDaysAgo } });
    const attendanceCount = await Attendance.countDocuments({ branchId, checkInTime: { $gte: thirtyDaysAgo } });
    const productivityPercentage = attendanceCount > 0 ? ((salesCount / attendanceCount) * 100).toFixed(2) : 0;

    // 6. Incoming / Outgoing Transfers
    const incomingRecs = await StockTransferRecommendation.countDocuments({ toBranchId: branchId, status: 'PENDING' });
    const incomingReqs = await StockTransferRequest.countDocuments({ requestedByBranchId: branchId, status: 'PENDING' });
    const outgoingRecs = await StockTransferRecommendation.countDocuments({ fromBranchId: branchId, status: 'PENDING' });
    const outgoingReqs = await StockTransferRequest.countDocuments({ targetBranchId: branchId, status: 'PENDING' });

    // 7. Transfer History
    const historyRecs = await StockTransferRecommendation.find({
        $or: [{ fromBranchId: branchId }, { toBranchId: branchId }], status: { $ne: 'PENDING' }
    }).populate('itemId fromBranchId toBranchId').sort({ updatedAt: -1 }).limit(5);

    const historyReqs = await StockTransferRequest.find({
        $or: [{ requestedByBranchId: branchId }, { targetBranchId: branchId }], status: { $ne: 'PENDING' }
    }).populate('itemId requestedByBranchId targetBranchId').sort({ updatedAt: -1 }).limit(5);

    const transferHistory = [...historyRecs, ...historyReqs].sort((a, b) => b.updatedAt - a.updatedAt).slice(0, 5);

    // 8. Low Stock
    const lowStockItems = [];
    for (const inv of allBranchInventories) {
        const salesData = await Sales.aggregate([
            { $match: { branchId, itemId: inv.itemId._id, createdAt: { $gte: sevenDaysAgo } } },
            { $group: { _id: null, totalSold: { $sum: "$quantitySold" } } }
        ]);
        const avgDailySales = salesData.length > 0 ? (salesData[0].totalSold / 7) : 0;
        let daysRemaining = avgDailySales > 0 ? inv.quantity / avgDailySales : (inv.quantity === 0 ? 0 : Infinity);
        if (daysRemaining < 3) {
            lowStockItems.push({ item: inv.itemId.name, stock: inv.quantity, daysRemaining: daysRemaining === Infinity ? null : daysRemaining.toFixed(1) });
        }
    }

    const payload = {
        profitability: { revenue: financials.revenue, profitPercentage: financials.profitPercentage.toFixed(2) },
        revenueTrend,
        forecast,
        lowMarginItems,
        productivityPercentage,
        incomingTransfers: incomingRecs + incomingReqs,
        outgoingTransfers: outgoingRecs + outgoingReqs,
        transferHistory,
        lowStockItems
    };

    // Save to Cache
    dashboardCache.manager[branchId] = { timestamp: Date.now(), data: payload };

    res.status(200).json({ success: true, data: payload });
});

// @desc    Get Admin Dashboard Data
// @route   GET /api/dashboard/admin
// @access  Private (Admin)
const getAdminDashboard = asyncHandler(async (req, res) => {
    // Check Cache
    const cachedData = dashboardCache.admin['global'];
    if (cachedData && (Date.now() - cachedData.timestamp < CACHE_TTL_MS)) {
        return res.status(200).json({ success: true, cached: true, data: cachedData.data });
    }

    const thirtyDaysAgo = new Date();
    thirtyDaysAgo.setDate(thirtyDaysAgo.getDate() - 30);

    // 1. Global Profit & Loss (sum of all branches for last 30 days)
    const branches = await Branch.find({});
    let globalRevenue = 0;
    let globalCogs = 0;
    const branchStats = [];

    for (const b of branches) {
        const fin = await ProfitLossService.calculateBranchFinancials(b._id, thirtyDaysAgo, new Date(), 'MONTHLY');
        globalRevenue += fin.revenue;
        globalCogs += fin.cogs;
        branchStats.push({ branchName: b.name, revenue: fin.revenue, profitPercentage: fin.profitPercentage });
    }

    const globalGrossProfit = globalRevenue - globalCogs;
    const globalProfitPercentage = globalRevenue > 0 ? (globalGrossProfit / globalRevenue) * 100 : 0;

    // Top / Least Profitable Branches
    branchStats.sort((a, b) => b.profitPercentage - a.profitPercentage);
    const topProfitableBranch = branchStats.length > 0 ? branchStats[0] : null;
    const leastProfitableBranch = branchStats.length > 0 ? branchStats[branchStats.length - 1] : null;

    // 2. Most Sold Item Globally
    const mostSold = await Sales.aggregate([
        { $match: { createdAt: { $gte: thirtyDaysAgo } } },
        { $group: { _id: "$itemId", totalQty: { $sum: "$quantitySold" } } },
        { $sort: { totalQty: -1 } },
        { $limit: 1 }
    ]);
    let mostSoldItemName = "None";
    if (mostSold.length > 0) {
        const itm = await Item.findById(mostSold[0]._id);
        if (itm) mostSoldItemName = itm.name;
    }

    // 3. Low Margin Alert
    const lowMarginAlerts = await Item.find({ marginPercentage: { $lt: 20 } }).select('name marginPercentage');

    // 4. Attendance Performance (Total checkins vs Branches)
    const totalCheckins = await Attendance.countDocuments({ checkInTime: { $gte: thirtyDaysAgo } });

    // 5. Transfer Analytics
    const pendingRecs = await StockTransferRecommendation.countDocuments({ status: 'PENDING' });
    const pendingReqs = await StockTransferRequest.countDocuments({ status: 'PENDING' });

    const payload = {
        financials: {
            globalRevenue,
            globalProfitPercentage: globalProfitPercentage.toFixed(2),
        },
        branchComparisons: {
            topProfitableBranch,
            leastProfitableBranch,
            allBranches: branchStats
        },
        mostSoldItem: mostSoldItemName,
        lowMarginAlerts,
        attendancePerformance: { totalCheckinsLast30Days: totalCheckins },
        transferAnalytics: { totalPendingTransfers: pendingRecs + pendingReqs }
    };

    // Save to Cache
    dashboardCache.admin['global'] = { timestamp: Date.now(), data: payload };

    res.status(200).json({ success: true, data: payload });
});

module.exports = {
    getManagerDashboard,
    getAdminDashboard
};
