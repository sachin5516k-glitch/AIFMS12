const asyncHandler = require('express-async-handler');
const Sales = require('./salesModel');
const Item = require('../item/itemModel');
const Inventory = require('../inventory/inventoryModel');

const submitSales = asyncHandler(async (req, res) => {
    const branchId = req.user.role === 'admin' ? req.body.branchId : req.user.branchId;
    const { itemId, quantitySold, paymentMode } = req.body;

    if (!branchId || !itemId || !quantitySold || !paymentMode) {
        res.status(400);
        throw new Error('Please add all fields');
    }

    const item = await Item.findById(itemId);
    if (!item) {
        res.status(404);
        throw new Error('Item not found');
    }

    const totalAmount = item.unitPrice * quantitySold;

    // Deduct inventory
    let inventory = await Inventory.findOne({ branchId, itemId });
    if (!inventory || inventory.quantity < quantitySold) {
        res.status(400);
        throw new Error('Insufficient stock for this item');
    }

    inventory.quantity -= quantitySold;
    inventory.lastUpdated = new Date();
    await inventory.save();

    const sales = await Sales.create({
        branchId,
        itemId,
        quantitySold,
        totalAmount,
        paymentMode,
        createdBy: req.user._id,
    });

    res.status(201).json({
        success: true,
        message: 'Sales submitted successfully, inventory deducted',
        data: sales
    });
});

const getSales = asyncHandler(async (req, res) => {
    const branchId = req.user.role === 'admin' ? (req.query.branchId || null) : req.user.branchId;
    let query = {};
    if (branchId) query.branchId = branchId;

    const sales = await Sales.find(query).populate('itemId', 'name category unitPrice');

    res.json({
        success: true,
        message: 'Sales retrieved successfully',
        data: sales
    });
});

module.exports = { submitSales, getSales };
