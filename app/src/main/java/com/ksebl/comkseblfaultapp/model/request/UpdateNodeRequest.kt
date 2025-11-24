package com.ksebl.comkseblfaultapp.model.request

import com.squareup.moshi.Json

data class UpdateNodeRequest(
    @Json(name = "node_id") val nodeId: Long,
    val status: String
)
