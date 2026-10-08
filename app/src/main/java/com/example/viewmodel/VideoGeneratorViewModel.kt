package com.example.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.BuildConfig
import com.example.database.VideoDatabase
import com.example.models.AspectRatio
import com.example.models.CameraMotion
import com.example.models.CreditTransaction
import com.example.models.DurationOption
import com.example.models.FpsOption
import com.example.models.GenerationMode
import com.example.models.QualityOption
import com.example.models.StyleOption
import com.example.models.User
import com.example.models.VideoGeneration
import com.example.network.VideoGenerationService
import com.example.services.ContentModerator
import com.example.services.CreditManager
import com.example.services.PromptEnhancer
import com.example.services.VideoCacheManager
import com.example.services.VideoGenerationPollingManager
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

class VideoGeneratorViewModel(application: Application) : AndroidViewModel(application) {

    private val database = VideoDatabase.getInstance(application)
    private val videoDao = database.videoGenerationDao()
    private val creditDao = database.creditTransactionDao()
    private val userDao = database.userDao()

    // Configuration
    val customApiUrl = MutableStateFlow(
        runCatching { BuildConfig.VIDEO_API_URL }.getOrNull() ?: "https://api.nirdoshvideo.ai/v1"
    )
    val customApiKey = MutableStateFlow(
        runCatching { BuildConfig.VIDEO_API_KEY }.getOrNull() ?: ""
    )

    private var videoService = VideoGenerationService(customApiUrl.value, customApiKey.value)
    private val pollingManager = VideoGenerationPollingManager.getInstance(application)

    // Polling System State
    val isPollingActive: StateFlow<Boolean> = pollingManager.isPollingActive
    val pollingStatusMessage: StateFlow<String?> = pollingManager.pollingStatusMessage

