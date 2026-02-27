const express = require('express');
const {
    getRecommendations,
    approveRecommendation,
    rejectRecommendation,
    createManualRequest,
    getRequests,
    approveRequest,
    rejectRequest
} = require('./transferController');
const { protect } = require('../middleware/authMiddleware');

const router = express.Router();

router.use(protect);

router.route('/recommendations')
    .get(getRecommendations);

router.route('/recommendations/:id/approve')
    .put(approveRecommendation);

router.route('/recommendations/:id/reject')
    .put(rejectRecommendation);

router.route('/requests')
    .post(createManualRequest)
    .get(getRequests);

router.route('/requests/:id/approve')
    .put(approveRequest);

router.route('/requests/:id/reject')
    .put(rejectRequest);

module.exports = router;
