package com.example.todoapp.ui.navigation

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Calendar : Screen("calendar")
    object Analytics : Screen("analytics")
    object AddEditTask : Screen("add_edit_task?taskId={taskId}") {
        fun passTaskId(taskId: Int? = null): String {
            return "add_edit_task?taskId=$taskId"
        }
    }
}
