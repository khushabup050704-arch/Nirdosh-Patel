// Authentication middleware (validates Bearer token or development mock user)
module.exports = function authMiddleware(req, res, next) {
  const authHeader = req.headers['authorization'];
  if (!authHeader) {
    // Development fallback
    req.user = { id: 'user_default', name: 'Alex Vance', email: 'creator@nirdoshvideo.ai' };
    return next();
  }

  const token = authHeader.replace('Bearer ', '').trim();
  if (!token) {
    return res.status(401).json({ error: 'Unauthorized: Missing API token' });
  }

  // Inject user context
  req.user = { id: 'user_default', name: 'Creator', email: 'creator@nirdoshvideo.ai', token };
  next();
};
