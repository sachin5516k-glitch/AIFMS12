const mongoose = require('mongoose');

const disputeSchema = mongoose.Schema(
    {
        outletId: {
            type: String,
            required: true,
            index: true
        },
        raisedBy: {
            type: mongoose.Schema.Types.ObjectId,
            ref: 'User',
            required: true
        },
        reason: {
            type: String,
            required: true,
            trim: true
        },
        status: {
            type: String,
            enum: ['pending', 'reviewing', 'resolved', 'rejected'],
            default: 'pending'
        },
        resolution: {
            type: String
        },
        relatedRecordIds: [String] // IDs of Sales or Inventory records
    },
    {
        timestamps: true
    }
);

disputeSchema.index({ outletId: 1, status: 1 });

module.exports = mongoose.model('Dispute', disputeSchema);
