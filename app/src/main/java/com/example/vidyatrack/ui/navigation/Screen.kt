package com.example.vidyatrack.ui.navigation

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object AdminDashboard : Screen("admin_dashboard")
    object TeacherDashboard : Screen("teacher_dashboard")
    object StudentList : Screen("student_list")
    object StudentDetail : Screen("student_detail/{studentId}") {
        fun createRoute(studentId: Int) = "student_detail/$studentId"
    }
    object AddEditStudent : Screen("add_edit_student?studentId={studentId}") {
        fun createRoute(studentId: Int? = null) = if (studentId != null) "add_edit_student?studentId=$studentId" else "add_edit_student"
    }
    object TeacherList : Screen("teacher_list")
    object AddTeacher : Screen("add_teacher")
    object ClassList : Screen("class_list")
    object AddClass : Screen("add_class")
    object ClassDetail : Screen("class_detail/{classId}") {
        fun createRoute(classId: Int) = "class_detail/$classId"
    }
    object Attendance : Screen("attendance/{classId}") {
        fun createRoute(classId: Int) = "attendance/$classId"
    }
    object ClassReports : Screen("class_reports/{classId}") {
        fun createRoute(classId: Int) = "class_reports/$classId"
    }
}
