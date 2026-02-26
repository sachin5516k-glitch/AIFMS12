package com.aifranchise.data.repository

import com.aifranchise.data.remote.ApiService
import com.aifranchise.data.remote.ResultState
import com.aifranchise.data.remote.SalesRequest
import com.aifranchise.data.remote.SalesResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

class SalesRepository @Inject constructor(
    private val apiService: ApiService
) {
    suspend fun submitSales(request: SalesRequest): Flow<ResultState<SalesResponse>> = flow {
        emit(ResultState.Loading)
        try {
            val response = apiService.submitSales(request)
            if (response.success) {
                emit(ResultState.Success(response.data))
            } else {
                emit(ResultState.Error(Exception(response.message)))
            }
        } catch (e: Exception) {
            emit(ResultState.Error(Exception(com.aifranchise.util.ApiUtils.parseError(e))))
        }
    }.flowOn(Dispatchers.IO)
}
