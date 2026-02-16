package com.aifranchise.data.repository

import com.aifranchise.data.remote.ApiService
import com.aifranchise.data.remote.InventoryItem
import com.aifranchise.data.remote.InventoryRequest
import com.aifranchise.data.remote.InventoryResponse
import com.aifranchise.data.remote.ResultState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

class InventoryRepository @Inject constructor(
    private val apiService: ApiService
) {
    suspend fun getInventoryItems(): Flow<ResultState<List<InventoryItem>>> = flow {
        emit(ResultState.Loading)
        try {
            val items = apiService.getInventoryItems()
            emit(ResultState.Success(items))
        } catch (e: Exception) {
            emit(ResultState.Error(e))
        }
    }.flowOn(Dispatchers.IO)

    suspend fun submitInventory(request: InventoryRequest): Flow<ResultState<InventoryResponse>> = flow {
        emit(ResultState.Loading)
        try {
            val response = apiService.submitInventory(request)
            emit(ResultState.Success(response))
        } catch (e: Exception) {
            emit(ResultState.Error(e))
        }
    }.flowOn(Dispatchers.IO)
}
