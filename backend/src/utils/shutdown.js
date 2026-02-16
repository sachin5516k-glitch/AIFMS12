const mongoose = require('mongoose');

const gracefulShutdown = (server) => {
    const shutdown = async () => {
        console.log('\nReceived kill signal, shutting down gracefully');

        server.close(() => {
            console.log('Closed out remaining connections');

            mongoose.connection.close(false, () => {
                console.log('MongoDB connection closed');
                process.exit(0);
            });
        });

        // Force close if taking too long
        setTimeout(() => {
            console.error('Could not close connections in time, forcefully shutting down');
            process.exit(1);
        }, 10000);
    };

    process.on('SIGTERM', shutdown);
    process.on('SIGINT', shutdown);
};

module.exports = gracefulShutdown;
