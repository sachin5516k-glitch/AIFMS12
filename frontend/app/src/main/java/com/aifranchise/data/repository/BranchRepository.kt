package com.aifranchise.data.repository

import com.aifranchise.data.remote.ApiService
import com.aifranchise.data.remote.BranchDetailDto
import com.aifranchise.data.remote.ResultState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BranchRepository @Inject constructor(
    private val apiService: ApiService
) {
    fun getBranchDetails(id: String): Flow<ResultState<BranchDetailDto>> = flow {
        emit(ResultState.Loading)
        try {
            val response = apiService.getBranchById(id)
            if (response.success && response.data != null) {
                emit(ResultState.Success(response.data))
            } else {
                emit(ResultState.Error(Exception(response.message ?: "Failed to fetch branch details")))
            }
        } catch (e: Exception) {
            emit(ResultState.Error(Exception(com.aifranchise.util.ApiUtils.parseError(e))))
        }
    }.flowOn(Dispatchers.IO)
}
