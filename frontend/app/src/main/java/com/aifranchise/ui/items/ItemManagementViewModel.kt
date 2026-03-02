package com.aifranchise.ui.items

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aifranchise.data.remote.ApiService
import com.aifranchise.data.remote.CreateItemRequest
import com.aifranchise.data.remote.ItemDto
import com.aifranchise.data.remote.ResultState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ItemManagementViewModel @Inject constructor(
    private val apiService: ApiService
) : ViewModel() {

    private val _itemsState = MutableStateFlow<ResultState<List<ItemDto>>?>(null)
    val itemsState = _itemsState.asStateFlow()

    private val _createState = MutableStateFlow<ResultState<ItemDto>?>(null)
    val createState = _createState.asStateFlow()

    fun loadItems() {
        viewModelScope.launch {
            _itemsState.value = ResultState.Loading
            try {
                val response = apiService.getItems()
                if (response.success && response.data != null) {
                    _itemsState.value = ResultState.Success(response.data)
                } else {
                    _itemsState.value = ResultState.Error(Exception(response.message ?: "Failed to load items"))
                }
            } catch (e: Exception) {
                _itemsState.value = ResultState.Error(Exception(com.aifranchise.util.ApiUtils.parseError(e)))
            }
        }
    }

    fun createItem(name: String, category: String, unitPrice: Double, purchaseCost: Double) {
        viewModelScope.launch {
            _createState.value = ResultState.Loading
            try {
                val request = CreateItemRequest(name, category, unitPrice, purchaseCost)
                val response = apiService.createItem(request)
                if (response.success && response.data != null) {
                    _createState.value = ResultState.Success(response.data)
                    loadItems() // Refresh list
                } else {
                    _createState.value = ResultState.Error(Exception(response.message ?: "Failed to create item"))
                }
            } catch (e: Exception) {
                _createState.value = ResultState.Error(Exception(com.aifranchise.util.ApiUtils.parseError(e)))
            }
        }
    }

    fun clearCreateState() {
        _createState.value = null
    }
}
