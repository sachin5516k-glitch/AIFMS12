const asyncHandler = require('express-async-handler');
const Sales = require('../sales/salesModel');
const Inventory = require('../inventory/inventoryModel');
const Attendance = require('../attendance/attendanceModel');

// @desc    Get Sales Report
// @route   GET /api/reports/sales
// @access  Private/Owner
const getSalesReport = asyncHandler(async (req, res) => {
    const { startDate, endDate, outletId } = req.query;

    const query = {};
    if (outletId) query.outletId = outletId;
    if (startDate && endDate) {
        query.createdAt = {
            $gte: new Date(startDate),
            $lte: new Date(endDate)
        };
    }

    const report = await Sales.aggregate([
        { $match: query },
        {
            $group: {
                _id: "$outletId",
                totalSales: { $sum: "$amount" },
                avgFraudScore: { $avg: "$fraudScore" },
                transactionCount: { $sum: 1 }
            }
        }
    ]);

    res.json(report);
});

// @desc    Export Sales Data as CSV
// @route   GET /api/reports/export/sales
// @access  Private/Admin
const exportSalesCsv = asyncHandler(async (req, res) => {
    // Only Admin can export entire system data generally,
    // but the system has role checks in middleware protecting this route.
    const sales = await Sales.find({})
        .populate('branchId', 'name')
        .populate('itemId', 'name purchaseCost sellingPrice');

    let csvContent = "Transaction ID,Branch,Item,Quantity,Total Amount,Payment Mode,Date\n";

    for (const sale of sales) {
        const branchName = sale.branchId ? sale.branchId.name : "Unknown Branch";
        const itemName = sale.itemId ? sale.itemId.name : "Unknown Item";
        const txId = sale._id.toString();
        const qty = sale.quantitySold;
        const amt = sale.totalAmount;
        const mode = sale.paymentMode;
        const date = new Date(sale.createdAt).toISOString();

        csvContent += `"${txId}","${branchName}","${itemName}",${qty},${amt},"${mode}","${date}"\n`;
    }

    res.header('Content-Type', 'text/csv');
    res.attachment('sales_export.csv');
    return res.status(200).send(csvContent);
});

// @desc    Get Inventory Summary for Admin
// @route   GET /api/admin/inventory-summary
// @access  Private/Admin
const getAdminInventorySummary = asyncHandler(async (req, res) => {
    try {
        const inventory = await Inventory.find({}).populate('itemId', 'name category unitPrice').populate('branchId', 'name');

        let lowStockCount = 0;
        let totalValuation = 0;

        // Let's count unique actual items based on itemId
        const uniqueItems = new Set();

        inventory.forEach(inv => {
            if (inv.itemId) {
                uniqueItems.add(inv.itemId._id.toString());
                totalValuation += inv.quantity * (inv.itemId.unitPrice || 0);
                if (inv.quantity < 10) {
                    lowStockCount++;
                }
            }
        });

        const totalItems = uniqueItems.size;

        const healthyPercentage = inventory.length > 0
            ? Math.round(((inventory.length - lowStockCount) / inventory.length) * 100)
            : 100;

        return res.status(200).json({
            success: true,
            message: 'Admin inventory summary retrieved',
            data: {
                totalItems,
                lowStockCount,
                healthyPercentage,
                totalValuation,
                items: inventory
            }
        });
    } catch (error) {
        return res.status(500).json({
            success: false,
            message: 'Failed to generate inventory summary',
            data: null
        });
    }
});

module.exports = { getSalesReport, exportSalesCsv, getAdminInventorySummary };
