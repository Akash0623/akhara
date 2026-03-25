package com.akhara.service

import android.app.Notification
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.CountDownTimer
import android.os.IBinder
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.support.v4.media.session.MediaSessionCompat
import com.akhara.ui.components.SetData
import com.akhara.ui.screens.workout.LockScreenWorkoutActivity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class ServiceExercise(
    val name: String,
    val exerciseId: Int,
    val sets: List<SetData>
)

class WorkoutService : Service() {

    companion object {
        const val ACTION_DONE_SET = "com.akhara.ACTION_DONE_SET"
        const val ACTION_SKIP = "com.akhara.ACTION_SKIP"
        const val ACTION_FINISH = "com.akhara.ACTION_FINISH"
        const val ACTION_REP_MINUS = "com.akhara.ACTION_REP_MINUS"
        const val ACTION_REP_PLUS = "com.akhara.ACTION_REP_PLUS"
        const val EXTRA_EXERCISES = "exercises_json"

        private val _state = MutableStateFlow(WorkoutServiceState())
        val state: StateFlow<WorkoutServiceState> = _state.asStateFlow()

        private val _completedSets = MutableStateFlow<List<CompletedSetEvent>>(emptyList())
        val completedSets: StateFlow<List<CompletedSetEvent>> = _completedSets.asStateFlow()

        fun clearCompletedSets() { _completedSets.value = emptyList() }
    }

    data class CompletedSetEvent(
        val exerciseIndex: Int,
        val setIndex: Int,
        val actualReps: Int = 0,
        val timestamp: Long = System.currentTimeMillis()
    )

    private var exercises: List<ServiceExercise> = emptyList()
    private var currentExerciseIdx = 0
    private var currentSetIdx = 0
    private var restTimer: CountDownTimer? = null
    private var isTimerFinishing = false
    private lateinit var mediaSession: MediaSessionCompat

