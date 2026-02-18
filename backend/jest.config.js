module.exports = {
    testEnvironment: 'node',
    testMatch: ['**/*.test.js'],
    verbose: true,
    forceExit: true,
    clearMocks: true,
    resetMocks: true,
    restoreMocks: true,
    testTimeout: 30000,
    collectCoverageFrom: [
        "src/**/*.js",
        "!src/server.js",
        "!src/config/db.js"
    ],
    coverageThreshold: {
        global: {
            branches: 80,
            functions: 80,
            lines: 80,
            statements: 80
        }
    }
};
