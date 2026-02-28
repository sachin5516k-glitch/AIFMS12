const Sales = require('../sales/salesModel');
const Branch = require('../branch/branchModel');

class ForecastService {

    /**
     * Forecast next 7 days sales for a branch using Simple Moving Average of last 30 days.
     */
    async getBranchForecast(branchId) {
        const thirtyDaysAgo = new Date();
        thirtyDaysAgo.setDate(thirtyDaysAgo.getDate() - 30);

        const sales = await Sales.find({
            branchId,
            createdAt: { $gte: thirtyDaysAgo }
        }).populate('itemId');

        // Total sales in 30 days
        let totalRevenue = 0;
        let itemFrequency = {};

        for (const sale of sales) {
            totalRevenue += sale.totalAmount;

            const itemIdStr = sale.itemId._id.toString();
            if (!itemFrequency[itemIdStr]) {
                itemFrequency[itemIdStr] = {
                    name: sale.itemId.name,
                    quantityTotal: 0
                };
            }
            itemFrequency[itemIdStr].quantityTotal += sale.quantitySold;
        }

        const avgDailyRevenue = totalRevenue / 30;

        // Next 7 days forecast
        const sevenDayRevenueForecast = avgDailyRevenue * 7;

        // Top 3 Items demand forecast
        const itemsArr = Object.values(itemFrequency).sort((a, b) => b.quantityTotal - a.quantityTotal);
        const topDemandItems = itemsArr.slice(0, 3).map(i => ({
            name: i.name,
            expected7DayDemand: Math.ceil((i.quantityTotal / 30) * 7)
        }));

        return {
            averageDailyRevenue: avgDailyRevenue,
            predicted7DayRevenue: sevenDayRevenueForecast,
            topPredictedDemands: topDemandItems
        };
    }
}

module.exports = new ForecastService();
