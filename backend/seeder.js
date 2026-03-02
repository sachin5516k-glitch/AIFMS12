const mongoose = require('mongoose');
const dotenv = require('dotenv');
const bcrypt = require('bcryptjs');

// Load env vars
dotenv.config();

mongoose.connect(process.env.MONGO_URI);

const Branch = require('./src/branch/branchModel');
const User = require('./src/auth/userModel');
const Item = require('./src/item/itemModel');
const Inventory = require('./src/inventory/inventoryModel');
const Sales = require('./src/sales/salesModel');

const seedData = async () => {
    try {
        console.log('Connecting to database...');
        await mongoose.connect(process.env.MONGO_URI);

        console.log('Clearing existing data...');
        await Branch.deleteMany();
        await User.deleteMany();
        await Item.deleteMany();
        await Inventory.deleteMany();
        await Sales.deleteMany();

        console.log('Creating Admin User...');
        const salt = await bcrypt.genSalt(10);
        const adminPassword = await bcrypt.hash('admin123', salt);

        const adminUser = await User.create({
            name: 'Super Admin',
            email: 'admin@pskfoods.com',
            password: adminPassword, // Directly assigning hashed password will bypass the pre-save hook, but we MUST avoid double hashing. Let's just pass plain text to .create because of the pre-save hook.
            // WAIT: the pre("save") hook hashes the password IF it's modified. .create() calls .save() internally.
        });

        // Actually, since userModel has pre('save'), we should pass the plaintext password.
        await User.deleteMany(); // Reset

        const admin = await User.create({
            name: 'Super Admin',
            email: 'admin@pskfoods.com',
            password: 'password123',
            role: 'admin',
            branchId: null
        });

        console.log('Creating Branches...');
        const branchCentral = await Branch.create({
            name: 'PSK Foods - Central',
            location: { lat: 12.9716, lng: 77.5946 }, // Bangalore coords
            createdBy: admin._id,
        });

        const branchNorth = await Branch.create({
            name: 'PSK Foods - North',
            location: { lat: 13.0285, lng: 77.5410 }, // Yeshwanthpur coords
            createdBy: admin._id,
        });

        console.log('Creating Managers & Employees...');
        const usersToCreate = [
            {
                name: 'Central Manager',
                email: 'manager.central@pskfoods.com',
                password: 'password123',
                role: 'manager',
                branchId: branchCentral._id
            },
            {
                name: 'North Manager',
                email: 'manager.north@pskfoods.com',
                password: 'password123',
                role: 'manager',
                branchId: branchNorth._id
            },
            {
                name: 'Employee One (Central)',
                email: 'emp1.central@pskfoods.com',
                password: 'password123',
                role: 'employee',
                branchId: branchCentral._id
            },
            {
                name: 'Employee One (North)',
                email: 'emp1.north@pskfoods.com',
                password: 'password123',
                role: 'employee',
                branchId: branchNorth._id
            }
        ];
        await User.insertMany(usersToCreate); // NOTE: insertMany bypasses the mongoose pre('save') hook!
        // Re-write to use individual .save() or map loop
        await User.deleteMany({ role: { $ne: 'admin' } }); // remove the insertMany failures

        for (const u of usersToCreate) {
            await User.create(u);
        }

        console.log('Creating Real Food Items...');
        const itemsList = [
            { name: 'Burger', category: 'Fast Food', unitPrice: 5.00, purchaseCost: 2.00, sellingPrice: 5.00, marginPercentage: 60, createdBy: admin._id },
            { name: 'Pizza', category: 'Fast Food', unitPrice: 8.00, purchaseCost: 4.00, sellingPrice: 8.00, marginPercentage: 50, createdBy: admin._id },
            { name: 'French Fries', category: 'Fast Food', unitPrice: 3.00, purchaseCost: 1.00, sellingPrice: 3.00, marginPercentage: 66.6, createdBy: admin._id },
            { name: 'Veg Wrap', category: 'Fast Food', unitPrice: 4.00, purchaseCost: 1.50, sellingPrice: 4.00, marginPercentage: 62.5, createdBy: admin._id },
            { name: 'Cold Coffee', category: 'Beverage', unitPrice: 3.50, purchaseCost: 1.00, sellingPrice: 3.50, marginPercentage: 71.4, createdBy: admin._id }
        ];

        const createdItems = await Item.insertMany(itemsList); // No password hashing needed here, insertMany is fine

        console.log('Creating Initial Inventory...');
        const inventoryData = [];

        // Seed Central Branch
        createdItems.forEach(item => {
            inventoryData.push({
                branchId: branchCentral._id,
                itemId: item._id,
                quantity: Math.floor(Math.random() * 100) + 50 // Random quantity between 50 and 150
            });
        });

        // Seed North Branch
        createdItems.forEach(item => {
            inventoryData.push({
                branchId: branchNorth._id,
                itemId: item._id,
                quantity: Math.floor(Math.random() * 100) + 50
            });
        });

        await Inventory.insertMany(inventoryData);

        console.log('Creating Sales Data...');
        const salesData = [];
        const startDate = new Date();
        startDate.setDate(startDate.getDate() - 14); // Last 14 days

        const employees = await User.find({ role: 'employee' });

        for (let i = 0; i < 50; i++) {
            const branch = i % 2 === 0 ? branchCentral : branchNorth;
            const item = createdItems[Math.floor(Math.random() * createdItems.length)];
            const qty = Math.floor(Math.random() * 5) + 1;

            const saleDate = new Date(startDate.getTime() + Math.random() * (new Date().getTime() - startDate.getTime()));
            const emp = employees.find(e => e.branchId.toString() === branch._id.toString());

            salesData.push({
                branchId: branch._id,
                itemId: item._id,
                quantitySold: qty,
                totalAmount: qty * item.sellingPrice,
                sellingPrice: item.sellingPrice,
                paymentMode: Math.random() > 0.5 ? 'Cash' : 'UPI',
                createdBy: emp ? emp._id : admin._id,
                createdAt: saleDate
            });
        }
        await Sales.insertMany(salesData);

        console.log('Database Seeding Completed Successfully!');
        process.exit();

    } catch (error) {
        console.error('Error with Data Seeding:', error);
        process.exit(1);
    }
};

seedData();
