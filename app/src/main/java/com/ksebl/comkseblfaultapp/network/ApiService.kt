package com.ksebl.comkseblfaultapp.network

import com.ksebl.comkseblfaultapp.model.request.RegisterDeviceRequest
import com.ksebl.comkseblfaultapp.model.request.ReportFaultRequest
import com.ksebl.comkseblfaultapp.model.request.UpdateNodeRequest
import com.ksebl.comkseblfaultapp.model.response.ApiResponse
import com.ksebl.comkseblfaultapp.model.response.UploadResponse
import com.ksebl.comkseblfaultapp.model.dto.NodeDto
import com.ksebl.comkseblfaultapp.model.dto.FaultDto
import com.ksebl.comkseblfaultapp.model.dto.StatsDto
import okhttp3.MultipartBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface ApiService {
    @POST("/api/v1/devices/register")
    suspend fun registerDevice(@Body req: RegisterDeviceRequest): ApiResponse<kotlin.collections.Map<String, Any>>

    @GET("/api/v1/nodes")
    suspend fun getNodes(): ApiResponse<List<NodeDto>>

    @POST("/api/v1/faults/report")
    suspend fun reportFault(@Body req: ReportFaultRequest): ApiResponse<kotlin.collections.Map<String, Any>>

    @GET("/api/v1/faults")
    suspend fun getFaults(): ApiResponse<List<FaultDto>>

    @POST("/api/v1/nodes/update")
    suspend fun updateNode(@Body req: UpdateNodeRequest): ApiResponse<NodeDto>

    @GET("/api/v1/stats")
    suspend fun getStats(): ApiResponse<StatsDto>

    // Optional upload endpoint
    @Multipart
    @POST("/api/v1/upload")
    suspend fun upload(@Part file: MultipartBody.Part): UploadResponse
}
