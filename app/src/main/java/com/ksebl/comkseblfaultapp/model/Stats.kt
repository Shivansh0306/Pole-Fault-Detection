package com.ksebl.comkseblfaultapp.model

data class Stats(
    val totalNodes: Int,
    val activeNodes: Int,
    val reportedFaults: Int,
    val resolvedFaults: Int,
    val pendingFaults: Int,
    val faultTypes: Map<String, Int>,
    val faultsByStatus: Map<String, Int>,
    val faultsByPriority: Map<Int, Int>,
    val recentFaults: List<Fault> = emptyList(),
    val lastUpdated: String
)
