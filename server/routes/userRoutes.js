const express = require('express');
const router = express.Router();
const userController = require('../controllers/userController');

router.get('/profile', userController.getProfile);
router.post('/credits/add', userController.addCredits);

module.exports = router;
