const cron = require('node-cron');
const stockAnalysisService = require('../services/stockAnalysisService');

// Schedule AI Stock Analysis to run every 12 hours
const startStockCronJobs = () => {
    // 0 0,12 * * * means 12:00 AM and 12:00 PM every day
    cron.schedule('0 0,12 * * *', async () => {
        console.log('[Cron Job] Executing AI Stock Analysis System...');
        await stockAnalysisService.analyzeAndGenerateRecommendations();
    });
    console.log('[Cron Job] AI Stock Analysis scheduled to run every 12 hours.');
};

module.exports = { startStockCronJobs };
