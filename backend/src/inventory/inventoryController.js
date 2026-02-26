const asyncHandler = require('express-async-handler');
const Inventory = require('./inventoryModel');

// @desc    Submit inventory
// @route   POST /api/inventory/submit
// @access  Private
const submitInventory = asyncHandler(async (req, res) => {
    const { outletId, items } = req.body;

    if (!items || items.length === 0) {
        res.status(400);
        throw new Error('No items to submit');
    }

    // Calculate Variance
    const processedItems = items.map((item) => {
        // Variance = Opening - Closing (Sales logic to be added in Part 2)
        // For now: Simple diff
        const variance = item.opening - item.closing;
        return { ...item, variance };
    });

    const inventory = await Inventory.create({
        outletId,
        items: processedItems,
    });

    res.status(201).json({
        success: true,
        message: 'Inventory submitted successfully',
        data: inventory
    });
});

// @desc    Get Inventory Items (Mock for now)
// @route   GET /api/inventory/items
const getInventoryItems = asyncHandler(async (req, res) => {
    // In real app, fetch from Product Catalog
    const mockItems = [
        { id: "item_001", name: "Burger Bun", lastStock: 50 },
        { id: "item_002", name: "Cheese Slice", lastStock: 100 },
        { id: "item_003", name: "Patty", lastStock: 45 }
    ];
    res.json({
        success: true,
        message: 'Items retrieved successfully',
        data: mockItems
    });
});

module.exports = { submitInventory, getInventoryItems };
