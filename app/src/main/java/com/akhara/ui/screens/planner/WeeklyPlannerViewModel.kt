package com.akhara.ui.screens.planner

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.akhara.data.MuscleGroup
import com.akhara.data.db.entity.Exercise
import com.akhara.data.db.entity.PlannedExercise
import com.akhara.data.repository.WorkoutRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class PlannedExerciseEntry(
    val exercise: Exercise,
    val targetSets: String = "3",
    val targetReps: String = "10",
    val targetWeight: String = "0"
)

data class DayPlan(
    val dayOfWeek: Int,
    val dayName: String,
    val muscleGroups: List<String> = emptyList(),
    val plannedExercises: List<PlannedExerciseEntry> = emptyList(),
    val isExpanded: Boolean = false
)

data class PlannerUiState(
    val days: List<DayPlan> = listOf(
        DayPlan(1, "Monday"),
        DayPlan(2, "Tuesday"),
        DayPlan(3, "Wednesday"),
        DayPlan(4, "Thursday"),
        DayPlan(5, "Friday"),
        DayPlan(6, "Saturday"),
        DayPlan(7, "Sunday")
    ),
    val allMuscleGroups: List<String> = MuscleGroup.entries.map { it.displayName },
    val isSaved: Boolean = false,
    val availableExercises: List<Exercise> = emptyList(),
    val showExercisePicker: Boolean = false,
    val pickerDayOfWeek: Int = 0,
    val exerciseSearchQuery: String = ""
)

