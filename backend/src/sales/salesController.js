const asyncHandler = require('express-async-handler');
const Sales = require('./salesModel');
const { calculateFraudScore } = require('../ai/fraud.engine');

// @desc    Submit daily sales
// @route   POST /api/sales/submit
// @access  Private
const submitSales = asyncHandler(async (req, res) => {
    const { outletId, amount, paymentMode, imageUrl } = req.body;

    if (!outletId || !amount || !paymentMode) {
        res.status(400);
        throw new Error('Please add all fields');
    }

    // Basic Duplicate Check (Same outlet, same amount, within last minute - simplified)
    const existingSale = await Sales.findOne({
        outletId,
        amount,
        createdAt: { $gt: new Date(Date.now() - 60000) }, // 1 min window
    });

    if (existingSale) {
        res.status(400);
        throw new Error('Duplicate sales submission detected');
    }

    // Calculate Fraud Score using AI Engine
    const { score, reasons } = await calculateFraudScore({ outletId, amount, paymentMode });

    const sales = await Sales.create({
        outletId,
        amount,
        paymentMode,
        imageUrl,
        fraudScore: score,
    });

    if (score > 50) {
        console.warn(`High Fraud Score detected for Outlet ${outletId}: ${score}. Reasons: ${reasons.join(', ')}`);
    }

    res.status(201).json(sales);
});

// @desc    Get sales history
// @route   GET /api/sales/:outletId
// @access  Private
const getSales = asyncHandler(async (req, res) => {
    const sales = await Sales.find({ outletId: req.params.outletId });
    res.json(sales);
});

module.exports = { submitSales, getSales };
