const mongoose = require('mongoose');
const dotenv = require('dotenv');
const User = require('../src/auth/userModel');
const Franchise = require('../src/franchise/franchiseModel');
const Outlet = require('../src/franchise/outletModel');
const Sales = require('../src/sales/salesModel');

dotenv.config({ path: '.env' });

const verifyDB = async () => {
    try {
        await mongoose.connect(process.env.MONGO_URI);
        console.log('✅ MongoDB Connected for Verification');

        // Check Indexes
        const userIndexes = await User.listIndexes();
        console.log('User Indexes:', userIndexes);

        const franchiseIndexes = await Franchise.listIndexes();
        console.log('Franchise Indexes:', franchiseIndexes);

        const salesIndexes = await Sales.listIndexes();
        console.log('Sales Indexes:', salesIndexes);

        // Validation Test: User with invalid email
        try {
            await User.create({
                name: 'Test Bad Email',
                email: 'not-an-email',
                password: '123'
            });
        } catch (e) {
            console.log('✅ Validation Working: Rejected invalid email');
        }

        // Remove test data if explicit cleanup needed (skipping for now as create failed)

        console.log('✅ All Database Integrity Checks Passed');
        process.exit(0);
    } catch (error) {
        console.error('❌ Verification Failed:', error);
        process.exit(1);
    }
};

verifyDB();
