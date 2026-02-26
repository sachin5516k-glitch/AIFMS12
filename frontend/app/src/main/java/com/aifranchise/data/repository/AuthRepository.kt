package com.aifranchise.data.repository

import com.aifranchise.data.remote.ApiService
import com.aifranchise.data.remote.LoginRequest
import com.aifranchise.data.remote.LoginResponse
import com.aifranchise.data.remote.ResultState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

class AuthRepository @Inject constructor(
    private val apiService: ApiService,
    private val tokenManager: com.aifranchise.util.TokenManager
) {
    suspend fun login(request: LoginRequest): Flow<ResultState<LoginResponse>> = flow {
        emit(ResultState.Loading)
        try {
            val response = apiService.login(request)
            if (response.success) {
                tokenManager.saveToken(response.data.token)
                emit(ResultState.Success(response.data))
            } else {
                emit(ResultState.Error(Exception(response.message)))
            }
        } catch (e: Exception) {
            emit(ResultState.Error(Exception(com.aifranchise.util.ApiUtils.parseError(e))))
        }
    }.flowOn(Dispatchers.IO)
}
