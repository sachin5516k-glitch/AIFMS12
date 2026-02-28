const BranchFinancials = require('../analytics/branchFinancialsModel');
const Sales = require('../sales/salesModel');
const Item = require('../item/itemModel');
const Branch = require('../branch/branchModel');

class ProfitLossService {

    /**
     * Calculates P&L for a specific branch over a date range and saves/updates it.
     */
    async calculateBranchFinancials(branchId, startDate, endDate, period = 'DAILY') {
        const sales = await Sales.find({
            branchId,
            createdAt: { $gte: startDate, $lte: endDate }
        });

        let revenue = 0;
        let cogs = 0;

        for (const sale of sales) {
            // Use snapshot sellingPrice if available, else fallback (though sellingPrice is now required!)
            const salePrice = sale.sellingPrice || (sale.totalAmount / sale.quantitySold);
            revenue += sale.totalAmount;

            // Fetch current purchase cost of the item to calculate COGS
            // In a strict financial system, purchaseCost would be snapshotted in Sales or Inventory ledger too.
            // Using the current item configuration as a baseline here.
            const item = await Item.findById(sale.itemId);
            if (item && item.purchaseCost) {
                cogs += (item.purchaseCost * sale.quantitySold);
            }
        }

        const grossProfit = revenue - cogs;
        const profitPercentage = revenue > 0 ? ((grossProfit / revenue) * 100) : 0;

        // Upsert the record for the period date (normalized to midnight if DAILY)
        let normalizedDate = new Date(startDate);
        if (period === 'DAILY') {
            normalizedDate.setHours(0, 0, 0, 0);
        } else if (period === 'MONTHLY') {
            normalizedDate.setDate(1);
            normalizedDate.setHours(0, 0, 0, 0);
        }

        const financials = await BranchFinancials.findOneAndUpdate(
            { branchId, date: normalizedDate, period },
            {
                revenue,
                cogs,
                grossProfit,
                profitPercentage
            },
            { upsert: true, new: true }
        );

        return financials;
    }

    /**
     * Calculates across all branches for yesterday (Daily Run)
     */
    async calculateGlobalYesterday() {
        console.log('[ProfitLoss] Running daily P&L calculation...');
        const startDate = new Date();
        startDate.setDate(startDate.getDate() - 1);
        startDate.setHours(0, 0, 0, 0);

        const endDate = new Date(startDate);
        endDate.setHours(23, 59, 59, 999);

        const branches = await Branch.find({});
        for (const branch of branches) {
            await this.calculateBranchFinancials(branch._id, startDate, endDate, 'DAILY');
        }
        console.log('[ProfitLoss] Daily P&L calculations complete.');
    }
}

module.exports = new ProfitLossService();
