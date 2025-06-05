package com.example.mygreenhouse.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.example.mygreenhouse.ui.screens.allplants.AllPlantsScreen
import com.example.mygreenhouse.ui.screens.auth.AuthViewModel
import com.example.mygreenhouse.ui.screens.auth.LockScreen
import com.example.mygreenhouse.ui.screens.auth.SetupPinScreen
import com.example.mygreenhouse.ui.screens.dashboard.DashboardScreen
import com.example.mygreenhouse.ui.screens.dankbank.AddHarvestScreen
import com.example.mygreenhouse.ui.screens.dankbank.AddSeedScreen
import com.example.mygreenhouse.ui.screens.dankbank.DankBankScreen
import com.example.mygreenhouse.ui.screens.dankbank.EditHarvestScreen
import com.example.mygreenhouse.ui.screens.dankbank.EditSeedScreen
import com.example.mygreenhouse.ui.screens.dankbank.HarvestDetailScreen
import com.example.mygreenhouse.ui.screens.dankbank.SeedDetailScreen
import com.example.mygreenhouse.ui.screens.editplant.EditPlantScreen
import com.example.mygreenhouse.ui.screens.quickstats.QuickStatsScreen
import com.example.mygreenhouse.ui.screens.settings.SettingsScreen
import com.example.mygreenhouse.ui.screens.task.EditTaskScreen
import com.example.mygreenhouse.ui.screens.task.ScheduleTaskScreen
import com.example.mygreenhouse.ui.screens.task.TaskListScreen
import com.example.mygreenhouse.ui.screens.task.TaskScreen
import com.example.mygreenhouse.ui.screens.task.TaskViewModel
import com.example.mygreenhouse.ui.screens.photomanagement.PhotoManagementScreen
import com.example.mygreenhouse.ui.screens.datamanagement.DataManagementScreen
import com.example.mygreenhouse.ui.settings.ThemePreference
import kotlinx.coroutines.flow.StateFlow

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
    object AllPlants : NavDestination("all_plants")
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
    object AddHarvest : NavDestination("add_harvest")
    object AddSeed : NavDestination("add_seed")
    object EditHarvest : NavDestination("editHarvest/{harvestId}") {
        fun createRoute(harvestId: String) = "editHarvest/$harvestId"
    }
    object EditSeed : NavDestination("editSeed/{seedId}") {
        fun createRoute(seedId: String) = "editSeed/$seedId"
    }
    object HarvestDetail : NavDestination("harvestDetail/{harvestId}") {
        fun createRoute(harvestId: String) = "harvestDetail/$harvestId"
    }
    object SeedDetail : NavDestination("seedDetail/{seedId}") {
        fun createRoute(seedId: String) = "seedDetail/$seedId"
    }
    object Settings : NavDestination("settings")
    object PhotoManagement : NavDestination("photo_management")
    object DataManagement : NavDestination("data_management")
    
    // Auth destinations
    object Lock : NavDestination("lock")
    object SetupPin : NavDestination("setup_pin")
}

/**
 * Navigation graph for the app
 */
