package com.example.vidyatrack.data.remote

import com.google.gson.annotations.SerializedName

data class ClassResponse(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("description") val description: String? = null
)
