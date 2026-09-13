package com.example.vidyatrack.data.remote

import com.google.gson.annotations.SerializedName

data class StudentResponse(
    @SerializedName("id") val id: Int,
    @SerializedName("full_name") val fullName: String,
    @SerializedName("dob") val dob: String,
    @SerializedName("gender") val gender: String,
    @SerializedName("parent_name") val parentName: String? = null,
    @SerializedName("parent_contact") val parentContact: String? = null,
    @SerializedName("address") val address: String? = null,
    @SerializedName("admission_date") val admissionDate: String? = null,
    @SerializedName("status") val status: String
)
