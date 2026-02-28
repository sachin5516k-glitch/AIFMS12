const asyncHandler = require('express-async-handler');
const mongoose = require('mongoose');
const StockTransferRecommendation = require('./stockTransferRecommendationModel');
const StockTransferRequest = require('./stockTransferRequestModel');
const Inventory = require('../inventory/inventoryModel');
const Notification = require('../notification/notificationModel');
const AuditLog = require('../audit/auditLogModel');

// @desc    Get Transfer Recommendations for a branch
// @route   GET /api/transfers/recommendations
// @access  Private (Manager/Admin)
const getRecommendations = asyncHandler(async (req, res) => {
    let query = {};
    if (req.user.role !== 'admin') {
        // Manager can see incoming and outgoing recommendations
        query = {
            $or: [
                { fromBranchId: req.user.branchId },
                { toBranchId: req.user.branchId }
            ]
        };
    }

    const recommendations = await StockTransferRecommendation.find(query)
        .populate('itemId', 'name category unitPrice')
        .populate('fromBranchId', 'name')
        .populate('toBranchId', 'name')
        .sort({ createdAt: -1 });

    res.status(200).json({ success: true, data: recommendations });
});

// @desc    Approve a Transfer Recommendation
// @route   PUT /api/transfers/recommendations/:id/approve
// @access  Private (Manager/Admin)
const approveRecommendation = asyncHandler(async (req, res) => {
    const session = await mongoose.startSession();
    session.startTransaction();

    try {
        const rec = await StockTransferRecommendation.findById(req.params.id)
            .populate('fromBranchId')
            .populate('toBranchId')
            .populate('itemId')
            .session(session);

        if (!rec) {
            throw new Error('Recommendation not found');
        }

        if (rec.status !== 'PENDING') {
            throw new Error('Recommendation is not pending');
        }

        // Authorization check
        if (req.user.role !== 'admin' &&
            req.user.branchId.toString() !== rec.fromBranchId._id.toString() &&
            req.user.branchId.toString() !== rec.toBranchId._id.toString()) {
            throw new Error('Not authorized to approve this transfer');
        }

        // 1. Deduct quantity from source branch inventory
        const sourceInventory = await Inventory.findOne({ branchId: rec.fromBranchId._id, itemId: rec.itemId._id }).session(session);
        if (!sourceInventory || sourceInventory.quantity < rec.suggestedQuantity) {
            throw new Error('Insufficient stock in the source branch');
        }
        sourceInventory.quantity -= rec.suggestedQuantity;
        await sourceInventory.save({ session });

        // 2. Add quantity to target branch inventory
        let targetInventory = await Inventory.findOne({ branchId: rec.toBranchId._id, itemId: rec.itemId._id }).session(session);
        if (targetInventory) {
            targetInventory.quantity += rec.suggestedQuantity;
            await targetInventory.save({ session });
        } else {
            await Inventory.create([{
                branchId: rec.toBranchId._id,
                itemId: rec.itemId._id,
                quantity: rec.suggestedQuantity
            }], { session });
        }

        // 3. Update status
        rec.status = 'APPROVED';
        await rec.save({ session });

        // 4. Notify both branches
        const message = `Transfer of ${rec.suggestedQuantity} ${rec.itemId.name} from ${rec.fromBranchId.name} to ${rec.toBranchId.name} has been APPROVED.`;

        await Notification.create([{ branchId: rec.fromBranchId._id, title: 'Transfer Approved', message, type: 'TRANSFER_APPROVED' }], { session });
        await Notification.create([{ branchId: rec.toBranchId._id, title: 'Transfer Approved', message, type: 'TRANSFER_APPROVED' }], { session });
        await Notification.create([{ branchId: null, title: 'Transfer Approved', message, type: 'TRANSFER_APPROVED' }], { session });

        await AuditLog.create([{
            userId: req.user._id,
            action: 'STOCK_TRANSFER_APPROVED',
            details: `Approved transfer of ${rec.suggestedQuantity} ${rec.itemId.name} from ${rec.fromBranchId.name} to ${rec.toBranchId.name}.`,
            ipAddress: req.ip,
            userAgent: req.headers['user-agent']
        }], { session });

        await session.commitTransaction();
        res.status(200).json({ success: true, data: rec, message: 'Transfer approved successfully' });
    } catch (error) {
        await session.abortTransaction();
        res.status(400).json({ success: false, message: error.message });
    } finally {
        session.endSession();
    }
});

