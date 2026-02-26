package com.akhara.ui.screens.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.akhara.data.db.entity.WorkoutSession
import com.akhara.data.db.entity.WorkoutSet
import com.akhara.data.repository.WorkoutRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth

data class CalendarUiState(
    val yearMonth: YearMonth = YearMonth.now(),
    val workoutDays: Set<LocalDate> = emptySet(),
    val selectedDate: LocalDate? = null,
    val selectedDaySessions: List<WorkoutSession> = emptyList(),
    val selectedSessionSets: Map<Int, List<WorkoutSet>> = emptyMap()
)

class CalendarViewModel(private val repository: WorkoutRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(CalendarUiState())
    val uiState: StateFlow<CalendarUiState> = _uiState.asStateFlow()

    init {
        loadMonth(YearMonth.now())
    }

    fun loadMonth(yearMonth: YearMonth) {
        viewModelScope.launch {
            val firstDay = yearMonth.atDay(1)
            val lastDay = yearMonth.atEndOfMonth()
            val start = repository.dateToEpochMillis(firstDay)
            val end = repository.dateToEpochMillis(lastDay) + 86400000L - 1

            repository.getSessionsForDateRange(start, end).collect { sessions ->
                val days = sessions.map { repository.epochMillisToDate(it.date) }.toSet()
                _uiState.value = _uiState.value.copy(
                    yearMonth = yearMonth,
                    workoutDays = days
                )
            }
        }
    }

    fun previousMonth() {
        val prev = _uiState.value.yearMonth.minusMonths(1)
        _uiState.value = _uiState.value.copy(yearMonth = prev, selectedDate = null, selectedDaySessions = emptyList())
        loadMonth(prev)
    }

    fun nextMonth() {
        val next = _uiState.value.yearMonth.plusMonths(1)
        _uiState.value = _uiState.value.copy(yearMonth = next, selectedDate = null, selectedDaySessions = emptyList())
        loadMonth(next)
    }

    fun selectDate(date: LocalDate) {
        _uiState.value = _uiState.value.copy(selectedDate = date)
        viewModelScope.launch {
            val start = repository.dateToEpochMillis(date)
            val end = start + 86400000L - 1
            val sessions = repository.getSessionsBetweenSync(start, end)
            val setsMap = mutableMapOf<Int, List<WorkoutSet>>()
            sessions.forEach { session ->
                setsMap[session.id] = repository.getSetsForSessionSync(session.id)
            }
            _uiState.value = _uiState.value.copy(
                selectedDaySessions = sessions,
                selectedSessionSets = setsMap
            )
        }
    }

    fun refresh() {
        loadMonth(_uiState.value.yearMonth)
    }

    class Factory(private val repository: WorkoutRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return CalendarViewModel(repository) as T
        }
    }
}
