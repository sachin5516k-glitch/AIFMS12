const Inventory = require('../inventory/inventoryModel');
const Sales = require('../sales/salesModel');
const Branch = require('../branch/branchModel');
const Item = require('../item/itemModel');
const StockTransferRecommendation = require('../transfer/stockTransferRecommendationModel');
const Notification = require('../notification/notificationModel');

/**
 * Service to analyze stock levels across all branches, identify surpluses and shortages,
 * and generate stock transfer recommendations.
 */
class StockAnalysisService {

    async analyzeAndGenerateRecommendations() {
        console.log('[StockAnalysisService] Starting AI Stock Analysis...');

        try {
            const items = await Item.find({});
            const branches = await Branch.find({});
            const sevenDaysAgo = new Date();
            sevenDaysAgo.setDate(sevenDaysAgo.getDate() - 7);

            for (const item of items) {
                const branchMetrics = [];

                for (const branch of branches) {
                    // Get current stock
                    const inventory = await Inventory.findOne({ branchId: branch._id, itemId: item._id });
                    const currentStock = inventory ? inventory.quantity : 0;

                    // Get average daily sales over the last 7 days
                    const salesData = await Sales.aggregate([
                        {
                            $match: {
                                branchId: branch._id,
                                itemId: item._id,
                                createdAt: { $gte: sevenDaysAgo }
                            }
                        },
                        { $group: { _id: null, totalSold: { $sum: "$quantitySold" } } }
                    ]);

                    const totalSales7Days = salesData.length > 0 ? salesData[0].totalSold : 0;
                    const avgDailySales = totalSales7Days / 7;

                    // Avoid division by zero. If avgDailySales is 0, they have infinite stock remaining.
                    // If they have 0 stock and 0 sales, we can consider them not needing stock right now, 
                    // or just having 0 days remaining but we probably shouldn't send them stock if they never sell it.
                    let daysRemaining = Infinity;
                    if (avgDailySales > 0) {
                        daysRemaining = currentStock / avgDailySales;
                    } else if (currentStock === 0) {
                        daysRemaining = 0; // 0 stock and 0 sales
                    }

                    let status = 'HEALTHY';
                    let excessStock = 0;
                    let requiredStock = 0;

                    if (daysRemaining < 3 && avgDailySales > 0) {
                        status = 'LOW_STOCK';
                        // Target to replenish to at least 7 days of stock
                        requiredStock = Math.ceil((7 * avgDailySales) - currentStock);
                    } else if (daysRemaining > 10 && currentStock > 0) {
                        status = 'SURPLUS';
                        // Safe to transfer excess above 10 days of stock
                        excessStock = Math.floor(currentStock - (10 * avgDailySales));
                    }

                    branchMetrics.push({
                        branchId: branch._id,
                        branchName: branch.name,
                        currentStock,
                        avgDailySales,
                        daysRemaining,
                        status,
                        requiredStock,
                        excessStock
                    });
                }

                // Match Low Stock branches with Surplus branches
                const lowStockBranches = branchMetrics.filter(b => b.status === 'LOW_STOCK');
                const surplusBranches = branchMetrics.filter(b => b.status === 'SURPLUS').sort((a, b) => b.excessStock - a.excessStock);

                for (const lowStock of lowStockBranches) {
                    let stillNeeded = lowStock.requiredStock;

                    // Find a surplus branch that can fulfill this
                    for (const surplus of surplusBranches) {
                        if (stillNeeded > 0 && surplus.excessStock > 0) {
                            // Check if a pending recommendation already exists between these two branches for this item
                            const existingRec = await StockTransferRecommendation.findOne({
                                itemId: item._id,
                                fromBranchId: surplus.branchId,
                                toBranchId: lowStock.branchId,
                                status: 'PENDING'
                            });

                            if (existingRec) {
                                continue; // Already recommended, don't spam
                            }

                            const transferQuantity = Math.min(stillNeeded, surplus.excessStock);

                            if (transferQuantity > 0) {
                                // Calculate urgency based on how few days are remaining
                                let urgencyLevel = 'MEDIUM';
                                if (lowStock.daysRemaining < 1) urgencyLevel = 'CRITICAL';
                                else if (lowStock.daysRemaining < 2) urgencyLevel = 'HIGH';

                                // Create Recommendation
                                const rec = await StockTransferRecommendation.create({
                                    itemId: item._id,
                                    fromBranchId: surplus.branchId,
                                    toBranchId: lowStock.branchId,
                                    suggestedQuantity: transferQuantity,
                                    reason: `AI Match: ${lowStock.branchName} has ${lowStock.daysRemaining.toFixed(1)} days stock remaining, while ${surplus.branchName} has an excess of ${surplus.excessStock}.`,
                                    urgencyLevel
                                });

                                // Reduce the available excess stock and still needed
                                surplus.excessStock -= transferQuantity;
                                stillNeeded -= transferQuantity;

                                // Create Notifications
                                await this.createNotificationsForRecommendation(rec, item, surplus, lowStock);
                            }
                        }
                    }
                }
            }
            console.log('[StockAnalysisService] AI Stock Analysis completed successfully.');
        } catch (error) {
            console.error('[StockAnalysisService] Error in AI Stock Analysis:', error.message);
        }
    }

    async createNotificationsForRecommendation(rec, item, fromBranch, toBranch) {
        const title = "New AI Transfer Recommendation";
        const message = `PSK Foods AI Alert: ${toBranch.branchName} has low stock of ${item.name}. Suggested transfer of ${rec.suggestedQuantity} units from ${fromBranch.branchName}.`;

        // Notify Admin
        await Notification.create({
            branchId: null, // Global admin
            title,
            message,
            type: 'TRANSFER_RECOMMENDATION'
        });

        // Notify Source Manager
        await Notification.create({
            branchId: fromBranch.branchId,
            title,
            message,
            type: 'TRANSFER_RECOMMENDATION'
        });

        // Notify Target Manager
        await Notification.create({
            branchId: toBranch.branchId,
            title,
            message,
            type: 'TRANSFER_RECOMMENDATION'
        });
    }
}

module.exports = new StockAnalysisService();
