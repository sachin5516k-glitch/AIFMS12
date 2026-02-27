const mongoose = require('mongoose');

const inventorySchema = mongoose.Schema(
    {
        branchId: {
            type: mongoose.Schema.Types.ObjectId,
            ref: 'Branch',
            required: true,
            index: true
        },
        itemId: {
            type: mongoose.Schema.Types.ObjectId,
            ref: 'Item',
            required: true
        },
        quantity: {
            type: Number,
            required: true,
            default: 0
        },
        lastUpdated: {
            type: Date,
            default: Date.now
        }
    },
    {
        timestamps: true,
    }
);

// Compound index for unique item per branch
inventorySchema.index({ branchId: 1, itemId: 1 }, { unique: true });

module.exports = mongoose.model('Inventory', inventorySchema);
