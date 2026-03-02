const express = require('express');
const router = express.Router();
const { predictFailureRisk } = require('./failure.engine');
const { calculateHealthScore } = require('./health.engine');
const Sales = require('../sales/salesModel');
const { protect } = require('../middleware/authMiddleware');

// @desc    Get AI Insights
// @route   GET /api/ai/insights/:outletId
// @access  Private
router.get('/insights/:outletId', protect, async (req, res) => {
    try {
        const { outletId } = req.params;

        // 1. Health Score
        const { healthScore } = await calculateHealthScore(outletId);

        // 2. Failure Risk
        const { riskLevel, factors } = await predictFailureRisk(outletId);

        // 3. Fraud Probability
        const matchStage = outletId === 'global' ? {} : { branchId: outletId };
        const fraudStats = await Sales.aggregate([
            { $match: matchStage },
            { $sort: { createdAt: -1 } },
            { $limit: 100 },
            { $group: { _id: null, avgFraud: { $avg: "$fraudScore" } } }
        ]);

        let fraudProbability = fraudStats.length > 0 ? Math.round(fraudStats[0].avgFraud) : 0;

        // Apply dynamic risk multipliers based on Health Engine factors
        if (factors.some(f => f.includes('CRITICAL: Sales registered but no staff attendance'))) {
            fraudProbability = Math.min(100, fraudProbability + 40);
        }
        if (factors.some(f => f.includes('Unusual spike in sales'))) {
            fraudProbability = Math.min(100, fraudProbability + 20);
        }

        res.json({
            success: true,
            data: {
                healthScore,
                fraudProbability,
                failureRisk: riskLevel,
                topFactors: factors
            }
        });

    } catch (error) {
        res.status(500).json({ message: error.message });
    }
});

// @desc    Get Item Sales Chart Data (Last 7 Days)
// @route   GET /api/analytics/item-sales
// @access  Private
router.get('/analytics/item-sales', protect, async (req, res) => {
    try {
        const branchId = req.user.role === 'manager' || req.user.role === 'employee'
            ? req.user.branchId
            : null;

        const dateOffset = new Date();
        dateOffset.setDate(dateOffset.getDate() - 7);

        const matchStage = { createdAt: { $gte: dateOffset } };
        if (branchId) matchStage.branchId = branchId;

        const salesAgg = await Sales.aggregate([
            { $match: matchStage },
            {
                $group: {
                    _id: "$itemId",
                    totalQuantity: { $sum: "$quantitySold" }
                }
            },
            {
                $lookup: {
                    from: "items",
                    localField: "_id",
                    foreignField: "_id",
                    as: "itemDetails"
                }
            },
            { $unwind: "$itemDetails" },
            {
                $project: {
                    _id: 0,
                    itemName: "$itemDetails.name",
                    totalSales: "$totalQuantity"
                }
            },
            { $sort: { totalSales: -1 } }
        ]);

        res.status(200).json({
            success: true,
            data: salesAgg
        });

    } catch (error) {
        res.status(500).json({
            success: false,
            message: 'Failed to fetch item sales analytics'
        });
    }
});

module.exports = router;
