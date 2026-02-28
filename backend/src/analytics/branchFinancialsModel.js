const mongoose = require('mongoose');

const branchFinancialsSchema = new mongoose.Schema({
    branchId: {
        type: mongoose.Schema.Types.ObjectId,
        ref: 'Branch',
        required: true,
        index: true
    },
    date: {
        type: Date,
        required: true,
        index: true
    },
    revenue: {
        type: Number,
        required: true,
        default: 0
    },
    cogs: {
        type: Number,
        required: true,
        default: 0
    },
    grossProfit: {
        type: Number,
        required: true,
        default: 0
    },
    profitPercentage: {
        type: Number,
        required: true,
        default: 0
    },
    period: {
        type: String,
        enum: ['DAILY', 'MONTHLY'],
        required: true
    }
}, {
    timestamps: true
});

// Compound index for quick lookups by branch and date
branchFinancialsSchema.index({ branchId: 1, date: 1, period: 1 }, { unique: true });

module.exports = mongoose.model('BranchFinancials', branchFinancialsSchema);
