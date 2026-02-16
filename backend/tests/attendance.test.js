const request = require('supertest');
const express = require('express');
const db = require('./utils/db');
const attendanceRoutes = require('../src/attendance/attendanceRoutes');
const Attendance = require('../src/attendance/attendanceModel');

const app = express();
app.use(express.json());
app.use((req, res, next) => {
    req.user = { _id: 'mockUserId', role: 'outlet_manager' }; // Mock Auth
    next();
});
app.use('/api/attendance', attendanceRoutes);

beforeAll(async () => await db.connect());
afterEach(async () => await db.clear());
afterAll(async () => await db.close());

describe('Attendance Module', () => {

    it('should check in successfully', async () => {
        const res = await request(app)
            .post('/api/attendance/checkin')
            .send({
                outletId: 'outlet_att_1',
                latitude: 12.9716,
                longitude: 77.5946,
                imageUrl: 'http://example.com/selfie.jpg'
            });

        expect(res.statusCode).toEqual(201);
        expect(res.body.status).toBe('present');
    });

    it('should prevent double check-in', async () => {
        // First Check-in
        await request(app)
            .post('/api/attendance/checkin')
            .send({
                outletId: 'outlet_att_1',
                latitude: 12.9716,
                longitude: 77.5946,
                imageUrl: 'img1'
            });

        // Second Check-in
        const res = await request(app)
            .post('/api/attendance/checkin')
            .send({
                outletId: 'outlet_att_1',
                latitude: 12.9716,
                longitude: 77.5946,
                imageUrl: 'img2'
            });

        expect(res.statusCode).toEqual(400);
        expect(res.body.message).toContain('already checked in');
    });

    it('should check out successfully', async () => {
        // Prerequisite: Check-in
        await request(app)
            .post('/api/attendance/checkin')
            .send({
                outletId: 'outlet_att_1',
                latitude: 12.9716,
                longitude: 77.5946,
                imageUrl: 'img1'
            });

        const res = await request(app)
            .post('/api/attendance/checkout')
            .send({
                outletId: 'outlet_att_1'
            });

        expect(res.statusCode).toEqual(200);
        expect(res.body.checkOutTime).toBeDefined();
    });
});
