package com.example.vidyatrack.data.remote

data class TeacherSummaryResponse(
    val teacher_name: String,
    val assigned_classes: List<AssignedClass>
)

data class AssignedClass(
    val id: Int,
    val name: String,
    val student_count: Int
)
