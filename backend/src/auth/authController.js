const asyncHandler = require('express-async-handler');
const jwt = require('jsonwebtoken');
const User = require('./userModel');

const generateToken = (id) => {
    return jwt.sign({ id }, process.env.JWT_SECRET, {
        expiresIn: '7d',
    });
};

// @desc    Auth user & get token
// @route   POST /api/auth/login
// @access  Public
const logger = require('../utils/logger');

// ...

const loginUser = asyncHandler(async (req, res) => {
    const { email, password } = req.body;

    const user = await User.findOne({ email });

    if (user && (await user.matchPassword(password))) {
        logger.info(`Login Success: ${email}`, { service: 'auth-service', userId: user._id });
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
        res.status(401);
        throw new Error('Invalid email or password');
    }
});

// @desc    Register a new user
// @route   POST /api/auth/register
// @access  Public (or Protected in future)
const registerUser = asyncHandler(async (req, res) => {
    const { name, email, password, role, branchId } = req.body;

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
