package com.aifranchise.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aifranchise.data.remote.ResultState
import com.aifranchise.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

import com.aifranchise.data.remote.LoginRequest

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val repository: AuthRepository
) : ViewModel() {

    private val _loginState = MutableStateFlow<ResultState<Any>>(ResultState.Loading)
    val loginState = _loginState.asStateFlow()

    fun login(email: String, password: String) {
        if (email.isEmpty() || password.isEmpty()) {
            _loginState.value = ResultState.Error(java.lang.Exception("Email and Password required"))
            return
        }
        
        viewModelScope.launch {
            _loginState.value = ResultState.Loading
            repository.login(LoginRequest(email, password)).collect { result ->
                _loginState.value = result
            }
        }
    }
}
