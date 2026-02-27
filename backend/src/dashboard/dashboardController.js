const asyncHandler = require('express-async-handler');
const Inventory = require('../inventory/inventoryModel');
const Sales = require('../sales/salesModel');
const Branch = require('../branch/branchModel');
const StockTransferRecommendation = require('../transfer/stockTransferRecommendationModel');
const StockTransferRequest = require('../transfer/stockTransferRequestModel');

// @desc    Get Manager Dashboard Data
// @route   GET /api/dashboard/manager
// @access  Private (Manager)
const getManagerDashboard = asyncHandler(async (req, res) => {
    const branchId = req.user.branchId;
    if (!branchId) {
        return res.status(400).json({ success: false, message: 'User does not belong to a branch' });
    }

    const sevenDaysAgo = new Date();
    sevenDaysAgo.setDate(sevenDaysAgo.getDate() - 7);

    // 1. Items Low in Stock (using similar logic to AI analysis)
    const inventories = await Inventory.find({ branchId }).populate('itemId');
    const lowStockItems = [];
    for (const inv of inventories) {
        const salesData = await Sales.aggregate([
            { $match: { branchId: branchId, itemId: inv.itemId._id, createdAt: { $gte: sevenDaysAgo } } },
            { $group: { _id: null, totalSold: { $sum: "$quantitySold" } } }
        ]);
        const totalSales7Days = salesData.length > 0 ? salesData[0].totalSold : 0;
        const avgDailySales = totalSales7Days / 7;
        let daysRemaining = Infinity;
        if (avgDailySales > 0) {
            daysRemaining = inv.quantity / avgDailySales;
        } else if (inv.quantity === 0) {
            daysRemaining = 0;
        }

        if (daysRemaining < 3) {
            lowStockItems.push({
                item: inv.itemId.name,
                currentStock: inv.quantity,
                daysRemaining: daysRemaining === Infinity ? null : daysRemaining.toFixed(1)
            });
        }
    }

    // 2. Incoming and Outgoing Transfers (PENDING)
    const incomingRecs = await StockTransferRecommendation.countDocuments({ toBranchId: branchId, status: 'PENDING' });
    const incomingReqs = await StockTransferRequest.countDocuments({ requestedByBranchId: branchId, status: 'PENDING' });
    const incomingTransfers = incomingRecs + incomingReqs;

    const outgoingRecs = await StockTransferRecommendation.countDocuments({ fromBranchId: branchId, status: 'PENDING' });
    const outgoingReqs = await StockTransferRequest.countDocuments({ targetBranchId: branchId, status: 'PENDING' });
    const outgoingTransfers = outgoingRecs + outgoingReqs;

    // 3. Transfer History 
    const historyRecs = await StockTransferRecommendation.find({
        $or: [{ fromBranchId: branchId }, { toBranchId: branchId }],
        status: { $ne: 'PENDING' }
    }).populate('itemId').populate('fromBranchId').populate('toBranchId').sort({ updatedAt: -1 }).limit(10);

    const historyReqs = await StockTransferRequest.find({
        $or: [{ requestedByBranchId: branchId }, { targetBranchId: branchId }],
        status: { $ne: 'PENDING' }
    }).populate('itemId').populate('requestedByBranchId').populate('targetBranchId').sort({ updatedAt: -1 }).limit(10);

    // Combine history and sort
    const transferHistory = [...historyRecs, ...historyReqs].sort((a, b) => new Date(b.updatedAt) - new Date(a.updatedAt)).slice(0, 10);

    res.status(200).json({
        success: true,
        data: {
            lowStockItems,
            incomingTransfers,
            outgoingTransfers,
            transferHistory
        }
    });
});

// @desc    Get Admin Dashboard Data
// @route   GET /api/dashboard/admin
// @access  Private (Admin)
const getAdminDashboard = asyncHandler(async (req, res) => {

    // 1. Global Stock Imbalance Chart (Total recommendations currently PENDING vs APPROVED)
    const pendingRecs = await StockTransferRecommendation.countDocuments({ status: 'PENDING' });
    const approvedRecs = await StockTransferRecommendation.countDocuments({ status: 'APPROVED' });
    const rejectedRecs = await StockTransferRecommendation.countDocuments({ status: 'REJECTED' });

    const pendingReqs = await StockTransferRequest.countDocuments({ status: 'PENDING' });
    const approvedReqs = await StockTransferRequest.countDocuments({ status: 'APPROVED' });
    const rejectedReqs = await StockTransferRequest.countDocuments({ status: 'REJECTED' });

    const transferAnalytics = {
        totalPending: pendingRecs + pendingReqs,
        totalApproved: approvedRecs + approvedReqs,
        totalRejected: rejectedRecs + rejectedReqs
    };

    // 2. Most Transferred Item
    const mostTransferredRecs = await StockTransferRecommendation.aggregate([
        { $match: { status: 'APPROVED' } },
        { $group: { _id: "$itemId", totalQuantity: { $sum: "$suggestedQuantity" } } },
        { $sort: { totalQuantity: -1 } },
        { $limit: 1 }
    ]);

    const mostTransferredReqs = await StockTransferRequest.aggregate([
        { $match: { status: 'APPROVED' } },
        { $group: { _id: "$itemId", totalQuantity: { $sum: "$quantity" } } },
        { $sort: { totalQuantity: -1 } },
        { $limit: 1 }
    ]);

    let mostTransferredItemData = null;
    if (mostTransferredRecs.length > 0) mostTransferredItemData = { itemId: mostTransferredRecs[0]._id, qty: mostTransferredRecs[0].totalQuantity };
    if (mostTransferredReqs.length > 0 && (!mostTransferredItemData || mostTransferredReqs[0].totalQuantity > mostTransferredItemData.qty)) {
        mostTransferredItemData = { itemId: mostTransferredReqs[0]._id, qty: mostTransferredReqs[0].totalQuantity };
    }

    let mostTransferredItemName = "None";
    if (mostTransferredItemData) {
        const Item = require('../item/itemModel');
        const itemInfo = await Item.findById(mostTransferredItemData.itemId);
        mostTransferredItemName = itemInfo ? itemInfo.name : "Unknown";
    }

    // 3. Branch Performance Comparison (Sales per branch)
    const branchPerformance = await Sales.aggregate([
        { $group: { _id: "$branchId", totalSalesRevenue: { $sum: "$totalAmount" } } },
        { $lookup: { from: 'branches', localField: '_id', foreignField: '_id', as: 'branch' } },
        { $unwind: "$branch" },
        { $project: { branchName: "$branch.name", totalSalesRevenue: 1 } },
        { $sort: { totalSalesRevenue: -1 } }
    ]);

    res.status(200).json({
        success: true,
        data: {
            transferAnalytics,
            mostTransferredItem: mostTransferredItemName,
            branchPerformance
        }
    });
});

module.exports = {
    getManagerDashboard,
    getAdminDashboard
};
