package com.aifranchise.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aifranchise.data.remote.DashboardSummaryResponse
import com.aifranchise.data.remote.ResultState
import com.aifranchise.data.repository.DashboardRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val repository: DashboardRepository
) : ViewModel() {

    private val _dashboardState = MutableStateFlow<ResultState<com.aifranchise.data.remote.DashboardSummaryResponse>?>(null)
    val dashboardState = _dashboardState.asStateFlow()

    private val _adminDashboardState = MutableStateFlow<ResultState<com.aifranchise.data.remote.AdminDashboardSummaryResponse>?>(null)
    val adminDashboardState = _adminDashboardState.asStateFlow()

    private var lastFetchTime = 0L
    private val CACHE_TTL_MS = 30 * 1000L // 30 seconds

    fun loadDashboardData(forceRefresh: Boolean = false) {
        val currentTime = System.currentTimeMillis()
        if (!forceRefresh && (currentTime - lastFetchTime < CACHE_TTL_MS) && _dashboardState.value is ResultState.Success) {
            return
        }
        
        viewModelScope.launch {
            repository.getDashboardSummary().collect { state ->
                _dashboardState.value = state
                if (state is ResultState.Success) {
                    lastFetchTime = System.currentTimeMillis()
                }
            }
        }
    }

    fun loadAdminDashboardData(forceRefresh: Boolean = false) {
        val currentTime = System.currentTimeMillis()
        if (!forceRefresh && (currentTime - lastFetchTime < CACHE_TTL_MS) && _adminDashboardState.value is ResultState.Success) {
            return
        }
        
        viewModelScope.launch {
            repository.getAdminDashboardSummary().collect { state ->
                _adminDashboardState.value = state
                if (state is ResultState.Success) {
                    lastFetchTime = System.currentTimeMillis()
                }
            }
        }
        
        viewModelScope.launch {
            repository.getBranches().collect { state ->
                _branchesState.value = state
            }
        }
    }

    private val _inventorySummaryState = MutableStateFlow<ResultState<com.aifranchise.data.remote.AdminInventorySummaryResponse>?>(null)
    val inventorySummaryState = _inventorySummaryState.asStateFlow()

    private val _branchesState = MutableStateFlow<ResultState<List<com.aifranchise.data.remote.BranchDto>>?>(null)
    val branchesState = _branchesState.asStateFlow()

    private val _selectedBranchInventoryState = MutableStateFlow<ResultState<List<com.aifranchise.data.remote.InventoryItem>>?>(null)
    val selectedBranchInventoryState = _selectedBranchInventoryState.asStateFlow()

    fun loadBranchInventory(branchId: String) {
        viewModelScope.launch {
            repository.getBranchInventoryItems(branchId).collect { state ->
                _selectedBranchInventoryState.value = state
            }
        }
    }

    fun loadAdminInventorySummary() {
        viewModelScope.launch {
            _inventorySummaryState.value = ResultState.Loading
            
            // Artificial delay to show shimmer if desired, or skip it
            _inventorySummaryState.value = repository.getAdminInventorySummary()
        }
    }
}
