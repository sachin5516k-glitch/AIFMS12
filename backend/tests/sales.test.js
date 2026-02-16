const request = require('supertest');
const express = require('express');
const db = require('./utils/db');
const salesRoutes = require('../src/sales/salesRoutes');
const Sales = require('../src/sales/salesModel');
const User = require('../src/auth/userModel');
const jwt = require('jsonwebtoken');

// Mock AI Engine
jest.mock('../src/ai/fraud.engine', () => ({
    calculateFraudScore: jest.fn().mockResolvedValue({ score: 10, reasons: [] })
}));

const app = express();
app.use(express.json());
app.use((req, res, next) => {
    req.user = { _id: 'mockUserId', role: 'outlet_manager' }; // Mock Auth Middleware
    next();
});
app.use('/api/sales', salesRoutes);

beforeAll(async () => await db.connect());
afterEach(async () => await db.clear());
afterAll(async () => await db.close());

describe('Sales Module', () => {

    it('should submit sales successfully', async () => {
        const res = await request(app)
            .post('/api/sales/submit')
            .send({
                outletId: 'outlet_001',
                amount: 5000,
                paymentMode: 'Cash',
                imageUrl: 'http://example.com/image.jpg'
            });

        expect(res.statusCode).toEqual(201);
        expect(res.body.amount).toBe(5000);
        expect(res.body.fraudScore).toBe(10); // From mock
    });

    it('should reject missing fields', async () => {
        const res = await request(app)
            .post('/api/sales/submit')
            .send({
                outletId: 'outlet_001'
                // Missing amount/paymentMode
            });

        expect(res.statusCode).toEqual(400);
    });

    it('should prevent duplicate submission (Same Outlet, Amount, Time Window)', async () => {
        // First submission
        await request(app)
            .post('/api/sales/submit')
            .send({
                outletId: 'outlet_001',
                amount: 5000,
                paymentMode: 'Cash',
                imageUrl: 'img1'
            });

        // Duplicate submission
        const res = await request(app)
            .post('/api/sales/submit')
            .send({
                outletId: 'outlet_001',
                amount: 5000,
                paymentMode: 'Cash',
                imageUrl: 'img1'
            });

        expect(res.statusCode).toEqual(400);
        expect(res.body.message).toContain('Duplicate');
    });
});
