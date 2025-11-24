package com.ksebl.comkseblfaultapp.model.response

data class UploadResponse(
    val status: String,
    val message: String?,
    val data: UploadedData?
)

data class UploadedData(
    val url: String
)


