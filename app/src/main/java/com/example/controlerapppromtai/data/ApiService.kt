package com.example.controlerapppromtai.data

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface ApiService {

    @POST("api/v1/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @GET("api/v1/profile")
    suspend fun getProfile(@Header("Authorization") token: String): Response<User>

    @POST("api/v1/refresh")
    suspend fun refreshToken(@Body request: RefreshTokenRequest): Response<RefreshTokenResponse>

    @DELETE("api/v1/logout")
    suspend fun logout(@Body request: RefreshTokenRequest): Response<LogoutResponse>

    @DELETE("api/v1/logout")
    suspend fun logoutWithHeader(@Header("Authorization") token: String): Response<LogoutResponse>
}

