require('dotenv').config();
const mongoose = require('mongoose');
const connectDB = require('./config/db');
const Branch = require('./branch/branchModel');
const Item = require('./item/itemModel');
const Inventory = require('./inventory/inventoryModel');
const Sales = require('./sales/salesModel');
const StockAnalysisService = require('./services/stockAnalysisService');
const StockTransferRecommendation = require('./transfer/stockTransferRecommendationModel');

async function runTest() {
    await connectDB();

    console.log('--- Cleaning up old test data ---');
    await StockTransferRecommendation.deleteMany({});

    // Grab a random item and two branches
    const items = await Item.find({});
    const branches = await Branch.find({});

    if (items.length < 1 || branches.length < 2) {
        console.log('Not enough items or branches in DB. Please seed DB first.');
        process.exit(1);
    }

    const item = items[0];
    const branchSurplus = branches[0];
    const branchLow = branches[1];

    console.log(`Setting up Item: ${item.name}`);
    console.log(`Surplus Branch: ${branchSurplus.name}`);
    console.log(`Low Stock Branch: ${branchLow.name}`);

    // Clear inventory and sales for this item in these branches
    await Inventory.deleteMany({ itemId: item._id, branchId: { $in: [branchSurplus._id, branchLow._id] } });
    await Sales.deleteMany({ itemId: item._id, branchId: { $in: [branchSurplus._id, branchLow._id] } });

    // Seed Sales for last 7 days to give them avg daily sales of 10
    const sevenDaysAgo = new Date();
    sevenDaysAgo.setDate(sevenDaysAgo.getDate() - 3); // 3 days ago

    for (let i = 0; i < 7; i++) {
        await Sales.create({
            branchId: branchSurplus._id,
            itemId: item._id,
            quantitySold: 10,
            totalAmount: 100,
            paymentMode: 'Cash',
            createdBy: (await require('./auth/userModel').findOne({}))?._id || new mongoose.Types.ObjectId(),
            createdAt: sevenDaysAgo
        });

        await Sales.create({
            branchId: branchLow._id,
            itemId: item._id,
            quantitySold: 10,
            totalAmount: 100,
            paymentMode: 'Cash',
            createdBy: (await require('./auth/userModel').findOne({}))?._id || new mongoose.Types.ObjectId(),
            createdAt: sevenDaysAgo
        });
    }

    // Target avg daily sales = 10 for both branches.
    // Surplus branch needs >10 days of stock (>100)
    await Inventory.create({ branchId: branchSurplus._id, itemId: item._id, quantity: 150 });

    // Low stock branch needs <3 days of stock (<30)
    await Inventory.create({ branchId: branchLow._id, itemId: item._id, quantity: 10 });

    console.log('--- Running AI Stock Analysis ---');
    await StockAnalysisService.analyzeAndGenerateRecommendations();

    console.log('--- Checking Results ---');
    const recs = await StockTransferRecommendation.find({
        itemId: item._id,
        fromBranchId: branchSurplus._id,
        toBranchId: branchLow._id
    });

    if (recs.length > 0) {
        console.log('SUCCESS: AI found the imbalance and generated a recommendation.');
        console.log(recs[0]);
    } else {
        console.log('FAILED: No recommendation generated.');
    }

    process.exit(0);
}

runTest();
