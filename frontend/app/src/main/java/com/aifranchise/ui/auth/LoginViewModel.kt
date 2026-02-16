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

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val repository: AuthRepository
) : ViewModel() {

    private val _loginState = MutableStateFlow<ResultState<Any>>(ResultState.Loading)
    val loginState = _loginState.asStateFlow()

    fun login(email: String, p0: String) {
        if (email.isEmpty() || p0.isEmpty()) {
            _loginState.value = ResultState.Error("Email and Password required")
            return
        }
        
        viewModelScope.launch {
            _loginState.value = ResultState.Loading
            repository.login(email, p0).collect { result ->
                // Assuming result is generic ResultState, need to cast or map
                // For simplicity, just passing through if types align
                // In real app, might map LoginResponse to UI Model
                _loginState.value = result as ResultState<Any> 
            }
        }
    }
}
