const db = require('./utils/db');
const Sales = require('../src/sales/salesModel');
const { calculateFraudScore } = require('../src/ai/fraud.engine.js'); // Testing actual logic, not mock

beforeAll(async () => await db.connect());
afterEach(async () => await db.clear());
afterAll(async () => await db.close());

describe('AI Fraud Engine', () => {

    it('should assign high fraud score for massive amount', async () => {
        const result = await calculateFraudScore({
            outletId: 'outlet_test',
            amount: 150000, // > 100k
            paymentMode: 'Cash'
        });

        expect(result.score).toBeGreaterThanOrEqual(50); // 40 (High Amount) + 10 (Large Cash)
        expect(result.reasons).toContain('Unusually high transaction amount');
    });

    it('should detect deviation from history', async () => {
        // Seed history: 10 sales of 1000
        for (let i = 0; i < 10; i++) {
            await Sales.create({
                outletId: 'outlet_history',
                amount: 1000,
                paymentMode: 'Cash',
                createdAt: new Date(Date.now() - 24 * 60 * 60 * 1000) // Yesterday
            });
        }

        // Test abnormal sale: 5000 (5x average)
        const result = await calculateFraudScore({
            outletId: 'outlet_history',
            amount: 5000,
            paymentMode: 'Card'
        });

        expect(result.score).toBeGreaterThan(0);
        expect(result.reasons).toContain('Amount exceeds 3x weekly average');
    });
});
