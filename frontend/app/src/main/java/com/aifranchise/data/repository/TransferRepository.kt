package com.aifranchise.data.repository

import com.aifranchise.data.remote.ApiService
import com.aifranchise.data.remote.ManualTransferRequest
import com.aifranchise.data.remote.ResultState
import com.aifranchise.data.remote.TransferRecommendationDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TransferRepository @Inject constructor(
    private val apiService: ApiService
) {
    fun getTransferRecommendations(): Flow<ResultState<List<TransferRecommendationDto>>> = flow {
        emit(ResultState.Loading)
        try {
            val response = apiService.getTransferRecommendations()
            if (response.success && response.data != null) {
                emit(ResultState.Success(response.data))
            } else {
                emit(ResultState.Error(Exception(response.message)))
            }
        } catch (e: Exception) {
            emit(ResultState.Error(Exception(com.aifranchise.util.ApiUtils.parseError(e))))
        }
    }.flowOn(Dispatchers.IO)

    fun createManualTransfer(request: ManualTransferRequest): Flow<ResultState<TransferRecommendationDto>> = flow {
        emit(ResultState.Loading)
        try {
            val response = apiService.createManualTransfer(request)
            if (response.success && response.data != null) {
                emit(ResultState.Success(response.data))
            } else {
                emit(ResultState.Error(Exception(response.message)))
            }
        } catch (e: Exception) {
            emit(ResultState.Error(Exception(com.aifranchise.util.ApiUtils.parseError(e))))
        }
    }.flowOn(Dispatchers.IO)
}
