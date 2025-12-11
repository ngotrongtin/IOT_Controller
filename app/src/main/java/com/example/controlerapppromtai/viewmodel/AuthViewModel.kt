package com.example.controlerapppromtai.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.controlerapppromtai.data.LoginResponse
import com.example.controlerapppromtai.data.User
import com.example.controlerapppromtai.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Authenticated(val user: User, val token: LoginResponse) : AuthState()
    data class Error(val message: String) : AuthState()
}

class AuthViewModel(private val authRepository: AuthRepository = AuthRepository()) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val response = authRepository.login(email, password)
                if (response.isSuccessful && response.body() != null) {
                    val loginResponse = response.body()!!
                    _authState.value = AuthState.Authenticated(loginResponse.user, loginResponse)
                } else {
                    _authState.value = AuthState.Error("Invalid email or password")
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "An unknown error occurred")
            }
        }
    }

    fun logout(refreshToken: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                authRepository.logout(refreshToken)
                _authState.value = AuthState.Idle
            } catch (e: Exception) {
                // Even if logout fails, we clear the local state
                _authState.value = AuthState.Idle
            }
        }
    }
}

