package com.example.mygreenhouse

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.example.mygreenhouse.ui.navigation.GreenhouseNavGraph
import com.example.mygreenhouse.ui.screens.settings.SettingsViewModel
import com.example.mygreenhouse.ui.settings.ThemePreference
import com.example.mygreenhouse.ui.theme.MyGreenHouseTheme
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import android.graphics.Color
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import android.app.Activity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen

class MainActivity : ComponentActivity() {

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // Permission is granted. You can now show notifications.
        } else {
            // Permission denied. Inform the user that notifications will not be shown.
            // You might want to show a Snackbar or dialog here.
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        // Install the splash screen before any other content
        val splashScreen = installSplashScreen()
        
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // Optional: Keep the splash screen visible while loading
        splashScreen.setKeepOnScreenCondition {
            // You can add conditions here to keep splash screen visible longer
            // For example, while data is loading
            false
        }
        
        requestNotificationPermission()

        setContent {
            val settingsViewModel: SettingsViewModel = viewModel(factory = SettingsViewModel.Factory)
            val themePreference by settingsViewModel.themePreference.collectAsState()
            val isLoadingTheme by settingsViewModel.isLoadingTheme.collectAsState()

            if (isLoadingTheme) {
                // Display a simple Surface while the theme is loading to prevent FOIT.
                // This Surface will adhere to the system theme briefly.
                Surface(modifier = Modifier.fillMaxSize()) {
                    // Optionally, you can put a CircularProgressIndicator or a splash icon here
                }
            } else {
                val useDarkTheme = when (themePreference) {
                    ThemePreference.LIGHT -> false
                    ThemePreference.DARK -> true
                    ThemePreference.SYSTEM -> isSystemInDarkTheme()
                }

                MyGreenHouseTheme(darkTheme = useDarkTheme) {
                    // Make status bar transparent for edge-to-edge
                    val view = LocalView.current
                    if (!view.isInEditMode) {
                        SideEffect {
                            val window = (view.context as Activity).window
                            window.statusBarColor = Color.TRANSPARENT // Set status bar to transparent
                            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !useDarkTheme
                        }
                    }

                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        val navController = rememberNavController()
                        GreenhouseNavGraph(
                            navController = navController,
                            themePreferenceState = settingsViewModel.themePreference,
                            onThemePreferenceChange = { newPreference ->
                                settingsViewModel.setThemePreference(newPreference)
                            },
                            darkTheme = useDarkTheme
                        )
                    }
                }
            }
        }
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { // TIRAMISU is API 33
            when {
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    // Permission is already granted
                }
                shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS) -> {
                    // Explain to the user why you need the permission
                    // Then, request the permission
                    // For now, just requesting directly, but in a real app, show rationale.
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
                else -> {
                    // Directly request the permission
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        }
    }
}