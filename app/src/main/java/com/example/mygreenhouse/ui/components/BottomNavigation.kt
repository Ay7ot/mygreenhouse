package com.example.mygreenhouse.ui.components

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.mygreenhouse.ui.navigation.NavDestination
import com.example.mygreenhouse.ui.theme.DarkBackground
import com.example.mygreenhouse.ui.theme.DarkSurface
import com.example.mygreenhouse.ui.theme.PrimaryGreen
import com.example.mygreenhouse.ui.theme.TextWhite

/**
 * Data class representing a bottom navigation item
 */
data class BottomNavItem(
    val label: String,
    val icon: ImageVector,
    val route: String
)

/**
 * Bottom navigation component
 */
@Composable
fun GreenhouseBottomNavigation(
    currentRoute: String,
    navController: NavController,
    modifier: Modifier = Modifier,
    darkTheme: Boolean
) {
    val navItems = listOf(
        BottomNavItem(
            label = "Home",
            icon = Icons.Filled.Home,
            route = NavDestination.Dashboard.route
        ),
        BottomNavItem(
            label = "Add Plant",
            icon = Icons.Filled.AddCircle,
            route = NavDestination.AddPlant.route
        ),
        BottomNavItem(
            label = "Tasks",
            icon = Icons.Filled.Checklist,
            route = NavDestination.Task.route
        ),
        BottomNavItem(
            label = "Stats",
            icon = Icons.Filled.BarChart,
            route = NavDestination.QuickStats.route
        ),
        BottomNavItem(
            label = "Dank Bank",
            icon = Icons.Filled.Savings,
            route = NavDestination.DankBank.route
        )
    )
    
    NavigationBar(
        modifier = modifier.navigationBarsPadding(),
        containerColor = if (darkTheme) DarkSurface else MaterialTheme.colorScheme.surface,
        contentColor = if (darkTheme) TextWhite else MaterialTheme.colorScheme.onSurface,
        tonalElevation = 4.dp
    ) {
        navItems.forEach { item ->
            NavigationBarItem(
                selected = currentRoute == item.route,
                onClick = { 
                    if (navController.currentDestination?.route != item.route) {
                        navController.navigate(item.route) {
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.label,
                        modifier = Modifier.size(24.dp)
                    )
                },
                label = {
                    Text(
                        text = item.label,
                        fontSize = 10.sp,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        modifier = Modifier.padding(top = 4.dp, start = 2.dp, end = 2.dp)
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = if (darkTheme) PrimaryGreen else MaterialTheme.colorScheme.primary,
                    selectedTextColor = if (darkTheme) PrimaryGreen else MaterialTheme.colorScheme.primary,
                    unselectedIconColor = (if (darkTheme) TextWhite else MaterialTheme.colorScheme.onSurface).copy(alpha = 0.7f),
                    unselectedTextColor = (if (darkTheme) TextWhite else MaterialTheme.colorScheme.onSurface).copy(alpha = 0.7f),
                    indicatorColor = Color.Transparent
                )
            )
        }
    }
} 