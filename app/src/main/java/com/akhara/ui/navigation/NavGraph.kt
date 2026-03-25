package com.akhara.ui.navigation

import android.app.Application
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.Lifecycle
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.akhara.data.repository.WorkoutRepository
import com.akhara.ui.screens.calendar.CalendarScreen
import com.akhara.ui.screens.calendar.CalendarViewModel
import com.akhara.ui.screens.exercises.ExerciseLibraryScreen
import com.akhara.ui.screens.exercises.ExerciseLibraryViewModel
import com.akhara.ui.screens.home.HomeScreen
import com.akhara.ui.screens.home.HomeViewModel
import com.akhara.ui.screens.insights.InsightsScreen
import com.akhara.ui.screens.insights.InsightsViewModel
import com.akhara.ui.screens.planner.WeeklyPlannerScreen
import com.akhara.ui.screens.planner.WeeklyPlannerViewModel
import com.akhara.ui.screens.settings.SecuritySettingsScreen
import com.akhara.ui.screens.calendar.ViewWorkoutScreen
import com.akhara.ui.screens.stats.StatsScreen
import com.akhara.ui.screens.stats.StatsViewModel
import com.akhara.ui.screens.workout.LogWorkoutScreen
import com.akhara.ui.screens.workout.LogWorkoutViewModel

@Composable
fun NavGraph(
    navController: NavHostController,
    repository: WorkoutRepository,
    application: Application
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(Screen.Home.route) {
            val vm: HomeViewModel = viewModel(factory = HomeViewModel.Factory(repository))
            val lifecycleOwner = LocalLifecycleOwner.current
            LaunchedEffect(lifecycleOwner) {
                lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                    vm.loadData()
                }
            }
            HomeScreen(
                viewModel = vm,
                onStartWorkout = { navController.navigate(Routes.LOG_WORKOUT) },
                onOpenPlanner = { navController.navigate(Routes.PLANNER) },
                onOpenSecurity = { navController.navigate(Routes.SECURITY_SETTINGS) }
            )
        }

        composable(Screen.Calendar.route) {
            val vm: CalendarViewModel = viewModel(factory = CalendarViewModel.Factory(repository))
            val lifecycleOwner = LocalLifecycleOwner.current
            LaunchedEffect(lifecycleOwner) {
                lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                    vm.refresh()
                }
            }
            CalendarScreen(
                viewModel = vm,
                onLogWorkout = { navController.navigate(Routes.LOG_WORKOUT) },
                onEditWorkout = { sessionId -> navController.navigate(Routes.editWorkout(sessionId)) },
                onViewWorkout = { sessionId -> navController.navigate(Routes.viewWorkout(sessionId)) }
            )
        }

        composable(Screen.Library.route) {
            val vm: ExerciseLibraryViewModel = viewModel(factory = ExerciseLibraryViewModel.Factory(repository))
            ExerciseLibraryScreen(viewModel = vm)
        }

        composable(Screen.Stats.route) {
            val vm: StatsViewModel = viewModel(factory = StatsViewModel.Factory(repository))
            StatsScreen(
                viewModel = vm,
                onSeeAllInsights = { navController.navigate(Routes.INSIGHTS) }
            )
        }

        composable(Routes.LOG_WORKOUT) {
            val vm: LogWorkoutViewModel = viewModel(factory = LogWorkoutViewModel.Factory(application, repository, null))
            LogWorkoutScreen(
                viewModel = vm,
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            Routes.EDIT_WORKOUT,
            arguments = listOf(navArgument("sessionId") { type = NavType.IntType })
        ) { backStackEntry ->
            val sessionId = backStackEntry.arguments?.getInt("sessionId")
            val vm: LogWorkoutViewModel = viewModel(factory = LogWorkoutViewModel.Factory(application, repository, sessionId))
            LogWorkoutScreen(
                viewModel = vm,
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            Routes.VIEW_WORKOUT,
            arguments = listOf(navArgument("sessionId") { type = NavType.IntType })
        ) { backStackEntry ->
            val sessionId = backStackEntry.arguments?.getInt("sessionId") ?: return@composable
            ViewWorkoutScreen(
                sessionId = sessionId,
                repository = repository,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.PLANNER) {
            val vm: WeeklyPlannerViewModel = viewModel(factory = WeeklyPlannerViewModel.Factory(repository))
            WeeklyPlannerScreen(
                viewModel = vm,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.INSIGHTS) {
            val vm: InsightsViewModel = viewModel(factory = InsightsViewModel.Factory(repository))
            InsightsScreen(
                viewModel = vm,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.SECURITY_SETTINGS) {
            SecuritySettingsScreen(
                onBack = { navController.popBackStack() }
            )
        }
    }
}
