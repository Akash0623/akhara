package com.akhara.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.FitnessCenter
import androidx.compose.material.icons.rounded.Home
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    data object Home : Screen("home", "Home", Icons.Rounded.Home)
    data object Calendar : Screen("calendar", "Calendar", Icons.Rounded.CalendarMonth)
    data object Library : Screen("library", "Library", Icons.Rounded.FitnessCenter)
    data object Stats : Screen("stats", "Stats", Icons.Rounded.BarChart)
}

object Routes {
    const val LOG_WORKOUT = "log_workout"
    const val EDIT_WORKOUT = "log_workout/{sessionId}"
    const val PLANNER = "planner"
    const val INSIGHTS = "insights"
    const val SECURITY_SETTINGS = "security_settings"

    fun editWorkout(sessionId: Int) = "log_workout/$sessionId"
}
