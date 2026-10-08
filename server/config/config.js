require('dotenv').config();

module.exports = {
  port: process.env.PORT || 3000,
  providerApiKey: process.env.VIDEO_PROVIDER_API_KEY || '',
  providerApiUrl: process.env.VIDEO_PROVIDER_API_URL || 'https://api.openai.com/v1/video/generations',
  jwtSecret: process.env.JWT_SECRET || 'nirdosh-secret-key-12345',
  creditsPerDuration: {
    30: 2,
    60: 4,
    180: 8,
    300: 12
  }
};
