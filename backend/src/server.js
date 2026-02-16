const express = require('express');
const dotenv = require('dotenv');
const cors = require('cors');
const morgan = require('morgan');
const connectDB = require('./config/db');
const { errorHandler } = require('./middleware/errorMiddleware');

// Load env vars
dotenv.config();

// Connect to database
connectDB();

const app = express();

// Middleware
app.use(express.json());
app.use(require('helmet')()); // Security Headers
app.use(cors());

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
app.use('/api/franchise', require('./franchise/franchiseRoutes'));
app.use('/api/sales', require('./sales/salesRoutes'));
app.use('/api/inventory', require('./inventory/inventoryRoutes'));
app.use('/api/attendance', require('./attendance/attendanceRoutes'));
app.use('/api/ai', require('./ai/aiRoutes'));
app.use('/api/admin', require('./reports/reportRoutes')); // Using /api/admin as base for reports/disputes

// Start Cron Jobs
const { startDailyJob } = require('./jobs/daily.job');
startDailyJob();

app.get('/', (req, res) => {
  res.send('API is running...');
});

// Error Handler
app.use(errorHandler);

const PORT = process.env.PORT || 5000;

app.listen(PORT, () => {
  console.log(`Server running in ${process.env.NODE_ENV} mode on port ${PORT}`);
});
