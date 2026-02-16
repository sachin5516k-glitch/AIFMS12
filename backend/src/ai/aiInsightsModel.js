const mongoose = require('mongoose');

const aiInsightsSchema = mongoose.Schema(
    {
        outletId: {
            type: String,
            required: true,
            index: true
        },
        healthScore: {
            type: Number,
            min: 0,
            max: 100,
            required: true
        },
        fraudProbability: {
            type: Number,
            min: 0,
            max: 100,
            required: true
        },
        failureRisk: {
            type: String,
            enum: ['LOW', 'MEDIUM', 'HIGH'],
            required: true
        },
        topFactors: [String],
        generatedAt: {
            type: Date,
            default: Date.now
        }
    },
    {
        timestamps: true
    }
);

aiInsightsSchema.index({ outletId: 1, generatedAt: -1 });

module.exports = mongoose.model('AiInsights', aiInsightsSchema);
