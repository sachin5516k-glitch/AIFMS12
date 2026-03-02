const asyncHandler = require('express-async-handler');
const Item = require('./itemModel');
const Branch = require('../branch/branchModel');
const Inventory = require('../inventory/inventoryModel');

// @desc    Create a new item
// @route   POST /api/items
// @access  Private/Admin
const createItem = asyncHandler(async (req, res) => {
    const { name, category, unitPrice } = req.body;

    const itemExists = await Item.findOne({ name });
    if (itemExists) {
        res.status(400);
        throw new Error('Item already exists');
    }

    const item = await Item.create({
        name,
        category,
        unitPrice,
        createdBy: req.user._id,
    });

    // Auto-create inventory entries for ALL existing branches
    const branches = await Branch.find({});
    const inventoryOps = branches.map(branch => ({
        insertOne: {
            document: { branchId: branch._id, itemId: item._id, quantity: 0 }
        }
    }));
    if (inventoryOps.length > 0) {
        await Inventory.bulkWrite(inventoryOps, { ordered: false }).catch(() => { });
    }

    res.status(201).json({
        success: true,
        message: 'Item created successfully',
        data: item,
    });
});

// @desc    Get all items
// @route   GET /api/items
// @access  Private (All roles)
const getItems = asyncHandler(async (req, res) => {
    const items = await Item.find({});

    res.status(200).json({
        success: true,
        message: 'Items retrieved successfully',
        data: items,
    });
});

module.exports = {
    createItem,
    getItems,
};
