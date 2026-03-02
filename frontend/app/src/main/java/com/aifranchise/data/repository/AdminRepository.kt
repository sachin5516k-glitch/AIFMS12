package com.aifranchise.data.repository

import com.aifranchise.data.remote.AddManagerRequest
import com.aifranchise.data.remote.ApiService
import com.aifranchise.data.remote.BranchDto
import com.aifranchise.data.remote.ManagerDto
import com.aifranchise.data.remote.ResultState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AdminRepository @Inject constructor(
    private val apiService: ApiService
) {
    fun getManagers(): Flow<ResultState<List<ManagerDto>>> = flow {
        emit(ResultState.Loading)
        try {
            val response = apiService.getManagers()
            if (response.success && response.data != null) {
                emit(ResultState.Success(response.data))
            } else {
                emit(ResultState.Error(Exception(response.message ?: "Failed to fetch managers")))
            }
        } catch (e: Exception) {
            emit(ResultState.Error(Exception(com.aifranchise.util.ApiUtils.parseError(e))))
        }
    }

    fun addManager(request: AddManagerRequest): Flow<ResultState<ManagerDto>> = flow {
        emit(ResultState.Loading)
        try {
            val response = apiService.addManager(request)
            if (response.success && response.data != null) {
                emit(ResultState.Success(response.data))
            } else {
                emit(ResultState.Error(Exception(response.message ?: "Failed to add manager")))
            }
        } catch (e: Exception) {
            emit(ResultState.Error(Exception(com.aifranchise.util.ApiUtils.parseError(e))))
        }
    }

    fun deactivateManager(id: String): Flow<ResultState<ManagerDto>> = flow {
        emit(ResultState.Loading)
        try {
            val response = apiService.deactivateManager(id)
            if (response.success && response.data != null) {
                emit(ResultState.Success(response.data))
            } else {
                emit(ResultState.Error(Exception(response.message ?: "Failed to deactivate manager")))
            }
        } catch (e: Exception) {
            emit(ResultState.Error(Exception(com.aifranchise.util.ApiUtils.parseError(e))))
        }
    }
}
