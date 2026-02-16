const request = require('supertest');
const express = require('express');
const db = require('./utils/db');
const authRoutes = require('../src/auth/authRoutes');
const User = require('../src/auth/userModel');

const app = express();
app.use(express.json());
app.use('/api/auth', authRoutes);

// Mock process.env
process.env.JWT_SECRET = 'testsecret';

beforeAll(async () => await db.connect());
afterEach(async () => await db.clear());
afterAll(async () => await db.close());

describe('Auth Module', () => {

    // 1. Register Logic
    it('should register a new user', async () => {
        const res = await request(app)
            .post('/api/auth/register')
            .send({
                name: 'Test User',
                email: 'test@example.com',
                password: 'password123',
                role: 'owner'
            });

        expect(res.statusCode).toEqual(201);
        expect(res.body).toHaveProperty('token');
        expect(res.body.email).toBe('test@example.com');
    });

    it('should reject duplicate email', async () => {
        await User.create({
            name: 'Existing',
            email: 'duplicate@example.com',
            password: 'pass'
        });

        const res = await request(app)
            .post('/api/auth/register')
            .send({
                name: 'New User',
                email: 'duplicate@example.com',
                password: 'pass'
            });

        expect(res.statusCode).toEqual(400); // Bad Request
    });

    // 2. Login Logic
    it('should login with valid credentials', async () => {
        // Create user first
        await request(app)
            .post('/api/auth/register')
            .send({
                name: 'Login User',
                email: 'login@example.com',
                password: 'password123'
            });

        const res = await request(app)
            .post('/api/auth/login')
            .send({
                email: 'login@example.com',
                password: 'password123'
            });

        expect(res.statusCode).toEqual(200);
        expect(res.body).toHaveProperty('token');
    });

    it('should reject invalid password', async () => {
        await request(app)
            .post('/api/auth/register')
            .send({
                name: 'Login User',
                email: 'login@example.com',
                password: 'password123'
            });

        const res = await request(app)
            .post('/api/auth/login')
            .send({
                email: 'login@example.com',
                password: 'wrongpassword'
            });

        expect(res.statusCode).toEqual(401);
    });
});
