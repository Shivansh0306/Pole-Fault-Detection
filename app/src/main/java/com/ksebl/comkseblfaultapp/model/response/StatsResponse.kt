package com.ksebl.comkseblfaultapp.model.response

import com.ksebl.comkseblfaultapp.model.Stats

data class StatsResponse(
    override val success: Boolean,
    override val message: String? = null,
    val data: Stats? = null
) : BaseResponse(success, message)
