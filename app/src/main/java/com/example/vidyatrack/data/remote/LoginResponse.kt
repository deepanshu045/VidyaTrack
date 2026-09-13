package com.example.vidyatrack.data.remote

import com.google.gson.annotations.SerializedName

data class LoginResponse(
    @SerializedName("token") val token: String,
    @SerializedName("role") val role: String,
    @SerializedName("username") val username: String,
    @SerializedName("teacher_id") val teacherId: Int? = null
)
