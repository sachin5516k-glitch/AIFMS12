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

// @desc    Get branches
// @route   GET /api/branches
// @access  Private
const getBranches = asyncHandler(async (req, res) => {
    let query = {};
    if (req.user.role !== 'admin') {
        query = { _id: req.user.branchId };
    }

    const branches = await Branch.find(query);

    res.status(200).json({
        success: true,
        message: 'Branches retrieved successfully',
        data: branches,
    });
});

module.exports = {
    createBranch,
    getBranches,
};
