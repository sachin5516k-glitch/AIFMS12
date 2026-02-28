const mongoose = require('mongoose');

const itemSchema = mongoose.Schema(
    {
        name: {
            type: String,
            required: true,
            unique: true
        },
        category: {
            type: String,
            required: true
        },
        unitPrice: {
            type: Number,
            required: true,
            min: 0
        },
        purchaseCost: {
            type: Number,
            required: true,
            min: 0,
            default: 0
        },
        sellingPrice: {
            type: Number,
            required: true,
            min: 0,
            default: 0
        },
        marginPercentage: {
            type: Number,
            default: 0
        },
        createdBy: {
            type: mongoose.Schema.Types.ObjectId,
            ref: 'User',
            required: true
        }
    },
    {
        timestamps: true
    }
);

module.exports = mongoose.model('Item', itemSchema);
