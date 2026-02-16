const mongoose = require('mongoose');

const inventorySchema = mongoose.Schema(
    {
        outletId: {
            type: String,
            required: true,
        },
        items: [
            {
                itemId: String,
                opening: Number,
                closing: Number,
                variance: Number,
            },
        ],
        date: {
            type: Date,
            default: Date.now,
        },
    },
    {
        timestamps: true,
    }
);

module.exports = mongoose.model('Inventory', inventorySchema);
