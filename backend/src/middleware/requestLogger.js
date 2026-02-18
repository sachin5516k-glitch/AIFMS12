const logger = require('../utils/logger');

const requestLogger = (req, res, next) => {
    const start = Date.now();

    // Log request start
    // logger.info(`Incoming ${req.method} ${req.originalUrl}`);

    res.on('finish', () => {
        const duration = Date.now() - start;
        const message = `${req.method} ${req.originalUrl} ${res.statusCode} ${duration}ms`;

        const meta = {
            method: req.method,
            url: req.originalUrl,
            status: res.statusCode,
            duration: duration,
            userId: req.user ? req.user._id : 'anonymous',
            ip: req.ip
        };

        if (res.statusCode >= 500) {
            logger.error(message, meta);
        } else if (res.statusCode >= 400) {
            logger.warn(message, meta);
        } else {
            logger.info(message, meta);
        }

        // Performance Alert
        if (duration > 500) {
            logger.warn(`Slow Request Detected: ${message}`, { ...meta, alert: true });
        }
    });

    next();
};

module.exports = requestLogger;
