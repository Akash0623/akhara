package com.akhara.service

data class WorkoutServiceState(
    val isActive: Boolean = false,
    val exerciseName: String = "",
    val exerciseIndex: Int = 0,
    val totalExercises: Int = 0,
    val currentSet: Int = 1,
    val totalSets: Int = 1,
    val targetReps: String = "",
    val adjustedReps: Int = 0,
    val targetWeight: String = "",
    val isResting: Boolean = false,
    val restSecondsRemaining: Int = 0
)