    // User & Data Flows
    val currentUser: StateFlow<User?> = userDao.getUserFlow("user_default")
        .stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            User(id = "user_default", name = "Alex Vance", email = "creator@nirdoshvideo.ai", credits = 50, totalGenerations = 1)
        )

    val allGenerations: StateFlow<List<VideoGeneration>> = videoDao.getAllGenerations()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val recentGenerations: StateFlow<List<VideoGeneration>> = videoDao.getRecentGenerations(6)
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // Visual Queue of pending & in-progress jobs
    val pendingQueueJobs: StateFlow<List<VideoGeneration>> = videoDao.getPendingGenerationsFlow()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // Form State
    val prompt = MutableStateFlow("")
    val negativePrompt = MutableStateFlow("")
    val aspectRatio = MutableStateFlow(AspectRatio.RATIO_16_9)
    val duration = MutableStateFlow(DurationOption.SEC_30)
    val quality = MutableStateFlow(QualityOption.Q_1080P)
    val style = MutableStateFlow(StyleOption.CINEMATIC)
    val camera = MutableStateFlow(CameraMotion.CINEMATIC)
    val fps = MutableStateFlow(FpsOption.FPS_30)
    val generationMode = MutableStateFlow(GenerationMode.TEXT_TO_VIDEO)
    val selectedImageUri = MutableStateFlow<Uri?>(null)

    // Active Generation Tracking
    val activeGeneration = MutableStateFlow<VideoGeneration?>(null)
    val isGenerating = MutableStateFlow(false)

    // Video Player Target
    val activePlayingVideo = MutableStateFlow<VideoGeneration?>(null)

    // Dialog & UI Visibility
    private val prefs = application.getSharedPreferences("nirdosh_prefs", android.content.Context.MODE_PRIVATE)
    val showOnboarding = MutableStateFlow(!prefs.getBoolean("has_completed_onboarding", false))

    val showCreditDialog = MutableStateFlow(false)
    val showSettingsSheet = MutableStateFlow(false)
    val showAdminDialog = MutableStateFlow(false)
    val showSafetyDialog = MutableStateFlow(false)

    // Feedback Notifications
    private val _userMessage = MutableSharedFlow<String>()
    val userMessage: SharedFlow<String> = _userMessage.asSharedFlow()

    init {
        // Ensure default user exists
        viewModelScope.launch {
            if (userDao.getUser("user_default") == null) {
                userDao.insertOrUpdate(
                    User(
                        id = "user_default",
                        name = "Alex Vance",
                        email = "creator@nirdoshvideo.ai",
                        credits = 50,
                        totalGenerations = 0,
                        isAdmin = true
                    )
                )
            }
        }
    }

    fun setPrompt(text: String) {
        prompt.value = text
    }

    fun setNegativePrompt(text: String) {
        negativePrompt.value = text
    }

    fun clearPrompt() {
        prompt.value = ""
    }

    fun completeOnboarding(suggestedPrompt: String? = null, mode: GenerationMode? = null) {
        prefs.edit().putBoolean("has_completed_onboarding", true).apply()
        showOnboarding.value = false
        if (!suggestedPrompt.isNullOrBlank()) {
            prompt.value = suggestedPrompt
        }
        if (mode != null) {
            generationMode.value = mode
        }
    }

    fun launchOnboarding() {
        showOnboarding.value = true
    }

    fun enhancePrompt() {
        val current = prompt.value
        if (current.isBlank()) {
            notifyUser("Please enter a basic prompt before enhancing.")
            return
        }
        val enhanced = PromptEnhancer.enhance(current, style.value, camera.value)
        prompt.value = enhanced
        notifyUser("Prompt enhanced with cinematic keywords!")
    }

    fun selectPresetPrompt(preset: String) {
        prompt.value = preset
    }

    fun setAspectRatio(ratio: AspectRatio) {
        aspectRatio.value = ratio
    }

    fun setDuration(dur: DurationOption) {
        duration.value = dur
    }

    fun setQuality(q: QualityOption) {
        quality.value = q
    }

    fun setStyle(s: StyleOption) {
        style.value = s
    }

    fun setCamera(c: CameraMotion) {
        camera.value = c
    }

    fun setFps(f: FpsOption) {
        fps.value = f
    }

    fun setGenerationMode(mode: GenerationMode) {
        generationMode.value = mode
    }

    fun setSelectedImage(uri: Uri?) {
        selectedImageUri.value = uri
    }

    fun startGeneration() {
        val currentPrompt = prompt.value.trim()
        val currentMode = generationMode.value
        val imageUri = selectedImageUri.value

        if (currentPrompt.isEmpty()) {
            notifyUser("Please describe the video you want to create.")
            return
        }

        if (currentMode == GenerationMode.IMAGE_TO_VIDEO && imageUri == null) {
            notifyUser("Please select an image for Image-to-Video generation.")
            return
        }

        // 1. Content Moderation Check
        val moderation = ContentModerator.validatePrompt(currentPrompt)
        if (!moderation.isSafe) {
            notifyUser(moderation.reason ?: "Prompt does not meet safety criteria.")
            return
        }

        // 2. Credits Check
        val requiredCredits = CreditManager.calculateRequiredCredits(duration.value)
        val user = currentUser.value
        val availableCredits = user?.credits ?: 0

        if (availableCredits < requiredCredits) {
            showCreditDialog.value = true
            notifyUser("You don't have enough credits ($requiredCredits needed, $availableCredits available).")
            return
        }

        // 3. Prepare Video Generation Entity
        val newGeneration = VideoGeneration(
            id = UUID.randomUUID().toString(),
            userId = user?.id ?: "user_default",
            prompt = currentPrompt,
            negativePrompt = negativePrompt.value.trim(),
            durationSeconds = duration.value.seconds,
            durationLabel = duration.value.label,
            aspectRatio = aspectRatio.value.label,
            resolution = quality.value.label,
            style = style.value.label,
            camera = camera.value.label,
            fps = fps.value.fpsValue,
            status = VideoGeneration.STATUS_QUEUED,
            progress = 10,
            inputImageUri = imageUri?.toString(),
            createdAt = System.currentTimeMillis()
        )

        viewModelScope.launch {
            // Deduct credits & record transaction
            userDao.addCredits(user?.id ?: "user_default", -requiredCredits)
            creditDao.insert(
                CreditTransaction(
                    userId = user?.id ?: "user_default",
                    amount = -requiredCredits,
                    type = "generation",
                    description = "Generated ${duration.value.label} video: \"${currentPrompt.take(25)}...\""
                )
            )

            // Save generation record to DB
            videoDao.insert(newGeneration)
            activeGeneration.value = newGeneration
            isGenerating.value = true

            // Dispatch to background polling manager
            pollingManager.enqueueAndTrack(newGeneration)

            // Also monitor generation flow directly for active UI feedback
            try {
                videoService.startGenerationFlow(newGeneration).collect { stateUpdate ->
                    activeGeneration.value = stateUpdate
                    videoDao.update(stateUpdate)

                    if (stateUpdate.isFinished) {
                        isGenerating.value = false
                        if (stateUpdate.isSuccess) {
                            userDao.incrementGenerations(user?.id ?: "user_default")
                            notifyUser("Video created successfully! Ready to watch.")
                            // Proactively cache video to local disk for instant loading
                            stateUpdate.videoUrl?.let { url ->
                                viewModelScope.launch {
                                    VideoCacheManager.cacheVideo(getApplication(), url)
                                }
                            }
                        } else {
                            notifyUser(stateUpdate.errorMessage ?: "Video generation failed.")
                        }
                    }
                }
            } catch (e: Exception) {
                isGenerating.value = false
                val failedState = newGeneration.copy(
                    status = VideoGeneration.STATUS_FAILED,
                    errorMessage = e.message ?: "Unexpected generation error."
                )
                activeGeneration.value = failedState
                videoDao.update(failedState)
                notifyUser("Generation failed: ${e.message}")
            }
        }
    }

    fun retryGeneration(generation: VideoGeneration) {
        viewModelScope.launch {
            val retried = generation.copy(
                status = VideoGeneration.STATUS_QUEUED,
                progress = 10,
                errorMessage = null
            )
            videoDao.update(retried)
            activeGeneration.value = retried
            isGenerating.value = true

            try {
                videoService.startGenerationFlow(retried).collect { stateUpdate ->
                    activeGeneration.value = stateUpdate
                    videoDao.update(stateUpdate)
                    if (stateUpdate.isFinished) {
                        isGenerating.value = false
                    }
                }
            } catch (e: Exception) {
                isGenerating.value = false
                val failed = retried.copy(
                    status = VideoGeneration.STATUS_FAILED,
                    errorMessage = e.message
                )
                activeGeneration.value = failed
                videoDao.update(failed)
            }
        }
    }

    fun cancelQueuedJob(job: VideoGeneration) {
        viewModelScope.launch {
            // Refund credits proportional to duration
            val requiredCredits = when (job.durationSeconds) {
                30 -> 2
                60 -> 4
                180 -> 8
                300 -> 12
                else -> 2
            }
            val user = currentUser.value
            if (user != null && requiredCredits > 0) {
                userDao.addCredits(user.id, requiredCredits)
                creditDao.insert(
                    CreditTransaction(
                        userId = user.id,
                        amount = requiredCredits,
                        type = "bonus",
                        description = "Refunded cancelled video job"
                    )
                )
            }
            pollingManager.cancelPollingForJob(job.id)
            videoDao.deleteById(job.id)
            if (activeGeneration.value?.id == job.id) {
                activeGeneration.value = null
            }
            notifyUser("Job cancelled & credits refunded.")
        }
    }

    fun deleteGeneration(generationId: String) {
        viewModelScope.launch {
            videoDao.deleteById(generationId)
            if (activeGeneration.value?.id == generationId) {
                activeGeneration.value = null
            }
            if (activePlayingVideo.value?.id == generationId) {
                activePlayingVideo.value = null
            }
            notifyUser("Video removed from history.")
        }
    }

    fun toggleFavorite(generation: VideoGeneration) {
        viewModelScope.launch {
            val updated = !generation.isFavorite
            videoDao.updateFavorite(generation.id, updated)
        }
    }

    fun openVideoPlayer(video: VideoGeneration) {
        activePlayingVideo.value = video
    }

    fun closeVideoPlayer() {
        activePlayingVideo.value = null
    }

    fun saveTrimmedClip(
        originalVideo: VideoGeneration,
        startMs: Int,
        endMs: Int,
        onSuccess: (VideoGeneration) -> Unit = {}
    ) {
        viewModelScope.launch {
            val clipDurationSec = ((endMs - startMs) / 1000).coerceAtLeast(1)
            val startFormatted = String.format("%02d:%02d", (startMs / 1000) / 60, (startMs / 1000) % 60)
            val endFormatted = String.format("%02d:%02d", (endMs / 1000) / 60, (endMs / 1000) % 60)
            val trimmedPrompt = "${originalVideo.prompt} (Clip: ${startFormatted} - ${endFormatted})"

            val trimmedGeneration = VideoGeneration(
                id = UUID.randomUUID().toString(),
                userId = originalVideo.userId,
                prompt = trimmedPrompt,
                negativePrompt = originalVideo.negativePrompt,
                durationSeconds = clipDurationSec,
                durationLabel = "$clipDurationSec seconds (Trimmed)",
                aspectRatio = originalVideo.aspectRatio,
                resolution = originalVideo.resolution,
                style = originalVideo.style,
                camera = originalVideo.camera,
                fps = originalVideo.fps,
                status = VideoGeneration.STATUS_COMPLETED,
                progress = 100,
                videoUrl = originalVideo.videoUrl,
                thumbnailUrl = originalVideo.thumbnailUrl,
                inputImageUri = originalVideo.inputImageUri,
                createdAt = System.currentTimeMillis()
            )

            videoDao.insert(trimmedGeneration)
            notifyUser("Saved ${clipDurationSec}s trimmed clip to Vault!")
            onSuccess(trimmedGeneration)
        }
    }

    fun purchaseCredits(pkg: CreditManager.CreditPackage) {
        viewModelScope.launch {
            val uid = currentUser.value?.id ?: "user_default"
            userDao.addCredits(uid, pkg.credits)
            creditDao.insert(
                CreditTransaction(
                    userId = uid,
                    amount = pkg.credits,
                    type = "purchase",
                    description = "Purchased ${pkg.tag ?: pkg.id.uppercase()} pack (${pkg.credits} credits)"
                )
            )
            showCreditDialog.value = false
            notifyUser("Added ${pkg.credits} credits! Thank you.")
        }
    }

    fun adminGrantCredits(amount: Int) {
        viewModelScope.launch {
            val uid = currentUser.value?.id ?: "user_default"
            userDao.addCredits(uid, amount)
            creditDao.insert(
                CreditTransaction(
                    userId = uid,
                    amount = amount,
                    type = "admin_grant",
                    description = "Admin manual grant: $amount credits"
                )
            )
            notifyUser("Admin credited $amount credits.")
        }
    }

    fun updateApiSettings(url: String, key: String) {
        customApiUrl.value = url.trim()
        customApiKey.value = key.trim()
        videoService = VideoGenerationService(customApiUrl.value, customApiKey.value)
        showSettingsSheet.value = false
        notifyUser("API configuration updated.")
    }

    fun updateUserProfile(name: String, email: String) {
        viewModelScope.launch {
            val current = currentUser.value ?: return@launch
            val updated = current.copy(name = name.trim(), email = email.trim())
            userDao.update(updated)
            notifyUser("Profile updated.")
        }
    }

    fun getLocalCacheSizeFormatted(): String {
        val bytes = VideoCacheManager.getCacheSizeBytes(getApplication())
        return VideoCacheManager.formatCacheSize(bytes)
    }

    fun clearLocalVideoCache() {
        val success = VideoCacheManager.clearCache(getApplication())
        if (success) {
            notifyUser("Video cache cleared.")
        } else {
            notifyUser("Some cache files could not be removed.")
        }
    }

    private fun notifyUser(msg: String) {
        viewModelScope.launch {
            _userMessage.emit(msg)
        }
    }
}