// @desc    Reject a Transfer Recommendation
// @route   PUT /api/transfers/recommendations/:id/reject
// @access  Private (Manager/Admin)
const rejectRecommendation = asyncHandler(async (req, res) => {
    const { reason } = req.body;
    const rec = await StockTransferRecommendation.findById(req.params.id)
        .populate('fromBranchId')
        .populate('toBranchId')
        .populate('itemId');

    if (!rec) {
        res.status(404);
        throw new Error('Recommendation not found');
    }

    if (rec.status !== 'PENDING') {
        res.status(400);
        throw new Error('Recommendation is not pending');
    }

    // Authorization check
    if (req.user.role !== 'admin' &&
        req.user.branchId.toString() !== rec.fromBranchId._id.toString() &&
        req.user.branchId.toString() !== rec.toBranchId._id.toString()) {
        res.status(403);
        throw new Error('Not authorized to reject this transfer');
    }

    rec.status = 'REJECTED';
    rec.reason = reason ? `${rec.reason} | Rejected because: ${reason}` : rec.reason;
    await rec.save();

    // Notify branches
    const message = `Transfer recommendation for ${rec.itemId.name} from ${rec.fromBranchId.name} to ${rec.toBranchId.name} was REJECTED.`;
    await Notification.create({ branchId: rec.fromBranchId._id, title: 'Transfer Rejected', message, type: 'TRANSFER_REJECTED' });
    await Notification.create({ branchId: rec.toBranchId._id, title: 'Transfer Rejected', message, type: 'TRANSFER_REJECTED' });

    await AuditLog.create({
        userId: req.user._id,
        action: 'STOCK_TRANSFER_REJECTED',
        details: `Rejected transfer of ${rec.itemId.name} from ${rec.fromBranchId.name} to ${rec.toBranchId.name}. Reason: ${reason || 'None'}`,
        ipAddress: req.ip,
        userAgent: req.headers['user-agent']
    });

    res.status(200).json({ success: true, data: rec, message: 'Transfer rejected' });
});

// @desc    Create Manual Stock Transfer Request
// @route   POST /api/transfers/requests
// @access  Private (Manager/Admin)
const createManualRequest = asyncHandler(async (req, res) => {
    const { targetBranchId, itemId, quantity } = req.body;

    const requestedByBranchId = req.user.role === 'admin' ? req.body.requestedByBranchId : req.user.branchId;

    if (!targetBranchId || !itemId || !quantity) {
        res.status(400);
        throw new Error('Please provide targetBranchId, itemId, and quantity');
    }

    const request = await StockTransferRequest.create({
        requestedByBranchId,
        targetBranchId,
        itemId,
        quantity
    });

    // Notify target branch and admin
    const populatedReq = await StockTransferRequest.findById(request._id)
        .populate('requestedByBranchId', 'name')
        .populate('itemId', 'name');

    const message = `${populatedReq.requestedByBranchId.name} has requested ${quantity} units of ${populatedReq.itemId.name}.`;

    await Notification.create({ branchId: targetBranchId, title: 'New Stock Request', message, type: 'TRANSFER_REQUEST' });
    await Notification.create({ branchId: null, title: 'New Stock Request', message, type: 'TRANSFER_REQUEST' });

    await AuditLog.create({
        userId: req.user._id,
        action: 'MANUAL_STOCK_REQUEST_CREATED',
        details: `Requested ${quantity} ${populatedReq.itemId.name} from ${targetBranchId} for ${requestedByBranchId}.`,
        ipAddress: req.ip,
        userAgent: req.headers['user-agent']
    });

    res.status(201).json({ success: true, data: request });
});

// @desc    Get Transfer Requests
// @route   GET /api/transfers/requests
// @access  Private (Manager/Admin)
const getRequests = asyncHandler(async (req, res) => {
    let query = {};
    if (req.user.role !== 'admin') {
        query = {
            $or: [
                { requestedByBranchId: req.user.branchId },
                { targetBranchId: req.user.branchId }
            ]
        };
    }

    const requests = await StockTransferRequest.find(query)
        .populate('itemId', 'name category unitPrice')
        .populate('requestedByBranchId', 'name')
        .populate('targetBranchId', 'name')
        .sort({ createdAt: -1 });

    res.status(200).json({ success: true, data: requests });
});

