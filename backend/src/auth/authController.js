const asyncHandler = require('express-async-handler');
const jwt = require('jsonwebtoken');
const User = require('./userModel');

const generateToken = (id) => {
    return jwt.sign({ id }, process.env.JWT_SECRET, {
        expiresIn: '7d',
    });
};

const AuditLog = require('../audit/auditLogModel');

// @desc    Auth user & get token
// @route   POST /api/auth/login
// @access  Public
const logger = require('../utils/logger');

// ...

const loginUser = asyncHandler(async (req, res) => {
    let { email, password } = req.body;
    if (email) email = email.toLowerCase();

    const user = await User.findOne({ email });

    if (user && (await user.matchPassword(password))) {
        logger.info(`Login Success: ${email}`, { service: 'auth-service', userId: user._id });

        await AuditLog.create({
            userId: user._id,
            action: 'USER_LOGIN_SUCCESS',
            details: `User ${email} logged in.`,
            ipAddress: req.ip,
            userAgent: req.headers['user-agent']
        });

        res.json({
            success: true,
            message: 'Login successful',
            data: {
                token: generateToken(user._id),
                user: {
                    id: user._id,
                    name: user.name,
                    role: user.role,
                    branchId: user.branchId,
                }
            }
        });
    } else {
        logger.warn(`Login Failed: ${email}`, { service: 'auth-service', ip: req.ip });

        await AuditLog.create({
            userId: user ? user._id : null,
            action: 'USER_LOGIN_FAILED',
            details: `Failed login attempt for email: ${email}`,
            ipAddress: req.ip,
            userAgent: req.headers['user-agent']
        });

        res.status(401);
        throw new Error('Invalid email or password');
    }
});

// @desc    Register a new user
// @route   POST /api/auth/register
// @access  Public (or Protected in future)
const registerUser = asyncHandler(async (req, res) => {
    let { name, email, password, role, branchId } = req.body;
    if (email) email = email.toLowerCase();

    const userExists = await User.findOne({ email });

    if (userExists) {
        res.status(400);
        throw new Error('User already exists');
    }

    const user = await User.create({
        name,
        email,
        password,
        role,
        branchId: role === 'admin' ? null : branchId,
    });

    if (user) {
        await AuditLog.create({
            userId: user._id,
            action: 'ROLE_CREATION',
            details: `New account registered: ${email} with role ${role}. (Created by: ${req.user ? req.user.email : 'Public/Initial'})`,
            ipAddress: req.ip,
            userAgent: req.headers['user-agent']
        });

        res.status(201).json({
            success: true,
            message: 'Registration successful',
            data: {
                token: generateToken(user._id),
                user: {
                    id: user._id,
                    name: user.name,
                    role: user.role,
                    branchId: user.branchId,
                }
            }
        });
    } else {
        res.status(400);
        throw new Error('Invalid user data');
    }
});

module.exports = { loginUser, registerUser };
