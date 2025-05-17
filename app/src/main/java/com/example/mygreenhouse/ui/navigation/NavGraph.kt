package com.example.mygreenhouse.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.mygreenhouse.data.AppDatabase
import com.example.mygreenhouse.data.model.TaskType
import com.example.mygreenhouse.data.repository.TaskRepository
import com.example.mygreenhouse.ui.screens.addplant.AddPlantScreen
import com.example.mygreenhouse.ui.screens.dashboard.DashboardScreen
import com.example.mygreenhouse.ui.screens.dankbank.DankBankScreen
import com.example.mygreenhouse.ui.screens.editplant.EditPlantScreen
import com.example.mygreenhouse.ui.screens.quickstats.QuickStatsScreen
import com.example.mygreenhouse.ui.screens.settings.SettingsScreen
import com.example.mygreenhouse.ui.screens.task.EditTaskScreen
import com.example.mygreenhouse.ui.screens.task.ScheduleTaskScreen
import com.example.mygreenhouse.ui.screens.task.TaskListScreen
import com.example.mygreenhouse.ui.screens.task.TaskScreen
import com.example.mygreenhouse.ui.screens.task.TaskViewModel

/**
 * Navigation destinations for the app
 */
sealed class NavDestination(val route: String) {
    // Main destinations
    object Dashboard : NavDestination("dashboard")
    object AddPlant : NavDestination("add_plant")
    object EditPlant : NavDestination("edit_plant/{plantId}") {
        fun createRoute(plantId: String) = "edit_plant/$plantId"
    }
    object Task : NavDestination("task")
    object TaskList : NavDestination("task_list")
    object ScheduleTask : NavDestination("schedule_task/{taskTypeName}") {
        fun createRoute(taskTypeName: String) = "schedule_task/$taskTypeName"
    }
    object EditTask : NavDestination("edit_task/{taskId}/{taskTypeName}") {
        fun createRoute(taskId: String, taskTypeName: String) = "edit_task/$taskId/$taskTypeName"
    }
    object QuickStats : NavDestination("quick_stats")
    object DankBank : NavDestination("dank_bank")
    object Settings : NavDestination("settings")
}

/**
 * Navigation graph for the app
 */
@Composable
fun GreenhouseNavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = NavDestination.Dashboard.route
    ) {
        composable(NavDestination.Dashboard.route) {
            DashboardScreen(
                navigateToAddPlant = { navController.navigate(NavDestination.AddPlant.route) },
                navigateToEditPlant = { plantId -> 
                    navController.navigate(NavDestination.EditPlant.createRoute(plantId))
                },
                navigateToTask = { navController.navigate(NavDestination.Task.route) },
                navigateToEditTask = { taskId, taskTypeName ->
                    navController.navigate(NavDestination.EditTask.createRoute(taskId, taskTypeName))
                }
            )
        }
        
        composable(NavDestination.AddPlant.route) {
            AddPlantScreen(
                onNavigateBack = { navController.popBackStack() },
                onPlantAdded = { navController.popBackStack() }
            )
        }
        
        composable(
            route = NavDestination.EditPlant.route,
            arguments = listOf(
                navArgument("plantId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            EditPlantScreen(
                onNavigateBack = { navController.popBackStack() },
                onPlantUpdated = { navController.popBackStack() }
            )
        }
        
        composable(NavDestination.Task.route) {
            TaskScreen(
                onNavigateBack = { 
                    if (navController.previousBackStackEntry?.destination?.route == NavDestination.Dashboard.route) {
                        navController.popBackStack()
                    } else {
                        navController.navigate(NavDestination.Dashboard.route) {
                            popUpTo(NavDestination.Dashboard.route) { inclusive = true }
                        }
                    }
                },
                onTaskTypeSelected = { taskType ->
                    navController.navigate(NavDestination.ScheduleTask.createRoute(taskType.name))
                },
                onViewTaskList = {
                    navController.navigate(NavDestination.TaskList.route)
                }
            )
        }
        
        composable(NavDestination.TaskList.route) {
            // Use the ViewModel's factory for instantiation
            val taskViewModel: TaskViewModel = viewModel(factory = TaskViewModel.Factory)
            
            TaskListScreen(
                viewModel = taskViewModel,
                onNavigateBack = { navController.popBackStack() },
                onEditTask = { task ->
                    navController.navigate(NavDestination.EditTask.createRoute(task.id, task.type.name))
                }
            )
        }
        
        composable(
            route = NavDestination.ScheduleTask.route,
            arguments = listOf(navArgument("taskTypeName") { type = NavType.StringType })
        ) { backStackEntry ->
            val taskTypeName = backStackEntry.arguments?.getString("taskTypeName")
            val taskType = taskTypeName?.let { TaskType.valueOf(it) }

            // Use the ViewModel's factory for instantiation
            val taskViewModel: TaskViewModel = viewModel(factory = TaskViewModel.Factory)

            if (taskType != null) {
                ScheduleTaskScreen(
                    taskType = taskType,
                    onNavigateBack = { navController.popBackStack() },
                    onSaveTask = { _, _, _, _ ->
                        // This lambda is kept for signature compatibility but logic is in ViewModel
                    },
                    viewModel = taskViewModel
                )
            } else {
                // Handle error case where taskType is null, e.g. navigate back or show error
                navController.popBackStack()
            }
        }
        
        composable(
            route = NavDestination.EditTask.route,
            arguments = listOf(
                navArgument("taskId") { type = NavType.StringType },
                navArgument("taskTypeName") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val taskId = backStackEntry.arguments?.getString("taskId")
            val taskTypeName = backStackEntry.arguments?.getString("taskTypeName")
            val taskType = taskTypeName?.let { TaskType.valueOf(it) }
            
            // Use the ViewModel's factory for instantiation
            val taskViewModel: TaskViewModel = viewModel(factory = TaskViewModel.Factory)
            
            // Correctly scope the composable function
            if (taskId != null && taskType != null) {
                // We can't directly call the ViewModel here as it's not a Composable context
                // Instead, we need to create another composable that handles this
                EditTaskScreen(
                    taskId = taskId,
                    taskType = taskType,
                    viewModel = taskViewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            } else {
                // Handle error case where taskId or taskType is null
                navController.popBackStack()
            }
        }
        
        composable(NavDestination.QuickStats.route) {
            QuickStatsScreen(
                onNavigateBack = { 
                    if (navController.previousBackStackEntry?.destination?.route == NavDestination.Dashboard.route) {
                        navController.popBackStack()
                    } else {
                        navController.navigate(NavDestination.Dashboard.route) {
                            popUpTo(NavDestination.Dashboard.route) { inclusive = true }
                        }
                    }
                }
            )
        }
        
        composable(NavDestination.DankBank.route) {
            DankBankScreen(
                onNavigateBack = { 
                    if (navController.previousBackStackEntry?.destination?.route == NavDestination.Dashboard.route) {
                        navController.popBackStack()
                    } else {
                        navController.navigate(NavDestination.Dashboard.route) {
                            popUpTo(NavDestination.Dashboard.route) { inclusive = true }
                        }
                    }
                }
            )
        }
        
        composable(NavDestination.Settings.route) {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
} 