package com.ksebl.comkseblfaultapp.model.response

data class ReportFaultResponse(
    override val success: Boolean,
    override val message: String? = null,
    val faultId: String? = null
) : BaseResponse(success, message)
