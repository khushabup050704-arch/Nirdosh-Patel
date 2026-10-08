const videoProviderService = require('../services/videoProviderService');

exports.generate = async (req, res) => {
  try {
    const { prompt, negative_prompt, duration, aspect_ratio, resolution, style, camera, fps, image_base64 } = req.body;

    if (!prompt || typeof prompt !== 'string' || prompt.trim().length === 0) {
      return res.status(400).json({ error: 'Valid prompt string is required' });
    }

    const result = await videoProviderService.submitGeneration({
      prompt,
      negative_prompt,
      duration,
      aspect_ratio,
      resolution,
      style,
      camera,
      fps,
      image_base64
    });

    res.status(200).json({
      job_id: result.jobId,
      status: result.status,
      message: 'Video generation queued'
    });
  } catch (err) {
    res.status(500).json({ error: err.message || 'Internal server error' });
  }
};

exports.getStatus = async (req, res) => {
  try {
    const { job_id } = req.params;
    if (!job_id) {
      return res.status(400).json({ error: 'job_id parameter is required' });
    }

    const status = await videoProviderService.getStatus(job_id);
    res.status(200).json(status);
  } catch (err) {
    res.status(500).json({ error: err.message || 'Internal server error' });
  }
};
