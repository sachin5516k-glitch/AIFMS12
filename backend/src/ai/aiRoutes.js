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

        // 3. Fraud Probability (Avg of recent fraud scores)
        // Aggregation to get average fraud score of last 50 sales
        const fraudStats = await Sales.aggregate([
            { $match: { outletId } },
            { $sort: { createdAt: -1 } },
            { $limit: 100 },
            { $group: { _id: null, avgFraud: { $avg: "$fraudScore" } } }
        ]);

        const fraudProbability = fraudStats.length > 0 ? Math.round(fraudStats[0].avgFraud) : 0;

        res.json({
            healthScore,
            fraudProbability,
            failureRisk: riskLevel,
            topFactors: factors
        });

    } catch (error) {
        res.status(500).json({ message: error.message });
    }
});

module.exports = router;
