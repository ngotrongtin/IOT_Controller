package com.example.controlerapppromtai.repository

import com.example.controlerapppromtai.data.ApiService
import com.example.controlerapppromtai.data.LoginRequest
import com.example.controlerapppromtai.data.RefreshTokenRequest
import com.example.controlerapppromtai.data.RetrofitClient

class AuthRepository(private val apiService: ApiService = RetrofitClient.instance) {

    suspend fun login(email: String, password: String) = apiService.login(LoginRequest(email, password))

    suspend fun getProfile(token: String) = apiService.getProfile("Bearer $token")

    suspend fun refreshToken(refreshToken: String) = apiService.refreshToken(RefreshTokenRequest(refreshToken))

    suspend fun logout(refreshToken: String) = apiService.logout(RefreshTokenRequest(refreshToken))

    suspend fun logoutWithHeader(refreshToken: String) = apiService.logoutWithHeader("Refresh $refreshToken")
}

