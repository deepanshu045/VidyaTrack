package com.example.vidyatrack.data.remote

import com.google.gson.annotations.SerializedName

data class AttendanceResponse(
    @SerializedName("id") val id: Int,
    @SerializedName("session_id") val sessionId: Int,
    @SerializedName("student_id") val studentId: Int,
    @SerializedName("status") val status: String
)