class WeeklyPlannerViewModel(private val repository: WorkoutRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(PlannerUiState())
    val uiState: StateFlow<PlannerUiState> = _uiState.asStateFlow()

    private var allExercisesCache: List<Exercise> = emptyList()

    init {
        loadPlan()
        loadExercises()
    }

    private fun loadExercises() {
        viewModelScope.launch {
            repository.getAllExercises().collect { exercises ->
                allExercisesCache = exercises
                _uiState.value = _uiState.value.copy(availableExercises = exercises)
            }
        }
    }

    private fun loadPlan() {
        viewModelScope.launch {
            repository.getWeeklyPlan().collect { plans ->
                val updatedDays = _uiState.value.days.map { day ->
                    val plan = plans.find { it.dayOfWeek == day.dayOfWeek }
                    val muscles = if (plan != null) {
                        plan.muscleGroups.split(",").filter { it.isNotBlank() }
                    } else emptyList()

                    val planned = repository.getPlannedExercisesForDay(day.dayOfWeek)
                    val entries = planned.mapNotNull { pe ->
                        val ex = allExercisesCache.find { it.id == pe.exerciseId }
                        if (ex != null) {
                            PlannedExerciseEntry(
                                exercise = ex,
                                targetSets = pe.targetSets.toString(),
                                targetReps = pe.targetReps.toString(),
                                targetWeight = pe.targetWeight.let { if (it == 0f) "0" else it.toString().trimEnd('0').trimEnd('.') }
                            )
                        } else null
                    }

                    day.copy(muscleGroups = muscles, plannedExercises = entries)
                }
                _uiState.value = _uiState.value.copy(days = updatedDays)
            }
        }
    }

    fun toggleMuscleGroup(dayOfWeek: Int, muscleGroup: String) {
        val updatedDays = _uiState.value.days.map { day ->
            if (day.dayOfWeek == dayOfWeek) {
                val groups = day.muscleGroups.toMutableList()
                if (muscleGroup in groups) groups.remove(muscleGroup)
                else groups.add(muscleGroup)
                day.copy(muscleGroups = groups)
            } else day
        }
        _uiState.value = _uiState.value.copy(days = updatedDays, isSaved = false)
    }

    fun toggleDayExpanded(dayOfWeek: Int) {
        val updatedDays = _uiState.value.days.map { day ->
            if (day.dayOfWeek == dayOfWeek) day.copy(isExpanded = !day.isExpanded)
            else day
        }
        _uiState.value = _uiState.value.copy(days = updatedDays)
    }

    fun openExercisePicker(dayOfWeek: Int) {
        val dayMuscles = _uiState.value.days
            .find { it.dayOfWeek == dayOfWeek }?.muscleGroups ?: emptyList()
        val filtered = if (dayMuscles.isNotEmpty()) {
            allExercisesCache.filter { it.muscleGroup in dayMuscles }
        } else {
            allExercisesCache
        }
        _uiState.value = _uiState.value.copy(
            showExercisePicker = true,
            pickerDayOfWeek = dayOfWeek,
            exerciseSearchQuery = "",
            availableExercises = filtered
        )
    }

    fun closeExercisePicker() {
        _uiState.value = _uiState.value.copy(showExercisePicker = false)
    }

    fun searchExercises(query: String) {
        _uiState.value = _uiState.value.copy(exerciseSearchQuery = query)
        val dayMuscles = _uiState.value.days
            .find { it.dayOfWeek == _uiState.value.pickerDayOfWeek }?.muscleGroups ?: emptyList()

        if (query.isBlank()) {
            val filtered = if (dayMuscles.isNotEmpty()) {
                allExercisesCache.filter { it.muscleGroup in dayMuscles }
            } else {
                allExercisesCache
            }
            _uiState.value = _uiState.value.copy(availableExercises = filtered)
        } else {
            viewModelScope.launch {
                repository.searchExercises(query).collect { exercises ->
                    val filtered = if (dayMuscles.isNotEmpty()) {
                        exercises.filter { it.muscleGroup in dayMuscles }
                    } else {
                        exercises
                    }
                    _uiState.value = _uiState.value.copy(availableExercises = filtered)
                }
            }
        }
    }

    fun addExerciseToDay(exercise: Exercise) {
        val dow = _uiState.value.pickerDayOfWeek
        val updatedDays = _uiState.value.days.map { day ->
            if (day.dayOfWeek == dow) {
                if (day.plannedExercises.none { it.exercise.id == exercise.id }) {
                    val updated = day.plannedExercises + PlannedExerciseEntry(exercise = exercise)
                    day.copy(plannedExercises = updated, isExpanded = true)
                } else day
            } else day
        }
        _uiState.value = _uiState.value.copy(
            days = updatedDays,
            showExercisePicker = false,
            isSaved = false
        )
    }

    fun removeExerciseFromDay(dayOfWeek: Int, exerciseIndex: Int) {
        val updatedDays = _uiState.value.days.map { day ->
            if (day.dayOfWeek == dayOfWeek) {
                val updated = day.plannedExercises.toMutableList()
                if (exerciseIndex in updated.indices) updated.removeAt(exerciseIndex)
                day.copy(plannedExercises = updated)
            } else day
        }
        _uiState.value = _uiState.value.copy(days = updatedDays, isSaved = false)
    }

    fun updateExerciseTarget(dayOfWeek: Int, exerciseIndex: Int, sets: String?, reps: String?, weight: String?) {
        val updatedDays = _uiState.value.days.map { day ->
            if (day.dayOfWeek == dayOfWeek) {
                val updated = day.plannedExercises.toMutableList()
                if (exerciseIndex in updated.indices) {
                    val entry = updated[exerciseIndex]
                    updated[exerciseIndex] = entry.copy(
                        targetSets = sets ?: entry.targetSets,
                        targetReps = reps ?: entry.targetReps,
                        targetWeight = weight ?: entry.targetWeight
                    )
                }
                day.copy(plannedExercises = updated)
            } else day
        }
        _uiState.value = _uiState.value.copy(days = updatedDays, isSaved = false)
    }

    fun savePlan() {
        viewModelScope.launch {
            _uiState.value.days.forEach { day ->
                repository.saveDayPlan(day.dayOfWeek, day.muscleGroups)

                val plannedEntities = day.plannedExercises.mapIndexed { idx, entry ->
                    PlannedExercise(
                        dayOfWeek = day.dayOfWeek,
                        exerciseId = entry.exercise.id,
                        targetSets = entry.targetSets.toIntOrNull() ?: 3,
                        targetReps = entry.targetReps.toIntOrNull() ?: 10,
                        targetWeight = entry.targetWeight.toFloatOrNull() ?: 0f,
                        orderIndex = idx
                    )
                }
                repository.savePlannedExercises(day.dayOfWeek, plannedEntities)
            }
            _uiState.value = _uiState.value.copy(isSaved = true)
        }
    }

    class Factory(private val repository: WorkoutRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return WeeklyPlannerViewModel(repository) as T
        }
    }
}
