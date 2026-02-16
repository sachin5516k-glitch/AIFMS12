package com.aifranchise.ui.inventory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aifranchise.data.remote.InventoryItem
import com.aifranchise.data.remote.InventoryRequest
import com.aifranchise.data.remote.InventoryResponse
import com.aifranchise.data.remote.InventoryUpdateItem
import com.aifranchise.data.remote.ResultState
import com.aifranchise.data.repository.InventoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class InventoryViewModel @Inject constructor(
    private val repository: InventoryRepository
) : ViewModel() {

    private val _itemsState = MutableStateFlow<ResultState<List<InventoryItem>>?>(null)
    val itemsState = _itemsState.asStateFlow()

    private val _submissionState = MutableStateFlow<ResultState<InventoryResponse>?>(null)
    val submissionState = _submissionState.asStateFlow()

    fun loadItems() {
        viewModelScope.launch {
            repository.getInventoryItems().collect {
                _itemsState.value = it
            }
        }
    }

    fun submitInventory(outletId: String, updates: List<InventoryUpdateItem>) {
        if (updates.isEmpty()) {
             _submissionState.value = ResultState.Error(Exception("No items to update"))
             return
        }
        
        // Validation: Check for negative values
        val hasInvalidValues = updates.any { it.opening < 0 || it.closing < 0 }
        if (hasInvalidValues) {
            _submissionState.value = ResultState.Error(Exception("Stock cannot be negative"))
            return
        }

        viewModelScope.launch {
            repository.submitInventory(InventoryRequest(outletId, updates)).collect {
                _submissionState.value = it
            }
        }
    }
}
