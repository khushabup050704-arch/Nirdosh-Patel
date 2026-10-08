const rateLimit = require('express-rate-limit');

// Rate limiter for video generation requests (e.g. 10 requests per 15 minutes per IP)
const generationLimiter = rateLimit({
  windowMs: 15 * 60 * 1000,
  max: 20,
  standardHeaders: true,
  legacyHeaders: false,
  message: {
    error: 'Too many video generation requests created from this IP, please try again after 15 minutes.'
  }
});

module.exports = { generationLimiter };
