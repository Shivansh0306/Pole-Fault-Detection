package com.ksebl.comkseblfaultapp.model.request

import com.squareup.moshi.Json

data class ReportFaultRequest(
    @Json(name = "node_id") val nodeId: Long,
    val description: String,
    val confidence: Double,
    @Json(name = "image_url") val imageUrl: String? = null
)
