const asyncHandler = require('express-async-handler');
const Inventory = require('./inventoryModel');
const Sales = require('../sales/salesModel');
const StockTransferRequest = require('../transfer/stockTransferRequestModel');
const StockTransferRecommendation = require('../transfer/stockTransferRecommendationModel');
const AuditLog = require('../audit/auditLogModel');

const submitInventory = asyncHandler(async (req, res) => {
    const branchId = req.user.role === 'admin' ? req.body.branchId : req.user.branchId;
    const { items } = req.body; // array of { itemId, quantityAdded }

    if (!branchId || !items || items.length === 0) {
        res.status(400);
        throw new Error('Please provide branchId and items');
    }

    const updatedItems = [];
    let auditDetails = `Inventory change at branch ${branchId}: `;

    for (const item of items) {
        let inv = await Inventory.findOne({ branchId, itemId: item.itemId });
        if (!inv) {
            // Create new inventory record if it doesnt exist
            inv = await Inventory.create({
                branchId,
                itemId: item.itemId,
                quantity: item.quantityAdded
            });
            auditDetails += `[New: ${item.itemId} +${item.quantityAdded}] `;
        } else {
            inv.quantity += item.quantityAdded;
            inv.lastUpdated = new Date();
            await inv.save();
            auditDetails += `[Add: ${item.itemId} +${item.quantityAdded}] `;
        }
        updatedItems.push(inv);
    }

    await AuditLog.create({
        userId: req.user._id,
        action: 'INVENTORY_CHANGE',
        details: auditDetails,
        ipAddress: req.ip,
        userAgent: req.headers['user-agent']
    });

    res.status(201).json({
        success: true,
        message: 'Inventory updated successfully',
        data: updatedItems
    });
});

const getInventoryItems = asyncHandler(async (req, res) => {
    const branchId = req.user.role === 'admin' ? (req.query.branchId || null) : req.user.branchId;
    let query = {};
    if (branchId) query.branchId = branchId;

    const inventory = await Inventory.find(query).populate('itemId', 'name category unitPrice');

    const today = new Date();
    today.setHours(0, 0, 0, 0);

    const resultItems = await Promise.all(inventory.map(async (inv) => {
        if (!inv.itemId) return null;

        // Sales today
        const salesDocs = await Sales.find({ branchId: inv.branchId, itemId: inv.itemId._id, createdAt: { $gte: today } });
        const salesCount = salesDocs.reduce((sum, s) => sum + s.quantitySold, 0);

        // Transfers OUT today
        const reqsOut = await StockTransferRequest.find({ targetBranchId: inv.branchId, itemId: inv.itemId._id, status: 'APPROVED', updatedAt: { $gte: today } });
        const recsOut = await StockTransferRecommendation.find({ fromBranchId: inv.branchId, itemId: inv.itemId._id, status: { $in: ['APPROVED', 'COMPLETED'] }, updatedAt: { $gte: today } });
        const transferOut = reqsOut.reduce((sum, r) => sum + r.quantity, 0) + recsOut.reduce((sum, r) => sum + r.quantity, 0);

        // Transfers IN today
        const reqsIn = await StockTransferRequest.find({ requestedByBranchId: inv.branchId, itemId: inv.itemId._id, status: 'APPROVED', updatedAt: { $gte: today } });
        const recsIn = await StockTransferRecommendation.find({ toBranchId: inv.branchId, itemId: inv.itemId._id, status: { $in: ['APPROVED', 'COMPLETED'] }, updatedAt: { $gte: today } });
        const transferIn = reqsIn.reduce((sum, r) => sum + r.quantity, 0) + recsIn.reduce((sum, r) => sum + r.quantity, 0);

        const closingStock = inv.quantity;
        const openingStock = closingStock + salesCount + transferOut - transferIn;

        return {
            _id: inv._id,
            itemId: inv.itemId._id.toString(),
            itemName: inv.itemId.name,
            name: inv.itemId.name,
            openingStock: openingStock,
            sales: salesCount,
            transferOut: transferOut,
            transferIn: transferIn,
            closingStock: closingStock,
            lastStock: closingStock // fallback for old android ver
        };
    }));

    res.json({
        success: true,
        message: 'Inventory retrieved successfully',
        data: resultItems.filter(Boolean)
    });
});

module.exports = { submitInventory, getInventoryItems };
