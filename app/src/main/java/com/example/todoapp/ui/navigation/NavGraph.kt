package com.example.todoapp.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.example.todoapp.ui.screens.home.HomeScreen
import com.example.todoapp.ui.screens.add_edit_task.AddEditTaskScreen
import com.example.todoapp.ui.screens.calendar.CalendarScreen
import com.example.todoapp.ui.screens.analytics.AnalyticsScreen

@Composable
fun NavGraph(
    navController: NavHostController = rememberNavController(),
    startTaskId: Int? = null
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    LaunchedEffect(startTaskId) {
        if (startTaskId != null) {
            navController.navigate(Screen.AddEditTask.passTaskId(startTaskId))
        }
    }

    val showBottomBar = currentDestination?.route in listOf(
        Screen.Home.route,
        Screen.Calendar.route,
        Screen.Analytics.route
    )

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Home, contentDescription = null) },
                        label = { Text("Home") },
                        selected = currentDestination?.route == Screen.Home.route,
                        onClick = {
                            navController.navigate(Screen.Home.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.CalendarMonth, contentDescription = null) },
                        label = { Text("Calendar") },
                        selected = currentDestination?.route == Screen.Calendar.route,
                        onClick = {
                            navController.navigate(Screen.Calendar.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Analytics, contentDescription = null) },
                        label = { Text("Stats") },
                        selected = currentDestination?.route == Screen.Analytics.route,
                        onClick = {
                            navController.navigate(Screen.Analytics.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(route = Screen.Home.route) {
                HomeScreen(
                    onAddTaskClick = {
                        navController.navigate(Screen.AddEditTask.passTaskId())
                    },
                    onTaskClick = { taskId ->
                        navController.navigate(Screen.AddEditTask.passTaskId(taskId))
                    }
                )
            }
            
            composable(route = Screen.Calendar.route) {
                CalendarScreen(
                    onBackClick = { navController.popBackStack() },
                    onTaskClick = { taskId ->
                        navController.navigate(Screen.AddEditTask.passTaskId(taskId))
                    }
                )
            }
            
            composable(route = Screen.Analytics.route) {
                AnalyticsScreen(
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable(
                route = Screen.AddEditTask.route,
                arguments = listOf(
                    navArgument("taskId") {
                        type = NavType.IntType
                        defaultValue = -1
                    }
                )
            ) {
                AddEditTaskScreen(
                    onBackClick = {
                        navController.popBackStack()
                    }
                )
            }
        }
    }
}
