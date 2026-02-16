package com.aifranchise.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aifranchise.data.remote.LoginRequest
import com.aifranchise.data.remote.LoginResponse
import com.aifranchise.data.remote.ResultState
import com.aifranchise.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val repository: AuthRepository
) : ViewModel() {

    private val _loginState = MutableStateFlow<ResultState<LoginResponse>?>(null)
    val loginState = _loginState.asStateFlow()

    fun login(email: String, pass: String) {
        if (email.isBlank() || pass.isBlank()) {
            return
        }
        viewModelScope.launch {
            repository.login(LoginRequest(email, pass)).collect {
                _loginState.value = it
            }
        }
    }
}
