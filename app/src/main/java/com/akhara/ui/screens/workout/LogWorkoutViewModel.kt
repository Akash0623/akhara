package com.akhara.ui.screens.workout

import android.app.Application
import android.app.NotificationManager
import android.content.Intent
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.akhara.data.MuscleGroup
import com.akhara.data.db.entity.Exercise
import com.akhara.data.db.entity.PlannedExercise
import com.akhara.data.db.entity.WorkoutSession
import com.akhara.data.db.entity.WorkoutSet
import com.akhara.data.repository.WorkoutRepository
import com.akhara.service.WorkoutService
import com.akhara.ui.components.SetData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.time.LocalDate

data class ExerciseEntry(
    val exercise: Exercise,
    val sets: List<SetData> = listOf(SetData(setNumber = 1)),
    val plannedSets: Int? = null,
    val plannedReps: Int? = null,
    val plannedWeight: Float? = null,
    val isDone: Boolean = false
)

sealed interface NotificationDialogType {
    data object PermissionRationale : NotificationDialogType
    data object LockScreenGuidance : NotificationDialogType
}

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
    val isEditMode: Boolean = false,
    val hasAnySaved: Boolean = false,
    val notificationDialogType: NotificationDialogType? = null
)

class LogWorkoutViewModel(
    private val application: Application,
    private val repository: WorkoutRepository,
    private val editSessionId: Int? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow(LogWorkoutUiState())
    val uiState: StateFlow<LogWorkoutUiState> = _uiState.asStateFlow()

    private var todayPlannedExercises: List<PlannedExercise> = emptyList()
    private var existingSession: WorkoutSession? = null
    private var activeSessionId: Int? = null
    private var lastProcessedEventCount = 0
    private val saveMutex = Mutex()

    init {
        // Clear stale events from any previous workout service run
        WorkoutService.clearCompletedSets()
        lastProcessedEventCount = 0

        if (editSessionId != null) {
            loadExistingSession(editSessionId)
        } else {
            loadTodayPlan()
        }
        observeServiceCompletedSets()
    }

    private fun observeServiceCompletedSets() {
        viewModelScope.launch {
            WorkoutService.completedSets.collect { events ->
                if (events.size <= lastProcessedEventCount) return@collect
                val newEvents = events.subList(lastProcessedEventCount, events.size)
                lastProcessedEventCount = events.size

                val current = _uiState.value.selectedExercises.toMutableList()
                for (event in newEvents) {
                    val idx = current.indexOfFirst { it.exercise.id == event.exerciseId }
                    if (idx >= 0) {
                        val entry = current[idx]
                        if (event.setIndex in entry.sets.indices) {
                            val newSets = entry.sets.toMutableList()
                            newSets[event.setIndex] = newSets[event.setIndex].copy(
                                reps = event.actualReps.toString()
                            )
                            current[idx] = entry.copy(sets = newSets)
                        }
                    }
                }
                _uiState.value = _uiState.value.copy(selectedExercises = current)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopWorkoutService()
        WorkoutService.clearCompletedSets()
    }

    private fun loadExistingSession(sessionId: Int) {
        viewModelScope.launch {
            val session = repository.getSessionById(sessionId) ?: return@launch
            existingSession = session
            activeSessionId = sessionId
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

            // Restore activeSessionId from an existing session created today
            val (dayStart, dayEnd) = repository.dayBounds(LocalDate.now())
            val todaySessions = repository.getSessionsBetweenSync(dayStart, dayEnd)
            if (todaySessions.isNotEmpty()) {
                activeSessionId = todaySessions.last().id
                _uiState.value = _uiState.value.copy(hasAnySaved = true)
            }

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
        // Close picker immediately to prevent double-tap race
        val current = _uiState.value.selectedExercises
        if (current.any { it.exercise.id == exercise.id }) return
        _uiState.value = _uiState.value.copy(showExercisePicker = false)

        viewModelScope.launch {
            val lastSets = repository.getLastSetsForExercise(exercise.id)
            val entry = if (lastSets.isNotEmpty()) {
                val setDataList = lastSets.map { ws ->
                    SetData(
                        setNumber = ws.setNumber,
                        reps = if (ws.reps > 0) ws.reps.toString() else "",
                        weight = if (ws.weight > 0f) {
                            if (ws.weight == ws.weight.toInt().toFloat()) ws.weight.toInt().toString()
                            else ws.weight.toString()
                        } else "",
                        lastReps = if (ws.reps > 0) ws.reps else null,
                        lastWeight = if (ws.weight > 0f) ws.weight else null
                    )
                }
                ExerciseEntry(exercise = exercise, sets = setDataList)
            } else {
                ExerciseEntry(exercise = exercise)
            }
            // Final duplicate check after async gap
            val updated = _uiState.value.selectedExercises.toMutableList()
            if (updated.none { it.exercise.id == exercise.id }) {
                updated.add(entry)
                _uiState.value = _uiState.value.copy(selectedExercises = updated)
            }
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
            val lastSet = entry.sets.lastOrNull()
            val newSet = SetData(
                setNumber = entry.sets.size + 1,
                reps = lastSet?.reps ?: "",
                weight = lastSet?.weight ?: "",
                restSeconds = "" // Don't inherit rest on the new last set
            )
            current[exerciseIndex] = entry.copy(sets = entry.sets + newSet)
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

    fun toggleExerciseDone(exerciseIndex: Int) {
        val current = _uiState.value.selectedExercises.toMutableList()
        if (exerciseIndex in current.indices) {
            val entry = current[exerciseIndex]
            val newDone = !entry.isDone
            current[exerciseIndex] = entry.copy(isDone = newDone)
            _uiState.value = _uiState.value.copy(selectedExercises = current)

            if (newDone) {
                saveExerciseIncrementally(current[exerciseIndex])
            }
        }
    }

    private fun saveExerciseIncrementally(entry: ExerciseEntry) {
        viewModelScope.launch {
            try {
                saveMutex.withLock {
                    val now = System.currentTimeMillis()
                    val sets = entry.sets.mapNotNull { setData ->
                        val reps = setData.reps.toIntOrNull() ?: 0
                        if (reps > 0) {
                            WorkoutSet(
                                sessionId = 0,
                                exerciseId = entry.exercise.id,
                                setNumber = setData.setNumber,
                                reps = reps,
                                weight = setData.weight.toFloatOrNull() ?: 0f,
                                restSeconds = setData.restSeconds.toIntOrNull() ?: 0,
                                plannedReps = setData.plannedReps,
                                plannedWeight = setData.plannedWeight,
                                completedAt = now
                            )
                        } else null
                    }
                    if (sets.isEmpty()) return@withLock

                    val sessionId = activeSessionId
                    if (sessionId == null) {
                        val session = WorkoutSession(date = repository.todayEpochMillis())
                        val newId = repository.saveWorkout(session, sets)
                        activeSessionId = newId.toInt()
                    } else {
                        repository.replaceSetsForExercise(sessionId, entry.exercise.id, sets)
                    }
                    _uiState.value = _uiState.value.copy(hasAnySaved = true)
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                android.util.Log.e("LogWorkoutVM", "Failed to save exercise ${entry.exercise.name}", e)
                // Revert isDone so user sees it wasn't saved
                val reverted = _uiState.value.selectedExercises.toMutableList()
                val idx = reverted.indexOfFirst { it.exercise.id == entry.exercise.id }
                if (idx >= 0) {
                    reverted[idx] = reverted[idx].copy(isDone = false)
                    _uiState.value = _uiState.value.copy(selectedExercises = reverted)
                }
            }
        }
    }

    fun saveWorkout() {
        if (_uiState.value.selectedExercises.isEmpty()) return
        _uiState.value = _uiState.value.copy(isSaving = true)

        viewModelScope.launch {
            try {
                saveMutex.withLock {
                    val now = System.currentTimeMillis()

                    if (_uiState.value.isEditMode && existingSession != null) {
                        val allSets = buildSetsList(now)
                        repository.updateWorkout(existingSession!!, allSets)
                    } else {
                        val unsavedEntries = _uiState.value.selectedExercises.filter { !it.isDone }
                        for (entry in unsavedEntries) {
                            val sets = entry.sets.mapNotNull { setData ->
                                val reps = setData.reps.toIntOrNull() ?: 0
                                if (reps > 0) {
                                    WorkoutSet(
                                        sessionId = 0,
                                        exerciseId = entry.exercise.id,
                                        setNumber = setData.setNumber,
                                        reps = reps,
                                        weight = setData.weight.toFloatOrNull() ?: 0f,
                                        restSeconds = setData.restSeconds.toIntOrNull() ?: 0,
                                        plannedReps = setData.plannedReps,
                                        plannedWeight = setData.plannedWeight,
                                        completedAt = now
                                    )
                                } else null
                            }
                            if (sets.isEmpty()) continue

                            val sessionId = activeSessionId
                            if (sessionId == null) {
                                val session = WorkoutSession(date = repository.todayEpochMillis())
                                val newId = repository.saveWorkout(session, sets)
                                activeSessionId = newId.toInt()
                            } else {
                                repository.replaceSetsForExercise(sessionId, entry.exercise.id, sets)
                            }
                        }
                    }
                }
                stopWorkoutService()
                _uiState.value = _uiState.value.copy(isSaving = false, isSaved = true)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                android.util.Log.e("LogWorkoutVM", "Failed to save workout", e)
                _uiState.value = _uiState.value.copy(isSaving = false)
            }
        }
    }

    private fun buildSetsList(now: Long): List<WorkoutSet> {
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
        return allSets
    }

    fun showNotificationDialog(type: NotificationDialogType) {
        _uiState.value = _uiState.value.copy(notificationDialogType = type)
    }

    fun dismissNotificationDialog() {
        _uiState.value = _uiState.value.copy(notificationDialogType = null)
    }

    fun isNotificationPermissionNeeded(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(
                application,
                android.Manifest.permission.POST_NOTIFICATIONS
            ) != android.content.pm.PackageManager.PERMISSION_GRANTED
    }

    fun checkNotificationChannel(): Boolean {
        val nm = application.getSystemService(NotificationManager::class.java)
        val channel = nm.getNotificationChannel("akhara_workout") ?: return false
        return channel.importance != NotificationManager.IMPORTANCE_NONE
    }

    fun startWorkoutService() {
        // Guard: don't restart if already active
        if (WorkoutService.state.value.isActive) return
        val exercises = _uiState.value.selectedExercises
        if (exercises.isEmpty()) return

        // Encode using newlines and tabs (safe delimiters — exercise names won't contain these)
        val encoded = exercises.joinToString("\n\n") { entry ->
            val setsStr = entry.sets.joinToString("\t") { s ->
                "${s.setNumber},${s.reps},${s.weight},${s.restSeconds}"
            }
            "${entry.exercise.name}\n${entry.exercise.id}\n$setsStr"
        }

        val intent = Intent(application, WorkoutService::class.java).apply {
            putExtra(WorkoutService.EXTRA_EXERCISES, encoded)
        }
        try {
            application.startForegroundService(intent)
        } catch (e: Exception) {
            android.util.Log.e("LogWorkoutVM", "Failed to start workout service", e)
        }
    }

    fun stopWorkoutService() {
        if (!WorkoutService.state.value.isActive) return
        try {
            val intent = Intent(application, WorkoutService::class.java).apply {
                action = WorkoutService.ACTION_FINISH
            }
            application.startService(intent)
        } catch (_: Exception) {
            // Service not running
        }
    }

    class Factory(
        private val application: Application,
        private val repository: WorkoutRepository,
        private val editSessionId: Int?
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return LogWorkoutViewModel(application, repository, editSessionId) as T
        }
    }
}