// @desc    Approve a Manual Request
// @route   PUT /api/transfers/requests/:id/approve
// @access  Private (Manager/Admin)
const approveRequest = asyncHandler(async (req, res) => {
    const session = await mongoose.startSession();
    session.startTransaction();

    try {
        const transferReq = await StockTransferRequest.findById(req.params.id)
            .populate('requestedByBranchId')
            .populate('targetBranchId')
            .populate('itemId')
            .session(session);

        if (!transferReq) throw new Error('Request not found');
        if (transferReq.status !== 'PENDING') throw new Error('Request is not pending');

        // Only Admin or Target Branch Manager (the one giving stock) can approve
        if (req.user.role !== 'admin' && req.user.branchId.toString() !== transferReq.targetBranchId._id.toString()) {
            throw new Error('Not authorized to approve this request (Only Target Branch or Admin)');
        }

        // Deduct from Target Branch (Source of stock)
        const sourceInventory = await Inventory.findOne({ branchId: transferReq.targetBranchId._id, itemId: transferReq.itemId._id }).session(session);
        if (!sourceInventory || sourceInventory.quantity < transferReq.quantity) {
            throw new Error('Insufficient stock in the target branch to fulfill request');
        }
        sourceInventory.quantity -= transferReq.quantity;
        await sourceInventory.save({ session });

        // Add to Requesting Branch
        let destInventory = await Inventory.findOne({ branchId: transferReq.requestedByBranchId._id, itemId: transferReq.itemId._id }).session(session);
        if (destInventory) {
            destInventory.quantity += transferReq.quantity;
            await destInventory.save({ session });
        } else {
            await Inventory.create([{
                branchId: transferReq.requestedByBranchId._id,
                itemId: transferReq.itemId._id,
                quantity: transferReq.quantity
            }], { session });
        }

        transferReq.status = 'APPROVED';
        await transferReq.save({ session });

        const message = `Request for ${transferReq.quantity} ${transferReq.itemId.name} has been APPROVED by ${transferReq.targetBranchId.name}.`;
        await Notification.create([{ branchId: transferReq.requestedByBranchId._id, title: 'Request Approved', message, type: 'TRANSFER_APPROVED' }], { session });
        await Notification.create([{ branchId: null, title: 'Request Approved', message, type: 'TRANSFER_APPROVED' }], { session });

        await AuditLog.create([{
            userId: req.user._id,
            action: 'MANUAL_STOCK_REQUEST_APPROVED',
            details: `Approved manual request of ${transferReq.quantity} ${transferReq.itemId.name} from ${transferReq.targetBranchId.name} to ${transferReq.requestedByBranchId.name}.`,
            ipAddress: req.ip,
            userAgent: req.headers['user-agent']
        }], { session });

        await session.commitTransaction();
        res.status(200).json({ success: true, data: transferReq, message: 'Request approved successfully' });
    } catch (error) {
        await session.abortTransaction();
        res.status(400).json({ success: false, message: error.message });
    } finally {
        session.endSession();
    }
});

// @desc    Reject a Manual Request
// @route   PUT /api/transfers/requests/:id/reject
// @access  Private (Manager/Admin)
const rejectRequest = asyncHandler(async (req, res) => {
    const transferReq = await StockTransferRequest.findById(req.params.id)
        .populate('requestedByBranchId')
        .populate('targetBranchId')
        .populate('itemId');

    if (!transferReq) {
        res.status(404);
        throw new Error('Request not found');
    }

    if (transferReq.status !== 'PENDING') {
        res.status(400);
        throw new Error('Request is not pending');
    }

    if (req.user.role !== 'admin' && req.user.branchId.toString() !== transferReq.targetBranchId._id.toString()) {
        res.status(403);
        throw new Error('Not authorized to reject this request');
    }

    transferReq.status = 'REJECTED';
    await transferReq.save();

    const message = `Your request for ${transferReq.quantity} ${transferReq.itemId.name} was REJECTED by ${transferReq.targetBranchId.name}.`;
    await Notification.create({ branchId: transferReq.requestedByBranchId._id, title: 'Request Rejected', message, type: 'TRANSFER_REJECTED' });

    await AuditLog.create({
        userId: req.user._id,
        action: 'MANUAL_STOCK_REQUEST_REJECTED',
        details: `Rejected manual request of ${transferReq.quantity} ${transferReq.itemId.name} from ${transferReq.targetBranchId.name} to ${transferReq.requestedByBranchId.name}.`,
        ipAddress: req.ip,
        userAgent: req.headers['user-agent']
    });

    res.status(200).json({ success: true, data: transferReq, message: 'Request rejected' });
});

module.exports = {
    getRecommendations,
    approveRecommendation,
    rejectRecommendation,
    createManualRequest,
    getRequests,
    approveRequest,
    rejectRequest
};
