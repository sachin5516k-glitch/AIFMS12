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
            emit(ResultState.Success(response))
        } catch (e: Exception) {
            emit(ResultState.Error(e))
        }
    }.flowOn(Dispatchers.IO)
}
