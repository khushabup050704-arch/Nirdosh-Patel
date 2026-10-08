exports.getProfile = async (req, res) => {
  res.json({
    id: req.user.id || 'user_default',
    name: req.user.name || 'Creator',
    email: req.user.email || 'creator@nirdoshvideo.ai',
    credits: 50,
    totalGenerations: 1
  });
};

exports.addCredits = async (req, res) => {
  const { amount } = req.body;
  res.json({
    message: `Added ${amount || 50} credits`,
    newBalance: 100
  });
};
