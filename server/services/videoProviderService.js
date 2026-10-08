const { v4: uuidv4 } = require('uuid');
const config = require('../config/config');

// In-memory job repository (can be swapped with MongoDB or PostgreSQL)
const jobsMap = new Map();

class VideoProviderService {
  /**
   * Submit job to generation backend/provider
   */
  async submitGeneration({ prompt, negative_prompt, duration, aspect_ratio, resolution, style, camera, fps, image_base64 }) {
    const jobId = 'job_' + uuidv4().substring(0, 12);

    const jobRecord = {
      jobId,
      prompt,
      negative_prompt,
      duration: duration || 30,
      aspect_ratio: aspect_ratio || '16:9',
      resolution: resolution || '1080p',
      style: style || 'Cinematic',
      camera: camera || 'Cinematic',
      fps: fps || 30,
      status: 'queued',
      progress: 10,
      video_url: null,
      thumbnail_url: null,
      createdAt: Date.now()
    };

    jobsMap.set(jobId, jobRecord);

    // Simulate async pipeline stages
    this.advancePipeline(jobId);

    return { jobId, status: 'processing' };
  }

  /**
   * Status checker
   */
  async getStatus(jobId) {
    const job = jobsMap.get(jobId);
    if (!job) {
      return { status: 'failed', error: 'Job not found' };
    }
    return {
      job_id: job.jobId,
      status: job.status,
      progress: job.progress,
      video_url: job.video_url,
      thumbnail_url: job.thumbnail_url
    };
  }

  advancePipeline(jobId) {
    setTimeout(() => {
      const j = jobsMap.get(jobId);
      if (j) { j.status = 'processing'; j.progress = 35; }
    }, 2000);

    setTimeout(() => {
      const j = jobsMap.get(jobId);
      if (j) { j.status = 'generating'; j.progress = 70; }
    }, 5000);

    setTimeout(() => {
      const j = jobsMap.get(jobId);
      if (j) {
        j.status = 'completed';
        j.progress = 100;
        j.video_url = 'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4';
        j.thumbnail_url = 'https://images.unsplash.com/photo-1464822759023-fed622ff2c3b?auto=format&fit=crop&w=800&q=80';
      }
    }, 9000);
  }
}

module.exports = new VideoProviderService();
