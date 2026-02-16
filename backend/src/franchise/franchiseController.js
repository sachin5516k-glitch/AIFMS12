const asyncHandler = require('express-async-handler');
const Franchise = require('./franchiseModel');

// @desc    Create new franchise
// @route   POST /api/franchise
// @access  Private/Owner
const createFranchise = asyncHandler(async (req, res) => {
    const { name, ownerEmail, location, royaltyPercentage } = req.body;

    const franchise = await Franchise.create({
        name,
        ownerEmail,
        location,
        royaltyPercentage,
    });

    res.status(201).json(franchise);
});

// @desc    Get all franchises
// @route   GET /api/franchise
// @access  Private
const getFranchises = asyncHandler(async (req, res) => {
    const franchises = await Franchise.find({});
    res.json(franchises);
});

module.exports = { createFranchise, getFranchises };
