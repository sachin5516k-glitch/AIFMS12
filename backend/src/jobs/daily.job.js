const cron = require('node-cron');
const Branch = require('../branch/branchModel'); // Assuming we iterate outlets from franchises or separate Outlet model
const { checkCompliance } = require('../compliance/compliance.engine');
const { calculateHealthScore } = require('../ai/health.engine');

// Schedule: Daily at Midnight (0 0 * * *)
const startDailyJob = () => {
    cron.schedule('0 0 * * *', async () => {
        console.log('Running Daily Compliance & Health Check Job...');

        try {
            // Fetch all active outlets (Mocking list if API/Model not fully ready for outlet iteration)
            // In prod: await Outlet.find({ status: 'active' })
            const outlets = ['outlet_001'];

            for (const outletId of outlets) {
                // 1. Check Compliance
                await checkCompliance(outletId);

                // 2. Update Health Score (and store history)
                const { healthScore, factors } = await calculateHealthScore(outletId);
                console.log(`Outlet ${outletId} Health: ${healthScore}`);

                // TODO: Save to daily_stats collection
            }
        } catch (error) {
            console.error('Error in daily job:', error);
        }
    });
};

module.exports = { startDailyJob };
