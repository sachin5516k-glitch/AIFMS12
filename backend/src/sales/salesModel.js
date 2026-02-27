const mongoose = require('mongoose');

const salesSchema = mongoose.Schema(
    {
        branchId: {
            type: mongoose.Schema.Types.ObjectId,
            ref: 'Branch',
            required: [true, 'Branch ID is required'],
            index: true
        },
        itemId: {
            type: mongoose.Schema.Types.ObjectId,
            ref: 'Item',
            required: [true, 'Item ID is required']
        },
        quantitySold: {
            type: Number,
            required: [true, 'Quantity is required'],
            min: [1, 'Quantity must be at least 1']
        },
        totalAmount: {
            type: Number,
            required: [true, 'Total amount is required'],
            min: [0, 'Amount must be valid']
        },
        paymentMode: {
            type: String,
            enum: ['Cash', 'Card', 'UPI'],
            required: [true, 'Payment mode is required'],
        },
        createdBy: {
            type: mongoose.Schema.Types.ObjectId,
            ref: 'User',
            required: true
        }
    },
    {
        timestamps: true,
    }
);

salesSchema.index({ branchId: 1, createdAt: -1 });

module.exports = mongoose.model('Sales', salesSchema);
