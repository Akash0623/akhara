package com.akhara.ui.screens.exercises

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.akhara.data.MuscleGroup
import com.akhara.data.db.entity.Exercise
import com.akhara.data.repository.WorkoutRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ExerciseLibraryUiState(
    val exercises: List<Exercise> = emptyList(),
    val searchQuery: String = "",
    val selectedMuscleGroup: String? = null,
    val muscleGroups: List<String> = MuscleGroup.entries.map { it.displayName },
    val showAddDialog: Boolean = false,
    val newExerciseName: String = "",
    val newExerciseMuscleGroup: String = MuscleGroup.CHEST.displayName
)

class ExerciseLibraryViewModel(private val repository: WorkoutRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(ExerciseLibraryUiState())
    val uiState: StateFlow<ExerciseLibraryUiState> = _uiState.asStateFlow()

    init {
        loadExercises()
    }

    private fun loadExercises() {
        viewModelScope.launch {
            repository.getAllExercises().collect { exercises ->
                _uiState.value = _uiState.value.copy(exercises = exercises)
            }
        }
    }

    fun search(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
        viewModelScope.launch {
            if (query.isBlank() && _uiState.value.selectedMuscleGroup == null) {
                repository.getAllExercises().collect {
                    _uiState.value = _uiState.value.copy(exercises = it)
                }
            } else if (query.isNotBlank()) {
                repository.searchExercises(query).collect {
                    _uiState.value = _uiState.value.copy(exercises = it)
                }
            }
        }
    }

    fun filterByMuscleGroup(group: String?) {
        _uiState.value = _uiState.value.copy(selectedMuscleGroup = group, searchQuery = "")
        viewModelScope.launch {
            if (group != null) {
                repository.getExercisesByMuscleGroup(group).collect {
                    _uiState.value = _uiState.value.copy(exercises = it)
                }
            } else {
                repository.getAllExercises().collect {
                    _uiState.value = _uiState.value.copy(exercises = it)
                }
            }
        }
    }

    fun showAddDialog(show: Boolean) {
        _uiState.value = _uiState.value.copy(showAddDialog = show)
    }

    fun updateNewExerciseName(name: String) {
        _uiState.value = _uiState.value.copy(newExerciseName = name)
    }

    fun updateNewExerciseMuscleGroup(group: String) {
        _uiState.value = _uiState.value.copy(newExerciseMuscleGroup = group)
    }

    fun addCustomExercise() {
        val name = _uiState.value.newExerciseName.trim()
        if (name.isBlank()) return
        viewModelScope.launch {
            repository.addCustomExercise(name, _uiState.value.newExerciseMuscleGroup)
            _uiState.value = _uiState.value.copy(
                showAddDialog = false,
                newExerciseName = "",
                newExerciseMuscleGroup = MuscleGroup.CHEST.displayName
            )
        }
    }

    class Factory(private val repository: WorkoutRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ExerciseLibraryViewModel(repository) as T
        }
    }
}
