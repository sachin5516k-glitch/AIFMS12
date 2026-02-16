package com.aifranchise.ui.sales

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aifranchise.data.remote.ResultState
import com.aifranchise.data.remote.SalesRequest
import com.aifranchise.data.remote.SalesResponse
import com.aifranchise.data.repository.SalesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SalesViewModel @Inject constructor(
    private val repository: SalesRepository
) : ViewModel() {

    private val _salesState = MutableStateFlow<ResultState<SalesResponse>?>(null)
    val salesState = _salesState.asStateFlow()

    fun submitSales(outletId: String, amountStr: String, paymentMode: String, imageUrl: String?) {
        if (amountStr.isBlank()) {
            _salesState.value = ResultState.Error(Exception("Amount is required"))
            return
        }
        
        val amount = amountStr.toDoubleOrNull()
        if (amount == null || amount <= 0) {
            _salesState.value = ResultState.Error(Exception("Invalid amount"))
            return
        }

        if (imageUrl.isNullOrBlank()) {
             _salesState.value = ResultState.Error(Exception("Proof image is required"))
             return
        }

        val request = SalesRequest(outletId, amount, paymentMode, imageUrl)

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
