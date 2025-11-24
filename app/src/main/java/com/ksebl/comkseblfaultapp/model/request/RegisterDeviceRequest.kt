package com.ksebl.comkseblfaultapp.model.request

import com.squareup.moshi.Json

data class RegisterDeviceRequest(
    @Json(name = "fcm_token") val fcmToken: String,
    @Json(name = "role") val role: String
)
