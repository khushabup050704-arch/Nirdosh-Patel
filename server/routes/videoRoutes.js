const express = require('express');
const router = express.Router();
const videoController = require('../controllers/videoController');
const { generationLimiter } = require('../middleware/rateLimiter');

// POST /generate
router.post('/generate', generationLimiter, videoController.generate);

// GET /status/:job_id
router.get('/status/:job_id', videoController.getStatus);

module.exports = router;
