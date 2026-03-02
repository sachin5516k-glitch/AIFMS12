const mongoose = require('mongoose');

const branchSchema = mongoose.Schema(
    {
        name: {
            type: String,
            required: true,
        },
        location: {
            address: { type: String },
            city: { type: String },
            state: { type: String },
            lat: { type: Number, required: true },
            lng: { type: Number, required: true }
        },
        createdBy: {
            type: mongoose.Schema.Types.ObjectId,
            ref: 'User',
            required: true,
        },
        status: {
            type: String,
            enum: ['active', 'inactive'],
            default: 'active',
        },
    },
    {
        timestamps: true,
    }
);

module.exports = mongoose.model('Branch', branchSchema);
