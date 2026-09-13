package com.example.vidyatrack.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.vidyatrack.ui.screens.*

@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(navController = navController, startDestination = Screen.Login.route) {
        composable(Screen.Login.route) {
            LoginScreen(onLoginSuccess = { role ->
                if (role == "admin") navController.navigate(Screen.AdminDashboard.route) { popUpTo(Screen.Login.route) { inclusive = true } }
                else navController.navigate(Screen.TeacherDashboard.route) { popUpTo(Screen.Login.route) { inclusive = true } }
            })
        }
        composable(Screen.AdminDashboard.route) {
            AdminDashboardScreen(
                onLogout = { navController.navigate(Screen.Login.route) { popUpTo(Screen.AdminDashboard.route) { inclusive = true } } },
                onNavigateToStudents = { navController.navigate(Screen.StudentList.route) },
                onNavigateToTeachers = { navController.navigate(Screen.TeacherList.route) },
                onNavigateToClasses = { navController.navigate(Screen.ClassList.route) }
            )
        }
        composable(Screen.TeacherDashboard.route) {
            TeacherDashboardScreen(
                onLogout = { navController.navigate(Screen.Login.route) { popUpTo(Screen.TeacherDashboard.route) { inclusive = true } } },
                onMarkAttendance = { classId -> navController.navigate(Screen.Attendance.createRoute(classId)) }
            )
        }
        composable(Screen.Attendance.route) { AttendanceScreen(onNavigateBack = { navController.popBackStack() }) }
        composable(Screen.ClassReports.route) { ClassReportsScreen(onNavigateBack = { navController.popBackStack() }) }
        composable(Screen.StudentList.route) {
            StudentListScreen(
                onNavigateToAddEdit = { studentId -> navController.navigate(Screen.AddEditStudent.createRoute(studentId)) },
                onNavigateToDetail = { studentId -> navController.navigate(Screen.StudentDetail.createRoute(studentId)) }
            )
        }
        composable(Screen.StudentDetail.route) { StudentDetailScreen(onNavigateBack = { navController.popBackStack() }, onNavigateToEdit = { id -> navController.navigate(Screen.AddEditStudent.createRoute(id)) }) }
        composable(Screen.AddEditStudent.route) { AddEditStudentScreen(onNavigateBack = { navController.popBackStack() }) }
        composable(Screen.TeacherList.route) { TeacherListScreen(onNavigateToAdd = { navController.navigate(Screen.AddTeacher.route) }, onNavigateBack = { navController.popBackStack() }) }
        composable(Screen.AddTeacher.route) { AddTeacherScreen(onNavigateBack = { navController.popBackStack() }) }
        composable(Screen.ClassList.route) { ClassListScreen(onNavigateToAdd = { navController.navigate(Screen.AddClass.route) }, onNavigateToDetail = { id -> navController.navigate(Screen.ClassDetail.createRoute(id)) }, onNavigateBack = { navController.popBackStack() }) }
        composable(Screen.AddClass.route) { AddClassScreen(onNavigateBack = { navController.popBackStack() }) }
        composable(Screen.ClassDetail.route) { ClassDetailScreen(onNavigateBack = { navController.popBackStack() }, onViewReports = { id -> navController.navigate(Screen.ClassReports.createRoute(id)) }) }
    }
}
