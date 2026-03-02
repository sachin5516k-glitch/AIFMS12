package com.aifranchise.ui.transfer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aifranchise.data.remote.BranchDto
import com.aifranchise.data.remote.InventoryItem
import com.aifranchise.data.remote.ManualTransferRequest
import com.aifranchise.data.remote.ResultState
import com.aifranchise.data.remote.TransferRecommendationDto
import com.aifranchise.data.repository.DashboardRepository
import com.aifranchise.data.repository.InventoryRepository
import com.aifranchise.data.repository.TransferRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TransferViewModel @Inject constructor(
    private val transferRepository: TransferRepository,
    private val dashboardRepository: DashboardRepository,
    private val inventoryRepository: InventoryRepository
) : ViewModel() {

    private val _branchesState = MutableStateFlow<ResultState<List<BranchDto>>>(ResultState.Loading)
    val branchesState: StateFlow<ResultState<List<BranchDto>>> = _branchesState.asStateFlow()

    private val _itemsState = MutableStateFlow<ResultState<List<InventoryItem>>>(ResultState.Loading)
    val itemsState: StateFlow<ResultState<List<InventoryItem>>> = _itemsState.asStateFlow()

    private val _transferState = MutableStateFlow<ResultState<TransferRecommendationDto>?>(null)
    val transferState: StateFlow<ResultState<TransferRecommendationDto>?> = _transferState.asStateFlow()

    fun loadFormPrerequisites() {
        viewModelScope.launch {
            dashboardRepository.getBranches().collect {
                _branchesState.value = it
            }
        }
        viewModelScope.launch {
            inventoryRepository.getInventoryItems().collect {
                _itemsState.value = it
            }
        }
    }

    fun submitManualTransfer(fromBranchId: String, toBranchId: String, itemId: String, quantity: Int) {
        viewModelScope.launch {
            transferRepository.createManualTransfer(
                ManualTransferRequest(fromBranchId, toBranchId, itemId, quantity)
            ).collect {
                _transferState.value = it
            }
        }
    }

    fun clearTransferState() {
        _transferState.value = null
    }
}
