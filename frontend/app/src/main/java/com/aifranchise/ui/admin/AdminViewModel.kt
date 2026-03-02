package com.aifranchise.ui.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aifranchise.data.remote.AddManagerRequest
import com.aifranchise.data.remote.BranchDto
import com.aifranchise.data.remote.ManagerDto
import com.aifranchise.data.remote.ResultState
import com.aifranchise.data.repository.AdminRepository
import com.aifranchise.data.repository.DashboardRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AdminViewModel @Inject constructor(
    private val adminRepository: AdminRepository,
    private val dashboardRepository: DashboardRepository
) : ViewModel() {

    private val _managers = MutableStateFlow<ResultState<List<ManagerDto>>>(ResultState.Loading)
    val managers: StateFlow<ResultState<List<ManagerDto>>> = _managers.asStateFlow()

    private val _branches = MutableStateFlow<ResultState<List<BranchDto>>>(ResultState.Loading)
    val branches: StateFlow<ResultState<List<BranchDto>>> = _branches.asStateFlow()

    private val _addManagerState = MutableStateFlow<ResultState<ManagerDto>?>(null)
    val addManagerState: StateFlow<ResultState<ManagerDto>?> = _addManagerState.asStateFlow()

    private val _deactivateState = MutableStateFlow<ResultState<ManagerDto>?>(null)
    val deactivateState: StateFlow<ResultState<ManagerDto>?> = _deactivateState.asStateFlow()

    fun fetchManagers() {
        viewModelScope.launch {
            adminRepository.getManagers().collect { state ->
                _managers.value = state
            }
        }
    }

    fun fetchBranches() {
        viewModelScope.launch {
            dashboardRepository.getBranches().collect { state ->
                _branches.value = state
            }
        }
    }

    fun addManager(request: AddManagerRequest) {
        viewModelScope.launch {
            adminRepository.addManager(request).collect { state ->
                _addManagerState.value = state
                if (state is ResultState.Success) {
                    fetchManagers() // refresh list
                }
            }
        }
    }

    fun deactivateManager(id: String) {
        viewModelScope.launch {
            adminRepository.deactivateManager(id).collect { state ->
                _deactivateState.value = state
                if (state is ResultState.Success) {
                    fetchManagers() // refresh list
                }
            }
        }
    }

    fun clearAddManagerState() {
        _addManagerState.value = null
    }

    fun clearDeactivateState() {
        _deactivateState.value = null
    }
}
