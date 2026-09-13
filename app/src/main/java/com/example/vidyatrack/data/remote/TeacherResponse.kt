package com.example.vidyatrack.data.remote

import com.google.gson.annotations.SerializedName

data class TeacherResponse(
    @SerializedName("id") val id: Int,
    @SerializedName("full_name") val fullName: String,
    @SerializedName("email") val email: String,
    @SerializedName("contact_number") val contactNumber: String? = null
)
