package com.example.vidyatrack.data.remote

data class AdminStatsResponse(
    val total_students: Int,
    val active_students: Int,
    val total_teachers: Int,
    val total_classes: Int,
    val today_attendance: Int,
    val overall_percentage: Double
)
