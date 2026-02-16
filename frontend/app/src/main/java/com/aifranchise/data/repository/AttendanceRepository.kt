package com.aifranchise.data.repository

import com.aifranchise.data.remote.ApiService
import com.aifranchise.data.remote.AttendanceRequest
import com.aifranchise.data.remote.AttendanceResponse
import com.aifranchise.data.remote.ResultState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

class AttendanceRepository @Inject constructor(
    private val apiService: ApiService
) {
    suspend fun checkIn(request: AttendanceRequest): Flow<ResultState<AttendanceResponse>> = flow {
        emit(ResultState.Loading)
        try {
            val response = apiService.checkIn(request)
            emit(ResultState.Success(response))
        } catch (e: Exception) {
            emit(ResultState.Error(e))
        }
    }.flowOn(Dispatchers.IO)

    suspend fun checkOut(request: AttendanceRequest): Flow<ResultState<AttendanceResponse>> = flow {
        emit(ResultState.Loading)
        try {
            val response = apiService.checkOut(request)
            emit(ResultState.Success(response))
        } catch (e: Exception) {
            emit(ResultState.Error(e))
        }
    }.flowOn(Dispatchers.IO)
}
