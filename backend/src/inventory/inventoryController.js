const asyncHandler = require('express-async-handler');
const Inventory = require('./inventoryModel');

const submitInventory = asyncHandler(async (req, res) => {
    const branchId = req.user.role === 'admin' ? req.body.branchId : req.user.branchId;
    const { items } = req.body; // array of { itemId, quantityAdded }

    if (!branchId || !items || items.length === 0) {
        res.status(400);
        throw new Error('Please provide branchId and items');
    }

    const updatedItems = [];
    for (const item of items) {
        let inv = await Inventory.findOne({ branchId, itemId: item.itemId });
        if (!inv) {
            // Create new inventory record if it doesnt exist
            inv = await Inventory.create({
                branchId,
                itemId: item.itemId,
                quantity: item.quantityAdded
            });
        } else {
            inv.quantity += item.quantityAdded;
            inv.lastUpdated = new Date();
            await inv.save();
        }
        updatedItems.push(inv);
    }

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

    res.json({
        success: true,
        message: 'Inventory retrieved successfully',
        data: inventory
    });
});

module.exports = { submitInventory, getInventoryItems };
