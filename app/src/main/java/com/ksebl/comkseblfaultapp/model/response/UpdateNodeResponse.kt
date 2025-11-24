package com.ksebl.comkseblfaultapp.model.response

data class UpdateNodeResponse(
    override val success: Boolean,
    override val message: String? = null
) : BaseResponse(success, message)
