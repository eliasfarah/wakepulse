package com.wakepulse.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.wakepulse.app.ui.WakePulseViewModel
import com.wakepulse.app.ui.screens.DiagnosticsScreen
import com.wakepulse.app.ui.screens.HomeScreen
import com.wakepulse.app.ui.theme.WakePulseTheme

class MainActivity : ComponentActivity() {
    private val viewModel: WakePulseViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WakePulseTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    val navController = rememberNavController()
                    NavHost(navController = navController, startDestination = HOME) {
                        composable(HOME) {
                            HomeScreen(
                                viewModel = viewModel,
                                onOpenDiagnostics = { navController.navigate(DIAGNOSTICS) },
                            )
                        }
                        composable(DIAGNOSTICS) {
                            DiagnosticsScreen(
                                viewModel = viewModel,
                                onBack = navController::navigateUp,
                            )
                        }
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.refreshSystemStatus()
    }

    private companion object {
        const val HOME = "home"
        const val DIAGNOSTICS = "diagnostics"
    }
}
