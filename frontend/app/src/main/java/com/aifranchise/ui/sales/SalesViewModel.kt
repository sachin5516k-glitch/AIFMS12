package com.aifranchise.ui.sales

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aifranchise.data.remote.ApiService
import com.aifranchise.data.remote.ItemDto
import com.aifranchise.data.remote.ResultState
import com.aifranchise.data.remote.SalesRequest
import com.aifranchise.data.remote.SalesResponse
import com.aifranchise.data.repository.SalesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SalesViewModel @Inject constructor(
    private val repository: SalesRepository,
    private val apiService: ApiService
) : ViewModel() {

    private val _salesState = MutableStateFlow<ResultState<SalesResponse>?>(null)
    val salesState = _salesState.asStateFlow()

    private val _itemsState = MutableStateFlow<ResultState<List<ItemDto>>?>(null)
    val itemsState = _itemsState.asStateFlow()

    fun loadItems() {
        viewModelScope.launch(Dispatchers.IO) {
            _itemsState.value = ResultState.Loading
            try {
                val response = apiService.getItems()
                if (response.success && response.data != null) {
                    _itemsState.value = ResultState.Success(response.data)
                } else {
                    _itemsState.value = ResultState.Error(Exception(response.message))
                }
            } catch (e: Exception) {
                _itemsState.value = ResultState.Error(Exception(com.aifranchise.util.ApiUtils.parseError(e)))
            }
        }
    }

    fun submitSales(itemId: String, quantitySold: Int, paymentMode: String) {
        if (itemId.isBlank()) {
            _salesState.value = ResultState.Error(Exception("Please select an item"))
            return
        }
        if (quantitySold < 1) {
            _salesState.value = ResultState.Error(Exception("Quantity must be at least 1"))
            return
        }
        if (paymentMode.isBlank()) {
            _salesState.value = ResultState.Error(Exception("Please select a payment method"))
            return
        }

        val request = SalesRequest(itemId, quantitySold, paymentMode)

        viewModelScope.launch {
            repository.submitSales(request).collect {
                _salesState.value = it
            }
        }
    }
    
    fun resetState() {
        _salesState.value = null
    }
}
