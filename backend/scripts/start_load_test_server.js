process.env.NODE_ENV = 'test'; // Force test mode to prevent auto-DB connection

const { MongoMemoryServer } = require('mongodb-memory-server');
const mongoose = require('mongoose');
const app = require('../src/server');
const User = require('../src/auth/userModel');
const bcrypt = require('bcryptjs');

const PORT = 5000;

const startServer = async () => {
    // 1. Start In-Memory DB
    const mongoServer = await MongoMemoryServer.create();
    const uri = mongoServer.getUri();

    await mongoose.disconnect(); // Disconnect any existing attempts
    await mongoose.connect(uri);
    console.log(`Mock DB Connected: ${uri}`);

    // 2. Seed Data for Load Test
    const salt = await bcrypt.genSalt(10);
    const hashedPassword = await bcrypt.hash('password123', salt);

    await User.create({
        name: 'Load Test Owner',
        email: 'owner@test.com',
        password: hashedPassword, // 'password123'
        role: 'owner',
        outletId: 'outlet1'
    });
    console.log('Seeded Load Test User: owner@test.com / password123');

    // 3. Start Server
    const server = app.listen(PORT, () => {
        console.log(`Load Test Server running on port ${PORT}`);
    });

    // Handle shutdown
    const shutdown = async () => {
        await mongoose.disconnect();
        await mongoServer.stop();
        server.close(() => {
            console.log('Load Test Server stopped');
            process.exit(0);
        });
    };

    process.on('SIGINT', shutdown);
    process.on('SIGTERM', shutdown);
};

startServer();