@Composable
fun GreenhouseNavGraph(
    navController: NavHostController,
    themePreferenceState: StateFlow<ThemePreference>,
    onThemePreferenceChange: (ThemePreference) -> Unit,
    darkTheme: Boolean
) {
    // Get AuthViewModel to check if PIN lock is enabled
    val authViewModel: AuthViewModel = viewModel(factory = AuthViewModel.Factory)
    val isPinLockEnabled by authViewModel.isPinLockEnabled.collectAsState()
    val isAuthenticatedInSession by authViewModel.isAuthenticatedInSession.collectAsState()
    
    // Determine the start destination based on authentication status
    val startDestination = if (authViewModel.isAuthenticationRequired()) {
        NavDestination.Lock.route
    } else {
        NavDestination.Dashboard.route
    }
    
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // Lock Screen
        composable(NavDestination.Lock.route) {
            LockScreen(
                onUnlockSuccess = {
                    navController.navigate(NavDestination.Dashboard.route) {
                        popUpTo(NavDestination.Lock.route) { inclusive = true }
                    }
                },
                darkTheme = darkTheme
            )
        }
        
        // PIN Setup Screen
        composable(NavDestination.SetupPin.route) {
            SetupPinScreen(
                onNavigateBack = { navController.popBackStack() },
                darkTheme = darkTheme
            )
        }
        
        composable(NavDestination.Dashboard.route) {
            DashboardScreen(
                navigateToAddPlant = { navController.navigate(NavDestination.AddPlant.route) },
                navigateToEditPlant = { plantId -> 
                    navController.navigate(NavDestination.EditPlant.createRoute(plantId))
                },
                navigateToAllPlants = { navController.navigate(NavDestination.AllPlants.route) },
                navigateToTask = { navController.navigate(NavDestination.Task.route) },
                navigateToTaskList = { navController.navigate(NavDestination.TaskList.route) },
                navigateToEditTask = { taskId, taskTypeName ->
                    navController.navigate(NavDestination.EditTask.createRoute(taskId, taskTypeName))
                },
                navigateToQuickStats = { navController.navigate(NavDestination.QuickStats.route) },
                navigateToDankBank = { navController.navigate(NavDestination.DankBank.route) },
                navigateToSettings = { navController.navigate(NavDestination.Settings.route) },
                navController = navController,
                darkTheme = darkTheme
            )
        }
        
        composable(NavDestination.AddPlant.route) {
            AddPlantScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToDashboard = { 
                    navController.navigate(NavDestination.Dashboard.route) {
                        popUpTo(NavDestination.AddPlant.route) { inclusive = true }
                    }
                },
                navController = navController,
                darkTheme = darkTheme
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
                onPlantUpdated = { navController.popBackStack() },
                navController = navController,
                darkTheme = darkTheme
            )
        }
        
        composable(NavDestination.AllPlants.route) {
            AllPlantsScreen(
                onNavigateBack = { navController.popBackStack() },
                onEditPlant = { plantId ->
                    navController.navigate(NavDestination.EditPlant.createRoute(plantId))
                },
                darkTheme = darkTheme
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
                },
                navController = navController,
                darkTheme = darkTheme
            )
        }
        
        composable(NavDestination.TaskList.route) {
            val taskViewModel: TaskViewModel = viewModel(factory = TaskViewModel.Factory)
            TaskListScreen(
                viewModel = taskViewModel,
                onNavigateBack = { navController.popBackStack() },
                onEditTask = { task ->
                    navController.navigate(NavDestination.EditTask.createRoute(task.id, task.type.name))
                },
                darkTheme = darkTheme
            )
        }
        
        composable(
            route = NavDestination.ScheduleTask.route,
            arguments = listOf(navArgument("taskTypeName") { type = NavType.StringType })
        ) { backStackEntry ->
            val taskTypeName = backStackEntry.arguments?.getString("taskTypeName")
            val taskType = taskTypeName?.let { TaskType.valueOf(it) }
            val taskViewModel: TaskViewModel = viewModel(factory = TaskViewModel.Factory)
            if (taskType != null) {
                ScheduleTaskScreen(
                    taskType = taskType,
                    onNavigateBack = { navController.popBackStack() },
                    onSaveTask = { _, _, _, _ -> },
                    viewModel = taskViewModel,
                    darkTheme = darkTheme
                )
            } else {
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
            val taskViewModel: TaskViewModel = viewModel(factory = TaskViewModel.Factory)
            if (taskId != null && taskType != null) {
                EditTaskScreen(
                    taskId = taskId,
                    taskType = taskType,
                    viewModel = taskViewModel,
                    onNavigateBack = { navController.popBackStack() },
                    darkTheme = darkTheme
                )
            } else {
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
                },
                navController = navController,
                darkTheme = darkTheme
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
                },
                onNavigateToAddHarvest = { navController.navigate(NavDestination.AddHarvest.route) },
                onNavigateToAddSeed = { navController.navigate(NavDestination.AddSeed.route) },
                navController = navController,
                darkTheme = darkTheme
            )
        }
        
        composable(NavDestination.AddHarvest.route) {
            AddHarvestScreen(
                onNavigateBack = { navController.popBackStack() },
                onHarvestAdded = { navController.popBackStack() },
                navController = navController,
                darkTheme = darkTheme
            )
        }
        
        composable(NavDestination.AddSeed.route) {
            AddSeedScreen(
                onNavigateBack = { navController.popBackStack() },
                onSeedAdded = { navController.popBackStack() },
                navController = navController,
                darkTheme = darkTheme
            )
        }
        
        composable(
            route = NavDestination.EditHarvest.route,
            arguments = listOf(navArgument("harvestId") { type = NavType.StringType })
        ) { backStackEntry ->
            val harvestId = backStackEntry.arguments?.getString("harvestId")
            if (harvestId != null) {
                EditHarvestScreen(
                    harvestId = harvestId,
                    onNavigateBack = { navController.popBackStack() },
                    onHarvestUpdated = { navController.popBackStack() },
                    navController = navController,
                    darkTheme = darkTheme
                )
            } else {
                navController.popBackStack()
            }
        }
        
        composable(
            route = NavDestination.EditSeed.route,
            arguments = listOf(navArgument("seedId") { type = NavType.StringType })
        ) { backStackEntry ->
            val seedId = backStackEntry.arguments?.getString("seedId")
            if (seedId != null) {
                EditSeedScreen(
                    seedId = seedId,
                    onNavigateBack = { navController.popBackStack() },
                    onSeedUpdated = { navController.popBackStack() },
                    navController = navController,
                    darkTheme = darkTheme
                )
            } else {
                navController.popBackStack()
            }
        }
        
        composable(
            route = NavDestination.HarvestDetail.route,
            arguments = listOf(navArgument("harvestId") { type = NavType.StringType })
        ) { backStackEntry ->
            val harvestId = backStackEntry.arguments?.getString("harvestId")
            if (harvestId != null) {
                HarvestDetailScreen(
                    harvestId = harvestId,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToEdit = { id -> navController.navigate(NavDestination.EditHarvest.createRoute(id)) },
                    onDeleteHarvest = { navController.popBackStack() },
                    navController = navController,
                    darkTheme = darkTheme
                )
            } else {
                navController.popBackStack()
            }
        }
        
        composable(
            route = NavDestination.SeedDetail.route,
            arguments = listOf(navArgument("seedId") { type = NavType.StringType })
        ) { backStackEntry ->
            val seedId = backStackEntry.arguments?.getString("seedId")
            if (seedId != null) {
                SeedDetailScreen(
                    seedId = seedId,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToEdit = { id -> navController.navigate(NavDestination.EditSeed.createRoute(id)) },
                    onDeleteSeed = { navController.popBackStack() },
                    navController = navController,
                    darkTheme = darkTheme
                )
            } else {
                navController.popBackStack()
            }
        }
        
        composable(NavDestination.Settings.route) {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToSetupPin = { navController.navigate(NavDestination.SetupPin.route) },
                onNavigateToPhotoManagement = { navController.navigate(NavDestination.PhotoManagement.route) },
                onNavigateToDataManagement = { navController.navigate(NavDestination.DataManagement.route) },
                currentThemePreferenceState = themePreferenceState,
                onThemePreferenceChange = onThemePreferenceChange,
                darkTheme = darkTheme
            )
        }
        
        composable(NavDestination.PhotoManagement.route) {
            PhotoManagementScreen(
                onNavigateBack = { navController.popBackStack() },
                darkTheme = darkTheme
            )
        }
        
        composable(NavDestination.DataManagement.route) {
            DataManagementScreen(
                onNavigateBack = { navController.popBackStack() },
                darkTheme = darkTheme
            )
        }
    }
} 