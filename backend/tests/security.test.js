const request = require('supertest');
const app = require('../src/server'); // Adjust path as needed
const mongoose = require('mongoose');
const { MongoMemoryServer } = require('mongodb-memory-server');

let mongoServer;

beforeAll(async () => {
    mongoServer = await MongoMemoryServer.create();
    const uri = mongoServer.getUri();
    await mongoose.connect(uri);
});

afterAll(async () => {
    await mongoose.disconnect();
    await mongoServer.stop();
});

describe('Security Vulnerability Tests', () => {

    describe('NoSQL Injection', () => {
        test('Should reject login with NoSQL injection payload', async () => {
            const res = await request(app)
                .post('/api/auth/login')
                .send({
                    email: { "$gt": "" },
                    password: { "$gt": "" }
                });

            // Should be 400 (Bad Request) or 401 (Unauthorized), NOT 200
            expect(res.status).not.toBe(200);
            expect(res.status).toBeGreaterThanOrEqual(400);
        });
    });

    describe('XSS Protection', () => {
        let token;
        // Mock a user creation if needed or just test a public endpoint that reflects input

        test('Should sanitize input containing script tags', async () => {
            // Assuming there's an endpoint that accepts data, e.g., sales
            // We can check if the input is sanitized or rejected
            // For now, checking if the server accepts it but sanitizes it would require inspecting the DB
            // Instead, we check if the server rejects potentially malicious input if validations are strict

            // Simulating a payload to a hypothetically vulnerable endpoint
            // If we don't have a specific reflection point, we rely on input validation rejection
            const res = await request(app)
                .post('/api/auth/register') // Assuming register validation
                .send({
                    name: "<script>alert('XSS')</script>",
                    email: "xss@test.com",
                    password: "password123",
                    role: "owner"
                });

            // If input validation is good, this might fail or strip tags
            // Ideally we want 400 Bad Request if it detects malicious chars
            // Or 201 created but with sanitized name. 
            // For strict security, we expect rejection or sanitization.

            if (res.status === 201) {
                expect(res.body.name).not.toContain("<script>");
            } else {
                expect(res.status).toBeGreaterThanOrEqual(400);
            }
        });
    });

    describe('Authentication Bypass', () => {
        test('Should deny access to protected route without token', async () => {
            const res = await request(app).get('/api/sales/history');
            expect(res.status).toBe(401);
        });

        test('Should deny access with malformed token', async () => {
            const res = await request(app)
                .get('/api/sales/history')
                .set('Authorization', 'Bearer malformed.token.here');
            expect(res.status).toBe(401); // or 403
        });
    });
});
