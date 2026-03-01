const mongoose = require('mongoose');
const bcrypt = require('bcryptjs');

const Branch = require('../branch/branchModel');
const User = require('../auth/userModel');
const Item = require('../item/itemModel');
const Inventory = require('../inventory/inventoryModel');

exports.seedDatabase = async (req, res) => {
    try {
        console.log('Seeding Database requested over API...');

        await Branch.deleteMany();
        await User.deleteMany();
        await Item.deleteMany();
        await Inventory.deleteMany();

        const admin = await User.create({
            name: 'Super Admin',
            email: 'admin@pskfoods.com',
            password: 'password123',
            role: 'admin',
            branchId: null
        });

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
                name: 'Employee Two (Central)',
                email: 'emp2.central@pskfoods.com',
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
            },
            {
                name: 'Employee Two (North)',
                email: 'emp2.north@pskfoods.com',
                password: 'password123',
                role: 'employee',
                branchId: branchNorth._id
            }
        ];

        for (const u of usersToCreate) {
            await User.create(u);
        }

        const itemsList = [
            { name: 'Burger', category: 'Fast Food', unitPrice: 5.00, purchaseCost: 2.00, sellingPrice: 5.00, marginPercentage: 60, createdBy: admin._id },
            { name: 'Pizza', category: 'Fast Food', unitPrice: 8.00, purchaseCost: 4.00, sellingPrice: 8.00, marginPercentage: 50, createdBy: admin._id },
            { name: 'French Fries', category: 'Fast Food', unitPrice: 3.00, purchaseCost: 1.00, sellingPrice: 3.00, marginPercentage: 66.6, createdBy: admin._id },
            { name: 'Veg Wrap', category: 'Fast Food', unitPrice: 4.00, purchaseCost: 1.50, sellingPrice: 4.00, marginPercentage: 62.5, createdBy: admin._id },
            { name: 'Chicken Wrap', category: 'Fast Food', unitPrice: 5.50, purchaseCost: 2.50, sellingPrice: 5.50, marginPercentage: 54.5, createdBy: admin._id },
            { name: 'Cold Coffee', category: 'Beverage', unitPrice: 3.50, purchaseCost: 1.00, sellingPrice: 3.50, marginPercentage: 71.4, createdBy: admin._id },
            { name: 'Soft Drink', category: 'Beverage', unitPrice: 2.00, purchaseCost: 0.50, sellingPrice: 2.00, marginPercentage: 75, createdBy: admin._id },
            { name: 'Sandwich', category: 'Fast Food', unitPrice: 3.50, purchaseCost: 1.50, sellingPrice: 3.50, marginPercentage: 57.1, createdBy: admin._id }
        ];

        const createdItems = await Item.insertMany(itemsList);

        const inventoryData = [];

        // Seed Central Branch
        createdItems.forEach(item => {
            inventoryData.push({
                branchId: branchCentral._id,
                itemId: item._id,
                quantity: Math.floor(Math.random() * 100) + 50
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

        res.status(200).json({ success: true, message: 'Database correctly seeded!' });
    } catch (error) {
        console.error('Error seeding DB via API:', error);
        res.status(500).json({ success: false, error: error.message });
    }
};
