package com.ksebl.comkseblfaultapp.model.response

data class RegisterDeviceResponse(
    override val success: Boolean,
    override val message: String? = null,
    val deviceId: String? = null
) : BaseResponse(success, message)
