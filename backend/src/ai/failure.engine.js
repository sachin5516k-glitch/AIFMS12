const { calculateHealthScore } = require('./health.engine');

/**
 * Predicts failure risk.
 * @param {String} outletId
 * @returns {Object} { riskLevel: String, reason: String }
 */
const predictFailureRisk = async (outletId) => {
    const { healthScore, factors } = await calculateHealthScore(outletId);

    let riskLevel = 'LOW';
    let reason = 'Healthy operations';

    if (healthScore < 40) {
        riskLevel = 'HIGH';
        reason = 'Critical health score due to: ' + factors.join(', ');
    } else if (healthScore < 70) {
        riskLevel = 'MEDIUM';
        reason = 'Declining health score due to: ' + factors.join(', ');
    }

    return { riskLevel, reason, factors };
};

module.exports = { predictFailureRisk };
