package com.ksebl.comkseblfaultapp.model.dto

import com.squareup.moshi.Json

data class NodeDto(
    val id: Long,
    val latitude: Double,
    val longitude: Double,
    val status: String,
    @Json(name = "last_updated") val lastUpdated: String?
)

data class FaultDto(
    val id: Long,
    @Json(name = "node_id") val nodeId: Long,
    val description: String,
    val confidence: Double?,
    @Json(name = "image_url") val imageUrl: String?,
    @Json(name = "reported_at") val reportedAt: String
)

data class StatsDto(
    @Json(name = "active_faults_count") val activeFaultsCount: Int,
    @Json(name = "total_nodes") val totalNodes: Int,
    @Json(name = "fault_percentage") val faultPercentage: Double
)
