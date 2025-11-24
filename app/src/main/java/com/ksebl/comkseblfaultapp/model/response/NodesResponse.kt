package com.ksebl.comkseblfaultapp.model.response

import com.ksebl.comkseblfaultapp.model.Node

data class NodesResponse(
    override val success: Boolean,
    override val message: String? = null,
    val data: List<Node> = emptyList()
) : BaseResponse(success, message)
