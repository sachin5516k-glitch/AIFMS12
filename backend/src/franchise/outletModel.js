const mongoose = require('mongoose');

const outletSchema = mongoose.Schema(
    {
        franchiseId: {
            type: mongoose.Schema.Types.ObjectId,
            ref: 'Franchise',
            required: true,
        },
        name: {
            type: String,
            required: true,
        },
        location: {
            type: {
                type: String,
                enum: ['Point'],
                required: true,
                default: 'Point',
            },
            coordinates: {
                type: [Number], // [longitude, latitude]
                required: true,
            },
        },
        radius: {
            type: Number, // Method allowed radius in meters
            default: 100,
        },
        status: {
            type: String,
            enum: ['active', 'inactive', 'suspended'],
            default: 'active',
        },
    },
    {
        timestamps: true,
    }
);

// Geo-Spatial Index
outletSchema.index({ location: '2dsphere' });
outletSchema.index({ franchiseId: 1 });

module.exports = mongoose.model('Outlet', outletSchema);
