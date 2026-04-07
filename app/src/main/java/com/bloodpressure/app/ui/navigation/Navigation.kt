package com.bloodpressure.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.bloodpressure.app.ui.home.HomeScreen
import com.bloodpressure.app.ui.record.RecordScreen
import com.bloodpressure.app.ui.history.HistoryScreen
import com.bloodpressure.app.ui.settings.SettingsScreen

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Record : Screen("record/{date}/{period}") {
        fun createRoute(date: String, period: String?) = "record/$date/${period ?: "auto"}"
    }
    object History : Screen("history")
    object Settings : Screen("settings")
}

@Composable
fun BloodPressureNavHost(
    navController: NavHostController = rememberNavController(),
    onExportData: () -> Unit = {}
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToRecord = { date, period ->
                    navController.navigate(Screen.Record.createRoute(date, period))
                },
                onNavigateToHistory = {
                    navController.navigate(Screen.History.route)
                },
                onNavigateToSettings = {
                    navController.navigate(Screen.Settings.route)
                }
            )
        }
        composable(Screen.Record.route) { backStackEntry ->
            val date = backStackEntry.arguments?.getString("date") ?: ""
            val period = backStackEntry.arguments?.getString("period") ?: ""
            RecordScreen(
                date = date,
                period = period,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(Screen.History.route) {
            HistoryScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(Screen.Settings.route) {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() },
                onExportData = onExportData
            )
        }
    }
}
