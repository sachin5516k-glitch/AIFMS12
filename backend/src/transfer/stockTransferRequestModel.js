const mongoose = require('mongoose');

const stockTransferRequestSchema = new mongoose.Schema({
    requestedByBranchId: {
        type: mongoose.Schema.Types.ObjectId,
        ref: 'Branch',
        required: true
    },
    targetBranchId: {
        type: mongoose.Schema.Types.ObjectId,
        ref: 'Branch',
        required: true
    },
    itemId: {
        type: mongoose.Schema.Types.ObjectId,
        ref: 'Item',
        required: true
    },
    quantity: {
        type: Number,
        required: true,
        min: 1
    },
    status: {
        type: String,
        enum: ['PENDING', 'APPROVED', 'REJECTED'],
        default: 'PENDING'
    }
}, {
    timestamps: true
});

// Index to find requests associated with a specific branch efficiently
stockTransferRequestSchema.index({ requestedByBranchId: 1 });
stockTransferRequestSchema.index({ targetBranchId: 1 });
stockTransferRequestSchema.index({ status: 1 });

module.exports = mongoose.model('StockTransferRequest', stockTransferRequestSchema);
