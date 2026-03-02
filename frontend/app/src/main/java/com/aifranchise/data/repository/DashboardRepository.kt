package com.aifranchise.data.repository

import com.aifranchise.data.remote.ApiService
import com.aifranchise.data.remote.DashboardSummaryResponse
import com.aifranchise.data.remote.ResultState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

class DashboardRepository @Inject constructor(
    private val apiService: ApiService,
    private val tokenManager: com.aifranchise.util.TokenManager
) {
    suspend fun getDashboardSummary(): Flow<ResultState<com.aifranchise.data.remote.DashboardSummaryResponse>> = flow {
        emit(ResultState.Loading)
        try {
            val response = apiService.getManagerDashboard()
            if (response.success && response.data != null) {
                emit(ResultState.Success(response.data))
            } else {
                emit(ResultState.Error(Exception(response.message)))
            }
        } catch (e: Exception) {
            emit(ResultState.Error(Exception(com.aifranchise.util.ApiUtils.parseError(e))))
        }
    }.flowOn(Dispatchers.IO)

    suspend fun getAdminDashboardSummary(): Flow<ResultState<com.aifranchise.data.remote.AdminDashboardSummaryResponse>> = flow {
        emit(ResultState.Loading)
        try {
            val response = apiService.getAdminDashboard()
            if (response.success && response.data != null) {
                emit(ResultState.Success(response.data))
            } else {
                emit(ResultState.Error(Exception(response.message)))
            }
        } catch (e: Exception) {
            emit(ResultState.Error(Exception(com.aifranchise.util.ApiUtils.parseError(e))))
        }
    }.flowOn(Dispatchers.IO)

    suspend fun getAdminInventorySummary(): com.aifranchise.data.remote.ResultState<com.aifranchise.data.remote.AdminInventorySummaryResponse> {
        return try {
            val response = apiService.getAdminInventorySummary()
            if (response.success && response.data != null) {
                com.aifranchise.data.remote.ResultState.Success(response.data)
            } else {
                com.aifranchise.data.remote.ResultState.Error(Exception(response.message))
            }
        } catch (e: Exception) {
            com.aifranchise.data.remote.ResultState.Error(Exception(com.aifranchise.util.ApiUtils.parseError(e)))
        }
    }

    fun addBranch(request: com.aifranchise.data.remote.AddBranchRequest): Flow<ResultState<com.aifranchise.data.remote.BranchDto>> = flow {
        emit(ResultState.Loading)
        try {
            val response = apiService.addBranch(request)
            if (response.success && response.data != null) {
                emit(ResultState.Success(response.data))
            } else {
                emit(ResultState.Error(Exception(response.message)))
            }
        } catch (e: Exception) {
            emit(ResultState.Error(Exception(com.aifranchise.util.ApiUtils.parseError(e))))
        }
    }.flowOn(Dispatchers.IO)

    suspend fun getBranches(): Flow<ResultState<List<com.aifranchise.data.remote.BranchDto>>> = flow {
        emit(ResultState.Loading)
        try {
            val response = apiService.getBranches()
            if (response.success && response.data != null) {
                emit(ResultState.Success(response.data))
            } else {
                emit(ResultState.Error(Exception(response.message)))
            }
        } catch (e: Exception) {
            emit(ResultState.Error(Exception(com.aifranchise.util.ApiUtils.parseError(e))))
        }
    }.flowOn(Dispatchers.IO)

    suspend fun getBranchInventoryItems(branchId: String): Flow<ResultState<List<com.aifranchise.data.remote.InventoryItem>>> = flow {
        emit(ResultState.Loading)
        try {
            val response = apiService.getBranchInventoryItems(branchId)
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
