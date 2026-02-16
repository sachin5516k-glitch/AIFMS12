const request = require('supertest');
const express = require('express');
const db = require('./utils/db');
const inventoryRoutes = require('../src/inventory/inventoryRoutes');
const Inventory = require('../src/inventory/inventoryModel');

const app = express();
app.use(express.json());
app.use((req, res, next) => {
    req.user = { _id: 'mockUserId', role: 'outlet_manager' };
    next();
});
app.use('/api/inventory', inventoryRoutes);

beforeAll(async () => await db.connect());
afterEach(async () => await db.clear());
afterAll(async () => await db.close());

describe('Inventory Module', () => {

    it('should submit inventory and calculate variance', async () => {
        const res = await request(app)
            .post('/api/inventory/submit')
            .send({
                outletId: 'outlet_inv_1',
                items: [
                    { itemId: 'item_1', opening: 100, closing: 90 }
                    // Expected Variance: 100 - 90 = 10
                ]
            });

        expect(res.statusCode).toEqual(201);
        expect(res.body.items[0].variance).toBe(10);
    });

    it('should reject empty submission', async () => {
        const res = await request(app)
            .post('/api/inventory/submit')
            .send({
                outletId: 'outlet_inv_1',
                items: []
            });

        expect(res.statusCode).toEqual(400);
    });
});
