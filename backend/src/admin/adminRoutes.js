const express = require('express');
const router = express.Router();
const {
    getManagers,
    addManager,
    deactivateManager
} = require('./adminController');
const { protect, authorize } = require('../middleware/authMiddleware');

router.use(protect);
router.use(authorize('admin'));

router.route('/managers')
    .get(getManagers)
    .post(addManager);

router.route('/managers/:id/deactivate')
    .put(deactivateManager);

router.route('/dashboard-summary')
    .get(require('./adminController').getDashboardSummary);

router.route('/stock-transfer/request')
    .post(require('./adminController').createManualTransferRequest);

module.exports = router;
