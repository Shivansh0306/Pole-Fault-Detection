package com.ksebl.comkseblfaultapp.model.response

data class ApiResponse<T>(
    val status: String,
    val message: String?,
    val data: T?
)

data class ErrorDetail(
    val detail: String?
)


