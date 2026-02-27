const express = require('express');
const mongoose = require('mongoose');
const dotenv = require('dotenv');
const cors = require('cors');
const morgan = require('morgan');
const connectDB = require('./config/db');
const { errorHandler } = require('./middleware/errorMiddleware');

// Load env vars
dotenv.config();

// Connect to database only if not in test mode
if (process.env.NODE_ENV !== 'test') {
  connectDB();
}

const app = express();

// Middleware
app.use(express.json());
app.use(require('helmet')()); // Security Headers
const { inputSanitizer } = require('./middleware/securityMiddleware');
app.use(inputSanitizer);
app.use(require('./middleware/requestLogger')); // Structured Request Logging
app.use(cors());

// Health Check Endpoint
app.get('/health', (req, res) => {
  const dbStatus = mongoose.connection.readyState === 1 ? 'Connected' : 'Disconnected';
  res.status(200).json({
    status: 'OK',
    uptime: process.uptime(),
    dbStatus: dbStatus,
    timestamp: new Date().toISOString()
  });
});

// Rate Limiting for Auth
const limiter = require('express-rate-limit')({
  windowMs: 15 * 60 * 1000, // 15 minutes
  max: 100, // Limit each IP to 100 requests per windowMs
  message: 'Too many requests from this IP, please try again later'
});
app.use('/api/auth', limiter);

if (process.env.NODE_ENV === 'development') {
  app.use(morgan('dev'));
}

// Graceful Shutdown
require('./utils/shutdown')(app);

// Routes
app.use('/api/auth', require('./auth/authRoutes'));
app.use('/api/branches', require('./branch/branchRoutes'));
app.use('/api/items', require('./item/itemRoutes'));
app.use('/api/sales', require('./sales/salesRoutes'));
app.use('/api/inventory', require('./inventory/inventoryRoutes'));
app.use('/api/attendance', require('./attendance/attendanceRoutes'));
app.use('/api/ai', require('./ai/aiRoutes'));
const reportRoutes = require('./reports/reportRoutes');
const diagnosticRoutes = require('./routes/diagnosticsRoutes');
app.use('/api/admin', reportRoutes);
app.use('/api/admin', diagnosticRoutes); // Mounts /api/admin/diagnostics
app.use('/api/logs', require('./routes/logRoutes')); // Crash Reporting & Logs

// Start Cron Jobs
if (process.env.NODE_ENV !== 'test') {
  const { startDailyJob } = require('./jobs/daily.job');
  startDailyJob();
}

app.get('/', (req, res) => {
  res.send('API is running...');
});

// Error Handler
app.use(errorHandler);

const PORT = process.env.PORT || 5000;

if (require.main === module) {
  app.listen(PORT, () => {
    console.log(`Server running in ${process.env.NODE_ENV} mode on port ${PORT}`);
  });
}

module.exports = app;
