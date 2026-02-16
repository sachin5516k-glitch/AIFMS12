const Sales = require('../sales/salesModel');
const Inventory = require('../inventory/inventoryModel');

/**
 * Calculates fraud score for a given sales transaction.
 * @param {Object} salesData - { outletId, amount, paymentMode, date }
 * @returns {Object} { score: Number, reasons: Array }
 */
const calculateFraudScore = async (salesData) => {
    let score = 0;
    const reasons = [];
    const { outletId, amount, paymentMode } = salesData;

    // 1. High Value Transaction Check
    // Threshold should be dynamic based on outlet history, hardcoded for now
    if (amount > 100000) {
        score += 40;
        reasons.push('Unusually high transaction amount');
    } else if (amount > 50000) {
        score += 20;
        reasons.push('High transaction amount');
    }

    // 2. Fetch recent sales for deviation analysis
    const recentSales = await Sales.find({
        outletId,
        createdAt: { $gt: new Date(Date.now() - 7 * 24 * 60 * 60 * 1000) } // Last 7 days
    }).limit(50).lean();

    if (recentSales.length > 5) {
        const total = recentSales.reduce((sum, s) => sum + s.amount, 0);
        const avg = total / recentSales.length;

        if (amount > avg * 3) {
            score += 30;
            reasons.push('Amount exceeds 3x weekly average');
        } else if (amount > avg * 2) {
            score += 15;
            reasons.push('Amount exceeds 2x weekly average');
        }
    }

    // 3. Payment Mode Risk (e.g., massive cash dump)
    if (paymentMode === 'Cash' && amount > 20000) {
        score += 10;
        reasons.push('Large cash transaction');
    }

    // Cap score at 100
    score = Math.min(score, 100);

    return { score, reasons };
};

module.exports = { calculateFraudScore };
