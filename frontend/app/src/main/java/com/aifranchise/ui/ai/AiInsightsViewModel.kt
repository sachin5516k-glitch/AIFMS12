package com.aifranchise.ui.ai

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aifranchise.data.remote.AiInsightsResponse
import com.aifranchise.data.remote.ResultState
import com.aifranchise.data.repository.AiInsightsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AiInsightsViewModel @Inject constructor(
    private val repository: AiInsightsRepository
) : ViewModel() {

    private val _insightsState = MutableStateFlow<ResultState<AiInsightsResponse>?>(null)
    val insightsState = _insightsState.asStateFlow()

    fun loadInsights(outletId: String) {
        viewModelScope.launch {
            repository.getAiInsights(outletId).collect {
                _insightsState.value = it
            }
        }
    }
}
