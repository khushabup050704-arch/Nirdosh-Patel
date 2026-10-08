class VideoGeneration {
  constructor({
    id,
    userId,
    prompt,
    negativePrompt = '',
    duration = 30,
    aspectRatio = '16:9',
    resolution = '1080p',
    style = 'Cinematic',
    camera = 'Cinematic',
    fps = 30,
    status = 'queued',
    progress = 0,
    videoUrl = null,
    thumbnailUrl = null,
    errorMessage = null
  }) {
    this.id = id;
    this.userId = userId;
    this.prompt = prompt;
    this.negativePrompt = negativePrompt;
    this.duration = duration;
    this.aspectRatio = aspectRatio;
    this.resolution = resolution;
    this.style = style;
    this.camera = camera;
    this.fps = fps;
    this.status = status; // queued, processing, generating, finalizing, completed, failed
    this.progress = progress;
    this.videoUrl = videoUrl;
    this.thumbnailUrl = thumbnailUrl;
    this.errorMessage = errorMessage;
    this.createdAt = new Date().toISOString();
  }
}

module.exports = VideoGeneration;
