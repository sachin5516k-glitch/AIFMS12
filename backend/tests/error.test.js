const request = require('supertest');
const express = require('express');
const { errorHandler } = require('../src/middleware/errorMiddleware');

const app = express();
app.use(express.json());

// Mock Routes for Error Testing
app.get('/error', (req, res) => {
    throw new Error('Simulated Server Error');
});

// 404 is handled by Express default or custom handler if added. 
// Here we test the errorHandler middleware specifically.
app.use(errorHandler);

describe('Error Handling Middleware', () => {

    it('should catch errors and return 500', async () => {
        const res = await request(app).get('/error');
        expect(res.statusCode).toEqual(500);
        expect(res.body.message).toBe('Simulated Server Error');
    });

    it('should return 404 for unknown routes', async () => {
        const res = await request(app).get('/unknown-route');
        // Express default 404 is HTML usually, unless helper is added.
        // Since we didn't add a "NotFound" middleware in server.js explicitly (only errorHandler), 
        // Express sends 404 default.
        expect(res.statusCode).toEqual(404);
    });
});
