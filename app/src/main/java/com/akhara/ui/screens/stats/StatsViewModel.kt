package com.akhara.ui.screens.stats

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

enum class StatsPeriod(val label: String) {
    WEEK("This Week"),
    MONTH("This Month"),
    ALL("All Time")
}

enum class DayStatus { COMPLETED, MISSED, REST, FUTURE }

data class StatsUiState(
    val selectedPeriod: StatsPeriod = StatsPeriod.WEEK,
    val totalWorkouts: Int = 0,
    val totalVolume: String = "0 kg",
    val avgRestTime: String = "0s",
    val mostTrainedMuscle: String = "-",
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val weeklyGoal: Int = 5,
    val dailyVolumes: List<Float> = emptyList(),
    val dailyLabels: List<String> = listOf("M", "T", "W", "T", "F", "S", "S"),
    val weeklyVolumes: List<Float> = emptyList(),
    val weeklyLabels: List<String> = emptyList(),
    val topInsights: List<Insight> = emptyList(),
    val bodyWeightData: List<Float> = emptyList(),
    val bodyWeightLabels: List<String> = emptyList(),
    val dailyStatuses: List<DayStatus> = emptyList()
)

class StatsViewModel(private val repository: WorkoutRepository) : ViewModel() {

    private val insightEngine = InsightEngine(repository)

    private val _uiState = MutableStateFlow(StatsUiState())
    val uiState: StateFlow<StatsUiState> = _uiState.asStateFlow()

    init {
        loadStats(StatsPeriod.WEEK)
    }

    fun selectPeriod(period: StatsPeriod) {
        _uiState.value = _uiState.value.copy(selectedPeriod = period)
        loadStats(period)
    }

    private fun loadStats(period: StatsPeriod) {
        viewModelScope.launch {
            val now = LocalDate.now()
            val (startDate, endDate) = when (period) {
                StatsPeriod.WEEK -> {
                    val monday = now.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                    Pair(
                        repository.dateToEpochMillis(monday),
                        repository.dateToEpochMillis(monday.plusDays(6)) + 86400000L - 1
                    )
                }
                StatsPeriod.MONTH -> {
                    val firstDay = now.withDayOfMonth(1)
                    val lastDay = now.withDayOfMonth(now.lengthOfMonth())
                    Pair(
                        repository.dateToEpochMillis(firstDay),
                        repository.dateToEpochMillis(lastDay) + 86400000L - 1
                    )
                }
                StatsPeriod.ALL -> Pair(0L, Long.MAX_VALUE)
            }

            repository.getSessionsForDateRange(startDate, endDate).collect { sessions ->
                val totalWorkouts = sessions.size

                val volume = repository.getTotalVolumeForRange(startDate, endDate) ?: 0f
                val volumeStr = when {
                    volume >= 1000 -> String.format("%.1fk kg", volume / 1000)
                    else -> String.format("%.0f kg", volume)
                }

                val avgRest = repository.getAvgRestTimeForRange(startDate, endDate)
                val mostTrained = repository.getMostTrainedMuscleGroup(startDate, endDate) ?: "-"

                val (currentStreak, longestStreak) = repository.calculateStreak()

                val settings = repository.getSettingsSync()
                val goal = settings?.weeklyGoal ?: 5

                val dailyVolumes = calculateDailyVolumes()
                val dailyStatuses = calculateDailyStatuses(dailyVolumes)
                val weekCount = 8
                val weeklyVolumes = calculateWeeklyVolumes(weekCount)
                val weeklyLabels = (1..weekCount).map { "W$it" }
                val topInsights = try { insightEngine.generateAllInsights().take(3) } catch (_: Exception) { emptyList() }

                val (bwData, bwLabels) = loadBodyWeightTrend()

                _uiState.value = StatsUiState(
                    selectedPeriod = period,
                    totalWorkouts = totalWorkouts,
                    totalVolume = volumeStr,
                    avgRestTime = "${avgRest?.toInt() ?: 0}s",
                    mostTrainedMuscle = mostTrained,
                    currentStreak = currentStreak,
                    longestStreak = longestStreak,
                    weeklyGoal = goal,
                    dailyVolumes = dailyVolumes,
                    weeklyVolumes = weeklyVolumes,
                    weeklyLabels = weeklyLabels,
                    topInsights = topInsights,
                    bodyWeightData = bwData,
                    bodyWeightLabels = bwLabels,
                    dailyStatuses = dailyStatuses
                )
            }
        }
    }

    private suspend fun calculateDailyVolumes(): List<Float> {
        val monday = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        return (0L until 7L).map { dayOffset ->
            val day = monday.plusDays(dayOffset)
            val (start, end) = repository.dayBounds(day)
            repository.getDailyVolume(start, end) ?: 0f
        }
    }

    private suspend fun calculateDailyStatuses(volumes: List<Float>): List<DayStatus> {
        val monday = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        val today = LocalDate.now()
        return (0 until 7).map { offset ->
            val day = monday.plusDays(offset.toLong())
            val dow = day.dayOfWeek.value
            val hasVolume = volumes.getOrElse(offset) { 0f } > 0f
            val hasPlan = repository.getPlanForDay(dow)?.muscleGroups?.isNotBlank() == true ||
                    repository.getPlannedExercisesForDay(dow).isNotEmpty()
            when {
                day.isAfter(today) -> DayStatus.FUTURE
                hasVolume -> DayStatus.COMPLETED
                hasPlan -> DayStatus.MISSED
                else -> DayStatus.REST
            }
        }
    }

    private suspend fun loadBodyWeightTrend(): Pair<List<Float>, List<String>> {
        val now = LocalDate.now()
        val eightWeeksAgo = now.minusWeeks(8)
        val start = repository.dateToEpochMillis(eightWeeksAgo)
        val end = repository.dateToEpochMillis(now) + 86400000L - 1
        val entries = repository.getBodyWeightsForRange(start, end)
        if (entries.isEmpty()) return Pair(emptyList(), emptyList())
        val fmt = DateTimeFormatter.ofPattern("d/M")
        val data = entries.map { it.weightKg }
        val labels = entries.map { repository.epochMillisToDate(it.date).format(fmt) }
        return Pair(data, labels)
    }

    private suspend fun calculateWeeklyVolumes(weeks: Int): List<Float> {
        val volumes = mutableListOf<Float>()
        var weekStart = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        for (i in 0 until weeks) {
            val start = repository.dateToEpochMillis(weekStart)
            val end = repository.dateToEpochMillis(weekStart.plusDays(6)) + 86400000L - 1
            val vol = repository.getTotalVolumeForRange(start, end) ?: 0f
            volumes.add(0, vol)
            weekStart = weekStart.minusWeeks(1)
        }
        return volumes
    }

    class Factory(private val repository: WorkoutRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return StatsViewModel(repository) as T
        }
    }
}
