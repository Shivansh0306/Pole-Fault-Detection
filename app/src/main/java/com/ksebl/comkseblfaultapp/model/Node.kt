package com.ksebl.comkseblfaultapp.model

data class Node(
    val id: String,
    val name: String,
    val location: Location,
    val status: String,
    val lastMaintained: String? = null,
    val notes: String? = null,
    val type: String? = null
)

data class Location(
    val latitude: Double,
    val longitude: Double,
    val address: String? = null
)
