const logger = require('../utils/logger');

const errorHandler = (err, req, res, next) => {
    const statusCode = res.statusCode === 200 ? 500 : res.statusCode;

    // Structured Error Log
    logger.error(`${statusCode} - ${err.message} - ${req.originalUrl} - ${req.method} - ${req.ip}`, {
        service: 'ai-franchise-backend',
        stack: err.stack,
        userId: req.user ? req.user._id : 'anonymous',
        body: req.body
    });

    res.status(statusCode);
    res.json({
        success: false,
        message: err.message,
        data: process.env.NODE_ENV === 'production' ? null : { stack: err.stack },
    });
};

module.exports = { errorHandler };
