package com.ksebl.comkseblfaultapp.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ksebl.comkseblfaultapp.model.*
import com.ksebl.comkseblfaultapp.model.dto.StatsDto
import com.ksebl.comkseblfaultapp.repository.MainRepository
import com.ksebl.comkseblfaultapp.model.request.ReportFaultRequest
import com.ksebl.comkseblfaultapp.model.request.UpdateNodeRequest
import com.ksebl.comkseblfaultapp.service.FaultNotificationService
import okhttp3.MultipartBody
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MainViewModel(private val repo: MainRepository) : ViewModel() {
    private val _nodes = MutableStateFlow<List<Node>>(emptyList())
    val nodes: StateFlow<List<Node>> = _nodes

    private val _faults = MutableStateFlow<List<Fault>>(emptyList())
    val faults: StateFlow<List<Fault>> = _faults

    private val _stats = MutableStateFlow<StatsDto?>(null)
    val stats: StateFlow<StatsDto?> = _stats

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _newFaultAlert = MutableStateFlow<Fault?>(null)
    val newFaultAlert: StateFlow<Fault?> = _newFaultAlert

    private var lastKnownFaultIds = emptySet<String>()

    fun fetchNodes() = viewModelScope.launch {
        try {
            _nodes.value = repo.getNodes()
        } catch (e: Exception) {
            _error.value = e.message
        }
    }

    fun fetchFaults() = viewModelScope.launch {
        try {
            val newFaults = repo.getFaults()
            
            // Check for new faults to show popup
            val currentFaultIds = newFaults.map { it.id }.toSet()
            val newFaultAlerts = newFaults.filter { fault ->
                fault.id !in lastKnownFaultIds && fault.status == "ACTIVE"
            }
            
            // Show popup for the first new fault
            if (newFaultAlerts.isNotEmpty()) {
                _newFaultAlert.value = newFaultAlerts.first()
            }
            
            lastKnownFaultIds = currentFaultIds
            _faults.value = newFaults
        } catch (e: Exception) {
            _error.value = e.message
        }
    }

    fun fetchStats() = viewModelScope.launch {
        try {
            _stats.value = repo.getStats()
        } catch (e: Exception) {
            _error.value = e.message
        }
    }

    fun reportFault(req: ReportFaultRequest) = viewModelScope.launch {
        try {
            repo.reportFault(req)
            fetchFaults()
        } catch (e: Exception) {
            _error.value = e.message
        }
    }

    fun clearError() {
        _error.value = null
    }

    fun updateNode(req: UpdateNodeRequest) = viewModelScope.launch {
        try {
            repo.updateNode(req)
            fetchNodes()
        } catch (e: Exception) {
            _error.value = e.message
        }
    }

    fun registerDevice(token: String, role: String) = viewModelScope.launch {
        try {
            repo.registerDevice(token, role)
        } catch (e: Exception) {
            e.printStackTrace()
            _error.value = "Device registration failed: ${e.message}"
        }
    }

    suspend fun uploadImage(part: MultipartBody.Part): String? = repo.uploadImage(part)

    fun startFaultMonitoring(context: Context) {
        try {
            FaultNotificationService.start(context)
        } catch (e: Exception) {
            e.printStackTrace()
            _error.value = "Failed to start monitoring service: ${e.message}"
        }
    }

    fun stopFaultMonitoring(context: Context) {
        FaultNotificationService.stop(context)
    }

    fun dismissFaultAlert() {
        _newFaultAlert.value = null
    }

    fun getActiveFaultCount(): Int {
        return _faults.value.count { it.status == "ACTIVE" }
    }

    fun markAllFaultsAsRead() {
        // This would typically update read status in the backend
        // For now, we'll just dismiss any active alert
        _newFaultAlert.value = null
    }
}
