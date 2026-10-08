package com.example.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class GenerateVideoRequest(
    @param:Json(name = "prompt") val prompt: String,
    @param:Json(name = "negative_prompt") val negativePrompt: String? = null,
    @param:Json(name = "duration") val duration: Int = 30,
    @param:Json(name = "aspect_ratio") val aspectRatio: String = "16:9",
    @param:Json(name = "resolution") val resolution: String = "1080p",
    @param:Json(name = "style") val style: String = "Cinematic",
    @param:Json(name = "camera") val camera: String = "Cinematic",
    @param:Json(name = "fps") val fps: Int = 30,
    @param:Json(name = "image_base64") val imageBase64: String? = null
)

@JsonClass(generateAdapter = true)
data class GenerateVideoResponse(
    @param:Json(name = "job_id") val jobId: String,
    @param:Json(name = "status") val status: String,
    @param:Json(name = "message") val message: String? = null
)

@JsonClass(generateAdapter = true)
data class JobStatusResponse(
    @param:Json(name = "job_id") val jobId: String,
    @param:Json(name = "status") val status: String, // queued, processing, generating, finalizing, completed, failed
    @param:Json(name = "progress") val progress: Int? = null,
    @param:Json(name = "video_url") val videoUrl: String? = null,
    @param:Json(name = "thumbnail_url") val thumbnailUrl: String? = null,
    @param:Json(name = "error") val error: String? = null
)
