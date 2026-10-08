package com.example.services

import android.content.Context
import android.util.Log
import com.example.database.VideoDatabase
import com.example.models.VideoGeneration
import com.example.network.VideoGenerationService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Background polling and queue orchestration coordinator.
 * Continuously polls API and manages state transitions (Queued -> Processing -> Generating -> Finalizing -> Completed/Failed)
 * for multiple concurrent video generation jobs.
 * Ensures state persistence in Room database and proactive local video caching.
 */
class VideoGenerationPollingManager private constructor(
    private val appContext: Context,
    private val videoService: VideoGenerationService = VideoGenerationService()
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val database = VideoDatabase.getInstance(appContext)
    private val videoDao = database.videoGenerationDao()
    private val userDao = database.userDao()

    // Active polling jobs per video ID to avoid duplicate parallel polls
    private val activePollingJobs = mutableMapOf<String, Job>()
    private val mutex = Mutex()

    private val _isPollingActive = MutableStateFlow(false)
    val isPollingActive: StateFlow<Boolean> = _isPollingActive.asStateFlow()

    private val _pollingStatusMessage = MutableStateFlow<String?>("Polling system ready")
    val pollingStatusMessage: StateFlow<String?> = _pollingStatusMessage.asStateFlow()

    private var masterSupervisorJob: Job? = null

    companion object {
        private const val TAG = "VideoPollingManager"
        const val POLL_INTERVAL_MS = 2500L

        @Volatile
        private var INSTANCE: VideoGenerationPollingManager? = null

        fun getInstance(context: Context): VideoGenerationPollingManager {
            return INSTANCE ?: synchronized(this) {
                val instance = VideoGenerationPollingManager(context.applicationContext)
                INSTANCE = instance
                instance
            }
        }
    }

    init {
        // Start background watcher on initialization
        startBackgroundPollingWorker()
    }

    /**
     * Start background loop that monitors uncompleted jobs in the queue
     * and ensures each job has an active polling/progression coroutine running.
     */
    fun startBackgroundPollingWorker() {
        if (masterSupervisorJob?.isActive == true) return

        masterSupervisorJob = scope.launch {
            _isPollingActive.value = true
            Log.d(TAG, "Master background polling worker started.")

            while (isActive) {
                try {
                    // Check database for pending jobs (Queued, Processing, Generating, Finalizing)
                    val pendingJobs = videoDao.getPendingGenerationsList()
                    pendingJobs.forEach { pendingJob ->
                        ensureJobPolling(pendingJob)
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Error in master polling loop: ${e.message}")
                }

                delay(POLL_INTERVAL_MS)
            }
            _isPollingActive.value = false
        }
    }

    /**
     * Submits and registers a new job into the background polling worker.
     */
    fun enqueueAndTrack(generation: VideoGeneration, imageBase64: String? = null) {
        scope.launch {
            videoDao.insert(generation)
            ensureJobPolling(generation, imageBase64)
        }
    }

    /**
     * Cancels polling for a specific video job.
     */
    fun cancelPollingForJob(jobId: String) {
        scope.launch {
            mutex.withLock {
                activePollingJobs[jobId]?.cancel()
                activePollingJobs.remove(jobId)
            }
        }
    }

    private suspend fun ensureJobPolling(job: VideoGeneration, imageBase64: String? = null) {
        mutex.withLock {
            if (activePollingJobs[job.id]?.isActive == true) {
                return // Already being polled
            }

            val jobCoroutine = scope.launch {
                try {
                    _pollingStatusMessage.value = "Polling updates for job ${job.id.take(8)}..."
                    Log.d(TAG, "Starting poll pipeline for job ${job.id} (${job.status})")

                    videoService.startGenerationFlow(job, imageBase64).collect { updatedJob ->
                        Log.d(TAG, "Job ${updatedJob.id} status transition: ${updatedJob.status} (${updatedJob.progress}%)")
                        
                        // Update Room DB
                        videoDao.update(updatedJob)

                        // If completed or failed, finish job tracking
                        if (updatedJob.isFinished) {
                            if (updatedJob.isSuccess) {
                                userDao.incrementGenerations(updatedJob.userId)
                                _pollingStatusMessage.value = "Job ${updatedJob.id.take(8)} completed successfully!"
                                // Proactively trigger local file caching so the video loads instantaneously in player
                                updatedJob.videoUrl?.let { url ->
                                    scope.launch {
                                        VideoCacheManager.cacheVideo(appContext, url)
                                    }
                                }
                            } else {
                                _pollingStatusMessage.value = "Job ${updatedJob.id.take(8)} ended: ${updatedJob.errorMessage}"
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error while polling job ${job.id}: ${e.message}")
                    val failedJob = job.copy(
                        status = VideoGeneration.STATUS_FAILED,
                        errorMessage = e.message ?: "Polling task error"
                    )
                    videoDao.update(failedJob)
                } finally {
                    mutex.withLock {
                        activePollingJobs.remove(job.id)
                    }
                }
            }

            activePollingJobs[job.id] = jobCoroutine
        }
    }

    fun stopAll() {
        scope.launch {
            mutex.withLock {
                activePollingJobs.values.forEach { it.cancel() }
                activePollingJobs.clear()
            }
            masterSupervisorJob?.cancel()
            _isPollingActive.value = false
        }
    }
}
