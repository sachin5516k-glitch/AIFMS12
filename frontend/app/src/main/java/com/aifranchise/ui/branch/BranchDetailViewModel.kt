package com.aifranchise.ui.branch

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aifranchise.data.remote.BranchDetailDto
import com.aifranchise.data.remote.ResultState
import com.aifranchise.data.repository.BranchRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BranchDetailViewModel @Inject constructor(
    private val branchRepository: BranchRepository
) : ViewModel() {

    private val _branchState = MutableStateFlow<ResultState<BranchDetailDto>>(ResultState.Loading)
    val branchState: StateFlow<ResultState<BranchDetailDto>> = _branchState.asStateFlow()

    fun fetchBranchDetails(id: String) {
        viewModelScope.launch {
            branchRepository.getBranchDetails(id).collect {
                _branchState.value = it
            }
        }
    }
}
