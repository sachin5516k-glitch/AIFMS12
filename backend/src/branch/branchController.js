const asyncHandler = require('express-async-handler');
const Branch = require('./branchModel');

// @desc    Create a new branch
// @route   POST /api/branches
// @access  Private/Admin
const createBranch = asyncHandler(async (req, res) => {
    const { name, location } = req.body;

    const branch = await Branch.create({
        name,
        location,
        createdBy: req.user._id,
    });

    res.status(201).json({
        success: true,
        message: 'Branch created successfully',
        data: branch,
    });
});

const Inventory = require('../inventory/inventoryModel');
const Sales = require('../sales/salesModel');
const User = require('../auth/userModel');

// @desc    Get branches
// @route   GET /api/branches
// @access  Private
const getBranches = asyncHandler(async (req, res) => {
    let query = {};
    if (req.user.role !== 'admin') {
        query = { _id: req.user.branchId };
    }

    const branchesRaw = await Branch.find(query).lean();

    const sevenDaysAgo = new Date();
    sevenDaysAgo.setDate(sevenDaysAgo.getDate() - 7);

    // Compute health status dynamically for each branch
    const branchesWithHealth = await Promise.all(branchesRaw.map(async (branch) => {
        const inventories = await Inventory.find({ branchId: branch._id });
        let lowestDaysRemaining = Infinity;

        for (const inv of inventories) {
            const salesData = await Sales.aggregate([
                {
                    $match: {
                        branchId: branch._id,
                        itemId: inv.itemId,
                        createdAt: { $gte: sevenDaysAgo }
                    }
                },
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

            if (daysRemaining < lowestDaysRemaining) {
                lowestDaysRemaining = daysRemaining;
            }
        }

        // Green = Healthy (>10 days)
        // Yellow = Warning (between 3 and 10 days)
        // Red = Critical (<3 days or 0 stock)
        let healthColor = 'Green';
        if (lowestDaysRemaining < 3) healthColor = 'Red';
        else if (lowestDaysRemaining <= 10) healthColor = 'Yellow';
        // If they have no items at all, default to Green or maybe Grey. Let's say Green.
        if (inventories.length === 0) healthColor = 'Green';

        return {
            ...branch,
            healthStatus: healthColor,
            lowestDaysRemaining: lowestDaysRemaining === Infinity ? null : lowestDaysRemaining
        };
    }));

    res.status(200).json({
        success: true,
        message: 'Branches retrieved successfully',
        data: branchesWithHealth,
    });
});

// @desc    Get single branch details with stats
// @route   GET /api/branches/:id
// @access  Private
const getBranchById = asyncHandler(async (req, res) => {
    const branch = await Branch.findById(req.params.id).lean();
    if (!branch) {
        res.status(404);
        throw new Error('Branch not found');
    }

    // Manager
    const manager = await User.findOne({ branchId: branch._id, role: 'manager' }).select('name');

    // Staff Count
    const staffCount = await User.countDocuments({ branchId: branch._id, role: 'employee' });

    // Total Sales
    const salesData = await Sales.aggregate([
        { $match: { branchId: branch._id } },
        { $group: { _id: null, totalRevenue: { $sum: "$totalAmount" } } }
    ]);
    const totalSales = salesData.length > 0 ? salesData[0].totalRevenue : 0;

    // Health
    const inventories = await Inventory.find({ branchId: branch._id });
    let isHealthy = "Good";
    const lowStockItems = inventories.filter(inv => inv.quantity < 10);
    if (lowStockItems.length > 0) {
        isHealthy = `Warning (${lowStockItems.length} items low)`;
    }
    if (inventories.length === 0) {
        isHealthy = "No Stock";
    }

    res.status(200).json({
        success: true,
        data: {
            ...branch,
            managerName: manager ? manager.name : 'Unassigned',
            staffCount,
            totalSales,
            stockHealth: isHealthy
        }
    });
});

module.exports = {
    createBranch,
    getBranches,
    getBranchById
};
