package com.akhara.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.akhara.data.intelligence.Insight
import com.akhara.data.intelligence.InsightEngine
import com.akhara.data.repository.WorkoutRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters

data class HomeUiState(
    val greeting: String = "",
    val dateText: String = "",
    val weeklyWorkoutCount: Int = 0,
    val weeklyGoal: Int = 5,
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val todayMuscleGroups: List<String> = emptyList(),
    val weeklyVolume: String = "0",
    val avgRestTime: String = "0s",
    val lastWorkoutDate: String = "",
    val lastWorkoutMuscles: String = "",
    val lastWorkoutExercises: Int = 0,
    val lastWorkoutSets: Int = 0,
    val lastWorkoutAvgRest: Int = 0,
    val hasLastWorkout: Boolean = false,
    val topInsights: List<Insight> = emptyList(),
    val latestBodyWeight: String = "",
    val bodyWeightInput: String = ""
)

class HomeViewModel(private val repository: WorkoutRepository) : ViewModel() {

    private val insightEngine = InsightEngine(repository)
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            val now = LocalDate.now()
            val hour = java.time.LocalTime.now().hour
            val greeting = when {
                hour < 12 -> "Good morning"
                hour < 17 -> "Good afternoon"
                else -> "Good evening"
            }
            val dateText = now.format(DateTimeFormatter.ofPattern("EEEE, MMM d"))

            val weeklyCount = repository.getWorkoutCountForWeek()

            val settings = repository.getSettings()
            var weeklyGoal = 5
            val settingsJob = viewModelScope.launch {
                settings.collect { s ->
                    weeklyGoal = s?.weeklyGoal ?: 5
                }
            }

            val (currentStreak, longestStreak) = repository.calculateStreak()

            val todayDayOfWeek = now.dayOfWeek.value
            val todayPlan = repository.getPlanForDay(todayDayOfWeek)
            val todayMuscles = todayPlan?.muscleGroups?.split(",")?.filter { it.isNotBlank() } ?: emptyList()

            val monday = now.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
            val sunday = monday.plusDays(6)
            val weekStart = repository.dateToEpochMillis(monday)
            val weekEnd = repository.dateToEpochMillis(sunday) + 86400000L - 1

            val volume = repository.getTotalVolumeForRange(weekStart, weekEnd) ?: 0f
            val avgRest = repository.getAvgRestTimeForRange(weekStart, weekEnd)

            val volumeStr = when {
                volume >= 1000 -> String.format("%.1fk", volume / 1000)
                else -> String.format("%.0f", volume)
            }

            settingsJob.cancel()

            val topInsights = try { insightEngine.generateAllInsights().take(2) } catch (_: Exception) { emptyList() }

            val latestBw = repository.getLatestBodyWeight()
            val bwStr = if (latestBw != null) String.format("%.1f kg", latestBw.weightKg) else ""

            _uiState.value = HomeUiState(
                greeting = greeting,
                dateText = dateText,
                weeklyWorkoutCount = weeklyCount,
                weeklyGoal = weeklyGoal,
                currentStreak = currentStreak,
                longestStreak = longestStreak,
                todayMuscleGroups = todayMuscles,
                weeklyVolume = "$volumeStr kg",
                avgRestTime = "${avgRest?.toInt() ?: 0}s",
                topInsights = topInsights,
                latestBodyWeight = bwStr
            )
        }
    }

    fun updateWeeklyGoal(goal: Int) {
        viewModelScope.launch {
            repository.updateWeeklyGoal(goal)
            loadData()
        }
    }

    fun onBodyWeightInputChange(value: String) {
        _uiState.value = _uiState.value.copy(bodyWeightInput = value)
    }

    fun logBodyWeight() {
        val weight = _uiState.value.bodyWeightInput.toFloatOrNull() ?: return
        viewModelScope.launch {
            repository.logBodyWeight(weight)
            _uiState.value = _uiState.value.copy(
                latestBodyWeight = String.format("%.1f kg", weight),
                bodyWeightInput = ""
            )
        }
    }

    class Factory(private val repository: WorkoutRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return HomeViewModel(repository) as T
        }
    }
}