    private val screenOnReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == Intent.ACTION_SCREEN_ON && _state.value.isActive) {
                launchLockScreenActivity()
            }
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        WorkoutNotificationManager.createChannel(this)
        mediaSession = WorkoutNotificationManager.getOrCreateMediaSession(this)
        mediaSession.setCallback(object : MediaSessionCompat.Callback() {
            override fun onPlay() { onDoneSet() }
            override fun onPause() { onDoneSet() }
            override fun onSkipToNext() { onSkipExercise() }
            override fun onSkipToPrevious() { onAdjustReps(-1) }
            override fun onStop() { onFinish() }
        })
        registerReceiver(screenOnReceiver, IntentFilter(Intent.ACTION_SCREEN_ON))
    }

    private fun launchLockScreenActivity() {
        val activityIntent = Intent(this, LockScreenWorkoutActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        startActivity(activityIntent)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Handle null intent restart (START_STICKY) — stop gracefully
        if (intent == null) {
            onFinish()
            return START_NOT_STICKY
        }

        when (intent.action) {
            ACTION_DONE_SET -> onDoneSet()
            ACTION_SKIP -> onSkipExercise()
            ACTION_FINISH -> onFinish()
            ACTION_REP_MINUS -> onAdjustReps(-1)
            ACTION_REP_PLUS -> onAdjustReps(1)
            else -> {
                val encoded = intent.getStringExtra(EXTRA_EXERCISES)
                if (encoded != null) {
                    exercises = parseExercises(encoded)
                    if (exercises.isEmpty()) {
                        stopSelf()
                        return START_NOT_STICKY
                    }
                    currentExerciseIdx = 0
                    currentSetIdx = 0
                    _completedSets.value = emptyList()
                    updateState()
                    WorkoutNotificationManager.updateMediaSession(mediaSession, _state.value)
                    val notification = WorkoutNotificationManager.buildNotification(this, _state.value, mediaSession)
                    startForegroundCompat(notification)
                }
            }
        }
        return START_NOT_STICKY
    }

    private fun startForegroundCompat(notification: Notification) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(
                WorkoutNotificationManager.NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
            )
        } else {
            startForeground(
                WorkoutNotificationManager.NOTIFICATION_ID,
                notification
            )
        }
    }

    private fun onDoneSet() {
        val currentState = _state.value
        if (!currentState.isActive || exercises.isEmpty()) return

        if (currentState.isResting) {
            // Skip rest — cancel timer and move to next set display
            cancelRestTimer()
            updateState()
            updateNotification()
            return
        }

        // Record the completed set with adjusted reps
        val events = _completedSets.value + CompletedSetEvent(
            currentExerciseIdx, currentSetIdx, currentState.adjustedReps
        )
        _completedSets.value = events

        val exercise = exercises.getOrNull(currentExerciseIdx) ?: return
        val currentSet = exercise.sets.getOrNull(currentSetIdx)
        val restSeconds = currentSet?.restSeconds?.toIntOrNull() ?: 0

        if (currentSetIdx < exercise.sets.lastIndex) {
            currentSetIdx++
            if (restSeconds > 0) {
                startRestTimer(restSeconds)
            } else {
                updateState()
                updateNotification()
            }
        } else {
            advanceToNextExercise()
        }
    }

    private fun onSkipExercise() {
        cancelRestTimer()
        advanceToNextExercise()
    }

    private fun onAdjustReps(delta: Int) {
        if (_state.value.isResting || !_state.value.isActive) return
        val current = _state.value.adjustedReps
        val newReps = (current + delta).coerceAtLeast(0)
        _state.value = _state.value.copy(adjustedReps = newReps)
        updateNotification()
    }

    private fun advanceToNextExercise() {
        if (currentExerciseIdx < exercises.lastIndex) {
            currentExerciseIdx++
            currentSetIdx = 0
            updateState()
            updateNotification()
        } else {
            onFinish()
        }
    }

    private fun onFinish() {
        cancelRestTimer()
        _state.value = WorkoutServiceState()
        WorkoutNotificationManager.releaseMediaSession()
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun cancelRestTimer() {
        restTimer?.cancel()
        restTimer = null
        isTimerFinishing = false
    }

    private fun startRestTimer(seconds: Int) {
        cancelRestTimer()
        isTimerFinishing = false

        _state.value = _state.value.copy(
            isResting = true,
            restSecondsRemaining = seconds
        )
        updateNotification()

        restTimer = object : CountDownTimer(seconds * 1000L, 1000L) {
            override fun onTick(millisUntilFinished: Long) {
                if (restTimer == null) return // timer was cancelled
                _state.value = _state.value.copy(
                    restSecondsRemaining = (millisUntilFinished / 1000).toInt() + 1
                )
                updateNotification()
            }

            override fun onFinish() {
                if (restTimer == null) return // timer was cancelled
                isTimerFinishing = true
                vibrateRestEnd()
                _state.value = _state.value.copy(isResting = false)
                updateState()
                updateNotification()
                isTimerFinishing = false
            }
        }.start()
    }

    private fun vibrateRestEnd() {
        try {
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val manager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                manager.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            }
            vibrator.vibrate(
                VibrationEffect.createWaveform(longArrayOf(0, 100, 80, 100, 80, 200), -1)
            )
        } catch (_: Exception) {
            // Vibration not available on this device
        }
    }

    private fun updateState() {
        val exercise = exercises.getOrNull(currentExerciseIdx) ?: return
        val setData = exercise.sets.getOrNull(currentSetIdx)
        val reps = setData?.reps?.toIntOrNull() ?: 0
        _state.value = WorkoutServiceState(
            isActive = true,
            exerciseName = exercise.name,
            exerciseIndex = currentExerciseIdx,
            totalExercises = exercises.size,
            currentSet = currentSetIdx + 1,
            totalSets = exercise.sets.size,
            targetReps = setData?.reps ?: "",
            adjustedReps = reps,
            targetWeight = setData?.weight ?: "",
            isResting = false,
            restSecondsRemaining = 0
        )
    }

    private fun updateNotification() {
        if (!_state.value.isActive) return
        try {
            WorkoutNotificationManager.updateMediaSession(mediaSession, _state.value)
            val notification = WorkoutNotificationManager.buildNotification(this, _state.value, mediaSession)
            val manager = getSystemService(NOTIFICATION_SERVICE) as android.app.NotificationManager
            manager.notify(WorkoutNotificationManager.NOTIFICATION_ID, notification)
        } catch (_: Exception) {
            // Notification update failed (e.g., after stopForeground)
        }
    }

    /**
     * Encoding format uses URL-safe delimiters.
     * Exercises separated by \n\n, fields by \n, sets by \t, set fields by comma.
     */
    private fun parseExercises(encoded: String): List<ServiceExercise> {
        return encoded.split("\n\n").mapNotNull { exerciseStr ->
            val parts = exerciseStr.split("\n", limit = 3)
            if (parts.size < 3) return@mapNotNull null
            val name = parts[0]
            val id = parts[1].toIntOrNull() ?: return@mapNotNull null
            val sets = parts[2].split("\t").mapNotNull { setStr ->
                val fields = setStr.split(",")
                if (fields.size < 4) return@mapNotNull null
                SetData(
                    setNumber = fields[0].toIntOrNull() ?: 1,
                    reps = fields[1],
                    weight = fields[2],
                    restSeconds = fields[3]
                )
            }
            if (sets.isEmpty()) return@mapNotNull null
            ServiceExercise(name = name, exerciseId = id, sets = sets)
        }
    }

    override fun onDestroy() {
        try { unregisterReceiver(screenOnReceiver) } catch (_: Exception) {}
        cancelRestTimer()
        WorkoutNotificationManager.releaseMediaSession()
        _state.value = WorkoutServiceState()
        super.onDestroy()
    }
}
