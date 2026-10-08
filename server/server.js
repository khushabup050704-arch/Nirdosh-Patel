const express = require('express');
const cors = require('cors');
const config = require('./config/config');
const authMiddleware = require('./middleware/auth');
const videoRoutes = require('./routes/videoRoutes');
const userRoutes = require('./routes/userRoutes');

const app = express();

app.use(cors());
app.use(express.json({ limit: '15mb' }));

// Health check endpoint
app.get('/health', (req, res) => {
  res.json({ status: 'healthy', service: 'Nirdosh AI Video Backend', timestamp: new Date() });
});

// Mount authenticated API routes under /v1
app.use('/v1', authMiddleware, videoRoutes);
app.use('/v1/users', authMiddleware, userRoutes);

// Central error handler
app.use((err, req, res, next) => {
  console.error('[Server Error]', err);
  res.status(500).json({ error: err.message || 'Internal server error' });
});

app.listen(config.port, () => {
  console.log(`Nirdosh AI Video backend running on port ${config.port}`);
});
