package com.ksebl.comkseblfaultapp.repository

import com.ksebl.comkseblfaultapp.model.request.RegisterDeviceRequest
import com.ksebl.comkseblfaultapp.model.request.ReportFaultRequest
import com.ksebl.comkseblfaultapp.model.request.UpdateNodeRequest
import com.ksebl.comkseblfaultapp.model.dto.FaultDto
import com.ksebl.comkseblfaultapp.model.dto.NodeDto
import com.ksebl.comkseblfaultapp.model.dto.StatsDto
import com.ksebl.comkseblfaultapp.model.Fault
import com.ksebl.comkseblfaultapp.model.Node
import okhttp3.MultipartBody
import com.ksebl.comkseblfaultapp.network.ApiService
import com.ksebl.comkseblfaultapp.BuildConfig

class MainRepository(private val api: ApiService) {
    suspend fun registerDevice(token: String, role: String) = api.registerDevice(
        RegisterDeviceRequest(
            fcmToken = token,
            role = role.lowercase()
        )
    )
    suspend fun getNodes(): List<Node> = api.getNodes().data.orEmpty().map { it.toUi() }
    suspend fun reportFault(req: ReportFaultRequest) = api.reportFault(req)
    suspend fun getFaults(): List<Fault> = api.getFaults().data.orEmpty()
        .sortedByDescending { it.reportedAt }
        .map { it.toUi() }
    suspend fun updateNode(req: UpdateNodeRequest): Node {
        val response = api.updateNode(req)
        val payload = response.data ?: throw Exception(response.message ?: "Empty response")
        return payload.toUi()
    }

    suspend fun getStats(): StatsDto {
        val response = api.getStats()
        return response.data ?: throw Exception(response.message ?: "Empty response")
    }

    suspend fun uploadImage(part: MultipartBody.Part): String? = toAbsoluteUrlIfRelative(api.upload(part).data?.url)
}

private fun toAbsoluteUrlIfRelative(relativeOrAbsolute: String?): String? {
    if (relativeOrAbsolute.isNullOrBlank()) return relativeOrAbsolute
    val url = relativeOrAbsolute
    return if (url.startsWith("http://") || url.startsWith("https://")) {
        url
    } else {
        // BuildConfig.API_BASE_URL ends with '/'. Ensure we don't double it.
        val base = BuildConfig.API_BASE_URL
        val trimmed = if (url.startsWith("/")) url.removePrefix("/") else url
        base + trimmed
    }
}

private fun NodeDto.toUi(): Node = Node(
    id = this.id.toString(),
    name = "Node ${this.id}",
    location = com.ksebl.comkseblfaultapp.model.Location(
        latitude = this.latitude,
        longitude = this.longitude,
        address = null
    ),
    status = this.status,
    lastMaintained = this.lastUpdated,
    notes = null,
    type = null
)

private fun FaultDto.toUi(): Fault = Fault(
    id = this.id.toString(),
    nodeId = this.nodeId.toString(),
    nodeName = null,
    faultType = "MANUAL",
    description = this.description,
    status = "ACTIVE",
    location = com.ksebl.comkseblfaultapp.model.Location(
        latitude = 0.0,
        longitude = 0.0,
        address = null
    ),
    reportedAt = this.reportedAt,
    resolvedAt = null,
    imageUrl = toAbsoluteUrlIfRelative(this.imageUrl),
    reportedBy = null,
    assignedTo = null,
    priority = 2,
    notes = emptyList()
)
