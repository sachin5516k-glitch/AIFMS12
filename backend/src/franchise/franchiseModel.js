const mongoose = require('mongoose');

const franchiseSchema = mongoose.Schema(
    {
        name: {
            type: String,
            required: [true, 'Franchise name is required'],
            trim: true
        },
        ownerId: {
            type: mongoose.Schema.Types.ObjectId,
            ref: 'User',
            required: [true, 'Owner ID is required']
        },
        ownerEmail: {
            type: String,
            required: [true, 'Owner Email is required']
        },
        region: {
            type: String,
            required: [true, 'Region is required']
        },
        location: {
            type: String,
            required: [true, 'Location is required']
        },
        royaltyPercentage: {
            type: Number,
            required: true,
            min: [0, 'Royalty cannot be negative'],
            max: [100, 'Royalty cannot exceed 100%'],
            default: 5
        },
        status: {
            type: String,
            enum: ['active', 'inactive'],
            default: 'active'
        },
    },
    { timestamps: true }
);

// Indexes for performance
franchiseSchema.index({ ownerId: 1 });
franchiseSchema.index({ region: 1 });

module.exports = mongoose.model('Franchise', franchiseSchema);
