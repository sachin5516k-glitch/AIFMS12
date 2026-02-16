const mongoose = require('mongoose');

const salesSchema = mongoose.Schema(
    {
        outletId: {
            type: String,
            required: [true, 'Outlet ID is required'],
            index: true
        },
        amount: {
            type: Number,
            required: [true, 'Sales amount is required'],
            min: [1, 'Amount must be greater than 0']
        },
        date: {
            type: Date,
            default: Date.now,
        },
        paymentMode: {
            type: String,
            enum: ['Cash', 'Card', 'UPI'],
            required: [true, 'Payment mode is required'],
        },
        imageUrl: {
            type: String,
            required: false
        },
        fraudScore: {
            type: Number,
            default: 0,
            min: 0,
            max: 100
        },
        status: {
            type: String,
            enum: ['pending', 'approved', 'flagged'],
            default: 'pending',
        },
    },
    {
        timestamps: true,
    }
);

// Compound Index: Optimizes queries by Outlet Date and prevents duplicate scenarios if unique was enforced (here just for perf)
salesSchema.index({ outletId: 1, date: -1 });
salesSchema.index({ fraudScore: -1 }); // Fast lookup for high fraud

module.exports = mongoose.model('Sales', salesSchema);
