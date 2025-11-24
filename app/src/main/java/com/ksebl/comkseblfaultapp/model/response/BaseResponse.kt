package com.ksebl.comkseblfaultapp.model.response

open class BaseResponse(
    open val success: Boolean,
    open val message: String? = null
)
