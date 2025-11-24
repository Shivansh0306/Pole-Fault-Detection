package com.ksebl.comkseblfaultapp.model

data class Fault(
    val id: String,
    val nodeId: String,
    val nodeName: String? = null,
    val faultType: String,
    val description: String,
    val status: String,
    val location: Location,
    val reportedAt: String,
    val resolvedAt: String? = null,
    val imageUrl: String? = null,
    val reportedBy: String? = null,
    val assignedTo: String? = null,
    val priority: Int = 2, // 1: High, 2: Medium, 3: Low
    val notes: List<Note> = emptyList()
)

data class Note(
    val id: String,
    val text: String,
    val createdAt: String,
    val createdBy: String
)
