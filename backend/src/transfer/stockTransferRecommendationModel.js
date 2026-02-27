const mongoose = require('mongoose');

const stockTransferRecommendationSchema = new mongoose.Schema({
    itemId: {
        type: mongoose.Schema.Types.ObjectId,
        ref: 'Item',
        required: true
    },
    fromBranchId: {
        type: mongoose.Schema.Types.ObjectId,
        ref: 'Branch',
        required: true
    },
    toBranchId: {
        type: mongoose.Schema.Types.ObjectId,
        ref: 'Branch',
        required: true
    },
    suggestedQuantity: {
        type: Number,
        required: true,
        min: 1
    },
    reason: {
        type: String,
        required: true
    },
    urgencyLevel: {
        type: String,
        enum: ['LOW', 'MEDIUM', 'HIGH', 'CRITICAL'],
        default: 'MEDIUM'
    },
    status: {
        type: String,
        enum: ['PENDING', 'APPROVED', 'REJECTED'],
        default: 'PENDING'
    }
}, {
    timestamps: true
});

// Index to prevent duplicate pending recommendations for the same item between same branches
stockTransferRecommendationSchema.index({ itemId: 1, fromBranchId: 1, toBranchId: 1, status: 1 });

module.exports = mongoose.model('StockTransferRecommendation', stockTransferRecommendationSchema);
