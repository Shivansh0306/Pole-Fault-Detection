package com.ksebl.comkseblfaultapp.model.response

import com.ksebl.comkseblfaultapp.model.Fault

data class FaultsResponse(
    override val success: Boolean,
    override val message: String? = null,
    val data: List<Fault> = emptyList()
) : BaseResponse(success, message)
