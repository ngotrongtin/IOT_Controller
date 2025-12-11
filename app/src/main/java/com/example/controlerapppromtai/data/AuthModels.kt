package com.example.controlerapppromtai.data

import com.google.gson.annotations.SerializedName

data class LoginRequest(
    val email: String,
    val password: String
)

data class LoginResponse(
    @SerializedName("access_token") val accessToken: String,
    @SerializedName("access_token_expires_at") val accessTokenExpiresAt: String,
    @SerializedName("refresh_token") val refreshToken: String,
    @SerializedName("refresh_token_expires_at") val refreshTokenExpiresAt: String,
    val user: User
)

data class User(
    val id: Int,
    val email: String,
    val fullname: String,
    val admin: Boolean
)

data class RefreshTokenRequest(
    @SerializedName("refresh_token") val refreshToken: String
)

data class RefreshTokenResponse(
    @SerializedName("access_token") val accessToken: String,
    @SerializedName("access_token_expires_at") val accessTokenExpiresAt: String,
    @SerializedName("refresh_token") val refreshToken: String,
    @SerializedName("refresh_token_expires_at") val refreshTokenExpiresAt: String
)

data class LogoutResponse(
    val success: Boolean
)

data class ApiError(
    val error: String
)

