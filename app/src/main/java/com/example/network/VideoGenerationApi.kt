package com.example.network

import com.example.models.GenerateVideoRequest
import com.example.models.GenerateVideoResponse
import com.example.models.JobStatusResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

interface VideoGenerationApi {

    @POST("generate")
    suspend fun generateVideo(
        @Header("Authorization") authHeader: String?,
        @Body request: GenerateVideoRequest
    ): Response<GenerateVideoResponse>

    @GET("status/{job_id}")
    suspend fun getJobStatus(
        @Header("Authorization") authHeader: String?,
        @Path("job_id") jobId: String
    ): Response<JobStatusResponse>
}
