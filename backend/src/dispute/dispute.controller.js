const asyncHandler = require('express-async-handler');

// @desc    Raise Dispute
// @route   POST /api/dispute
// @access  Private
const raiseDispute = asyncHandler(async (req, res) => {
    // Mock Logic
    res.status(201).json({ message: "Dispute raised successfully", status: "pending" });
});

module.exports = { raiseDispute };
