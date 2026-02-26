package com.akhara.ui.screens.workout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.akhara.data.MuscleGroup
import com.akhara.data.db.entity.Exercise
import com.akhara.data.db.entity.PlannedExercise
import com.akhara.data.db.entity.WorkoutSession
import com.akhara.data.db.entity.WorkoutSet
import com.akhara.data.repository.WorkoutRepository
import com.akhara.ui.components.SetData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate

data class ExerciseEntry(
    val exercise: Exercise,
    val sets: List<SetData> = listOf(SetData(setNumber = 1)),
    val plannedSets: Int? = null,
    val plannedReps: Int? = null,
    val plannedWeight: Float? = null
)

data class LogWorkoutUiState(
    val todayMuscleGroups: List<String> = emptyList(),
    val availableExercises: List<Exercise> = emptyList(),
    val selectedExercises: List<ExerciseEntry> = emptyList(),
    val searchQuery: String = "",
    val pickerMuscleFilter: String? = null,
    val allMuscleGroups: List<String> = MuscleGroup.entries.map { it.displayName },
    val showExercisePicker: Boolean = false,
    val isSaving: Boolean = false,
    val isSaved: Boolean = false,
    val hasPlan: Boolean = false,
    val isEditMode: Boolean = false
)

class LogWorkoutViewModel(
    private val repository: WorkoutRepository,
    private val editSessionId: Int? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow(LogWorkoutUiState())
    val uiState: StateFlow<LogWorkoutUiState> = _uiState.asStateFlow()

    private var todayPlannedExercises: List<PlannedExercise> = emptyList()
    private var existingSession: WorkoutSession? = null

    init {
        if (editSessionId != null) {
            loadExistingSession(editSessionId)
        } else {
            loadTodayPlan()
        }
    }

    private fun loadExistingSession(sessionId: Int) {
        viewModelScope.launch {
            val session = repository.getSessionById(sessionId) ?: return@launch
            existingSession = session
            val sets = repository.getSetsForSessionSync(sessionId)

            val exerciseGroups = sets.groupBy { it.exerciseId }
            val entries = exerciseGroups.mapNotNull { (exerciseId, exerciseSets) ->
                val exercise = repository.getExerciseByIdSync(exerciseId) ?: return@mapNotNull null
                val setDataList = exerciseSets.map { ws ->
                    SetData(
                        setNumber = ws.setNumber,
                        reps = if (ws.reps > 0) ws.reps.toString() else "",
                        weight = if (ws.weight > 0f) {
                            if (ws.weight == ws.weight.toInt().toFloat()) ws.weight.toInt().toString()
                            else ws.weight.toString()
                        } else "",
                        restSeconds = if (ws.restSeconds > 0) ws.restSeconds.toString() else "",
                        plannedReps = ws.plannedReps,
                        plannedWeight = ws.plannedWeight
                    )
                }
                ExerciseEntry(exercise = exercise, sets = setDataList)
            }

            _uiState.value = _uiState.value.copy(
                selectedExercises = entries,
                isEditMode = true
            )
            loadExercises(emptyList())
        }
    }

    private fun loadTodayPlan() {
        viewModelScope.launch {
            val todayDow = LocalDate.now().dayOfWeek.value
            val plan = repository.getPlanForDay(todayDow)
            val muscles = plan?.muscleGroups?.split(",")?.filter { it.isNotBlank() } ?: emptyList()

            todayPlannedExercises = repository.getPlannedExercisesForDay(todayDow)

            if (todayPlannedExercises.isNotEmpty()) {
                val allExercises = repository.getAllExercises().first()
                val preFilled = todayPlannedExercises.mapNotNull { pe ->
                    val exercise = allExercises.find { it.id == pe.exerciseId } ?: return@mapNotNull null
                    val sets = (1..pe.targetSets).map { setNum ->
                        SetData(
                            setNumber = setNum,
                            reps = pe.targetReps.toString(),
                            weight = if (pe.targetWeight > 0f) {
                                if (pe.targetWeight == pe.targetWeight.toInt().toFloat())
                                    pe.targetWeight.toInt().toString()
                                else pe.targetWeight.toString()
                            } else "",
                            plannedReps = pe.targetReps,
                            plannedWeight = if (pe.targetWeight > 0f) pe.targetWeight else null
                        )
                    }
                    ExerciseEntry(
                        exercise = exercise,
                        sets = sets,
                        plannedSets = pe.targetSets,
                        plannedReps = pe.targetReps,
                        plannedWeight = pe.targetWeight
                    )
                }
                _uiState.value = _uiState.value.copy(
                    todayMuscleGroups = muscles,
                    selectedExercises = preFilled,
                    hasPlan = true
                )
            } else {
                _uiState.value = _uiState.value.copy(todayMuscleGroups = muscles)
            }

            loadExercises(muscles)
        }
    }

    private fun loadExercises(muscles: List<String>) {
        viewModelScope.launch {
            val flow = if (muscles.isNotEmpty()) {
                repository.getExercisesByMuscleGroups(muscles)
            } else {
                repository.getAllExercises()
            }
            flow.collect { exercises ->
                _uiState.value = _uiState.value.copy(availableExercises = exercises)
            }
        }
    }

    fun searchExercises(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
        viewModelScope.launch {
            if (query.isBlank()) {
                val filter = _uiState.value.pickerMuscleFilter
                if (filter != null) {
                    repository.getExercisesByMuscleGroup(filter).collect { exercises ->
                        _uiState.value = _uiState.value.copy(availableExercises = exercises)
                    }
                } else {
                    loadExercises(_uiState.value.todayMuscleGroups)
                }
            } else {
                repository.searchExercises(query).collect { exercises ->
                    _uiState.value = _uiState.value.copy(availableExercises = exercises)
                }
            }
        }
    }

    fun filterPickerByMuscleGroup(group: String?) {
        _uiState.value = _uiState.value.copy(pickerMuscleFilter = group, searchQuery = "")
        viewModelScope.launch {
            if (group != null) {
                repository.getExercisesByMuscleGroup(group).collect { exercises ->
                    _uiState.value = _uiState.value.copy(availableExercises = exercises)
                }
            } else {
                loadExercises(_uiState.value.todayMuscleGroups)
            }
        }
    }

    fun toggleExercisePicker(show: Boolean) {
        _uiState.value = _uiState.value.copy(
            showExercisePicker = show,
            pickerMuscleFilter = null,
            searchQuery = ""
        )
        if (show) loadExercises(_uiState.value.todayMuscleGroups)
    }

    fun addExercise(exercise: Exercise) {
        val current = _uiState.value.selectedExercises.toMutableList()
        if (current.none { it.exercise.id == exercise.id }) {
            current.add(ExerciseEntry(exercise = exercise))
            _uiState.value = _uiState.value.copy(
                selectedExercises = current,
                showExercisePicker = false
            )
        }
    }

    fun removeExercise(index: Int) {
        val current = _uiState.value.selectedExercises.toMutableList()
        if (index in current.indices) {
            current.removeAt(index)
            _uiState.value = _uiState.value.copy(selectedExercises = current)
        }
    }

    fun addSet(exerciseIndex: Int) {
        val current = _uiState.value.selectedExercises.toMutableList()
        if (exerciseIndex in current.indices) {
            val entry = current[exerciseIndex]
            val newSets = entry.sets + SetData(setNumber = entry.sets.size + 1)
            current[exerciseIndex] = entry.copy(sets = newSets)
            _uiState.value = _uiState.value.copy(selectedExercises = current)
        }
    }

    fun removeSet(exerciseIndex: Int, setIndex: Int) {
        val current = _uiState.value.selectedExercises.toMutableList()
        if (exerciseIndex in current.indices) {
            val entry = current[exerciseIndex]
            val newSets = entry.sets.toMutableList()
            if (setIndex in newSets.indices && newSets.size > 1) {
                newSets.removeAt(setIndex)
                newSets.forEachIndexed { i, s -> newSets[i] = s.copy(setNumber = i + 1) }
                current[exerciseIndex] = entry.copy(sets = newSets)
                _uiState.value = _uiState.value.copy(selectedExercises = current)
            }
        }
    }

    fun updateSet(exerciseIndex: Int, setIndex: Int, setData: SetData) {
        val current = _uiState.value.selectedExercises.toMutableList()
        if (exerciseIndex in current.indices) {
            val entry = current[exerciseIndex]
            val newSets = entry.sets.toMutableList()
            if (setIndex in newSets.indices) {
                newSets[setIndex] = setData
                current[exerciseIndex] = entry.copy(sets = newSets)
                _uiState.value = _uiState.value.copy(selectedExercises = current)
            }
        }
    }

    fun saveWorkout() {
        if (_uiState.value.selectedExercises.isEmpty()) return
        _uiState.value = _uiState.value.copy(isSaving = true)

        viewModelScope.launch {
            val now = System.currentTimeMillis()

            val allSets = mutableListOf<WorkoutSet>()
            _uiState.value.selectedExercises.forEach { entry ->
                entry.sets.forEach { setData ->
                    val reps = setData.reps.toIntOrNull() ?: 0
                    val weight = setData.weight.toFloatOrNull() ?: 0f
                    val rest = setData.restSeconds.toIntOrNull() ?: 0
                    if (reps > 0) {
                        allSets.add(
                            WorkoutSet(
                                sessionId = 0,
                                exerciseId = entry.exercise.id,
                                setNumber = setData.setNumber,
                                reps = reps,
                                weight = weight,
                                restSeconds = rest,
                                plannedReps = setData.plannedReps,
                                plannedWeight = setData.plannedWeight,
                                completedAt = now
                            )
                        )
                    }
                }
            }

            if (_uiState.value.isEditMode && existingSession != null) {
                repository.updateWorkout(existingSession!!, allSets)
            } else {
                val session = WorkoutSession(date = repository.todayEpochMillis())
                repository.saveWorkout(session, allSets)
            }
            _uiState.value = _uiState.value.copy(isSaving = false, isSaved = true)
        }
    }

    class Factory(
        private val repository: WorkoutRepository,
        private val editSessionId: Int?
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return LogWorkoutViewModel(repository, editSessionId) as T
        }
    }
}
