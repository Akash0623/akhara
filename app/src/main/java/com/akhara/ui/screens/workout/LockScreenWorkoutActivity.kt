package com.akhara.ui.screens.workout

import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.akhara.service.WorkoutService
import com.akhara.ui.theme.AkharaTheme
import com.akhara.ui.theme.BackgroundDark
import com.akhara.ui.theme.PrimaryTeal
import com.akhara.ui.theme.SurfaceCard
import com.akhara.ui.theme.TextPrimary
import com.akhara.ui.theme.TextSecondary
import com.akhara.ui.theme.TextTertiary
import com.akhara.ui.theme.Destructive

class LockScreenWorkoutActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setShowWhenLocked(true)
        setTurnScreenOn(true)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        enableEdgeToEdge()

        setContent {
            AkharaTheme {
                val state by WorkoutService.state.collectAsState()

                if (!state.isActive) {
                    finish()
                    return@AkharaTheme
                }

                LockScreenWorkoutUI(
                    exerciseName = state.exerciseName,
                    exerciseProgress = "${state.exerciseIndex + 1}/${state.totalExercises}",
                    currentSet = state.currentSet,
                    totalSets = state.totalSets,
                    reps = state.adjustedReps,
                    weight = state.targetWeight,
                    isResting = state.isResting,
                    restSeconds = state.restSecondsRemaining,
                    onRepMinus = { sendServiceAction(WorkoutService.ACTION_REP_MINUS) },
                    onRepPlus = { sendServiceAction(WorkoutService.ACTION_REP_PLUS) },
                    onDoneSet = { sendServiceAction(WorkoutService.ACTION_DONE_SET) },
                    onSkipExercise = { sendServiceAction(WorkoutService.ACTION_SKIP) },
                    onFinish = {
                        sendServiceAction(WorkoutService.ACTION_FINISH)
                        finish()
                    },
                    onMinimize = { finish() }
                )
            }
        }
    }

    private fun sendServiceAction(action: String) {
        val intent = Intent(this, WorkoutService::class.java).apply {
            this.action = action
        }
        try {
            startService(intent)
        } catch (_: IllegalStateException) {
            // Background restriction on some OEMs — try foreground variant
            try {
                startForegroundService(intent)
            } catch (_: Exception) {
                // Service not available — action will be lost
            }
        }
    }
}

@Composable
private fun LockScreenWorkoutUI(
    exerciseName: String,
    exerciseProgress: String,
    currentSet: Int,
    totalSets: Int,
    reps: Int,
    weight: String,
    isResting: Boolean,
    restSeconds: Int,
    onRepMinus: () -> Unit,
    onRepPlus: () -> Unit,
    onDoneSet: () -> Unit,
    onSkipExercise: () -> Unit,
    onFinish: () -> Unit,
    onMinimize: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Exercise name
            Text(
                text = exerciseName,
                color = TextPrimary,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                maxLines = 2
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Exercise progress
            Text(
                text = "Exercise $exerciseProgress",
                color = TextSecondary,
                fontSize = 14.sp
            )

            Spacer(modifier = Modifier.height(32.dp))

            if (isResting) {
                // Rest timer UI
                RestingUI(
                    restSeconds = restSeconds,
                    currentSet = currentSet,
                    totalSets = totalSets,
                    onSkipRest = onDoneSet,
                    onSkipExercise = onSkipExercise,
                    onFinish = onFinish
                )
            } else {
                // Active set UI
                ActiveSetUI(
                    currentSet = currentSet,
                    totalSets = totalSets,
                    reps = reps,
                    weight = weight,
                    onRepMinus = onRepMinus,
                    onRepPlus = onRepPlus,
                    onDoneSet = onDoneSet,
                    onSkipExercise = onSkipExercise,
                    onFinish = onFinish
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Back to lock screen
            TextButton(onClick = onMinimize) {
                Text(
                    text = "Back to Lock Screen",
                    color = TextSecondary,
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
private fun ActiveSetUI(
    currentSet: Int,
    totalSets: Int,
    reps: Int,
    weight: String,
    onRepMinus: () -> Unit,
    onRepPlus: () -> Unit,
    onDoneSet: () -> Unit,
    onSkipExercise: () -> Unit,
    onFinish: () -> Unit
) {
    val haptic = LocalHapticFeedback.current

    // Set indicator
    Text(
        text = "Set $currentSet of $totalSets",
        color = PrimaryTeal,
        fontSize = 18.sp,
        fontWeight = FontWeight.SemiBold
    )

    if (weight.isNotBlank()) {
        Text(
            text = "${weight}kg",
            color = TextSecondary,
            fontSize = 16.sp
        )
    }

    Spacer(modifier = Modifier.height(24.dp))

    // Rep counter with +/- buttons
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        // Minus button
        FilledIconButton(
            onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onRepMinus()
            },
            enabled = reps > 0,
            modifier = Modifier.size(64.dp),
            shape = CircleShape,
            colors = IconButtonDefaults.filledIconButtonColors(
                containerColor = SurfaceCard,
                disabledContainerColor = SurfaceCard.copy(alpha = 0.3f)
            )
        ) {
            Icon(
                Icons.Default.Remove,
                contentDescription = "Decrease reps",
                tint = if (reps > 0) TextPrimary else TextTertiary,
                modifier = Modifier.size(32.dp)
            )
        }

        Spacer(modifier = Modifier.width(24.dp))

        // Rep count
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "$reps",
                color = TextPrimary,
                fontSize = 64.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "reps",
                color = TextSecondary,
                fontSize = 14.sp
            )
        }

        Spacer(modifier = Modifier.width(24.dp))

        // Plus button
        FilledIconButton(
            onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onRepPlus()
            },
            modifier = Modifier.size(64.dp),
            shape = CircleShape,
            colors = IconButtonDefaults.filledIconButtonColors(
                containerColor = SurfaceCard
            )
        ) {
            Icon(
                Icons.Default.Add,
                contentDescription = "Increase reps",
                tint = TextPrimary,
                modifier = Modifier.size(32.dp)
            )
        }
    }

    Spacer(modifier = Modifier.height(32.dp))

    // Done Set button (primary action)
    Button(
        onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            onDoneSet()
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(containerColor = PrimaryTeal)
    ) {
        Icon(
            Icons.Default.Check,
            contentDescription = null,
            tint = Color.Black,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "Done Set",
            color = Color.Black,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
    }

    Spacer(modifier = Modifier.height(12.dp))

    // Secondary actions row
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OutlinedButton(
            onClick = onSkipExercise,
            modifier = Modifier
                .weight(1f)
                .height(48.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(
                Icons.Default.SkipNext,
                contentDescription = null,
                tint = TextPrimary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text("Skip", color = TextPrimary, fontSize = 14.sp)
        }

        OutlinedButton(
            onClick = onFinish,
            modifier = Modifier
                .weight(1f)
                .height(48.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(
                Icons.Default.Close,
                contentDescription = null,
                tint = Destructive,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text("Finish", color = Destructive, fontSize = 14.sp)
        }
    }
}

@Composable
private fun RestingUI(
    restSeconds: Int,
    currentSet: Int,
    totalSets: Int,
    onSkipRest: () -> Unit,
    onSkipExercise: () -> Unit,
    onFinish: () -> Unit
) {
    // Big rest timer
    Text(
        text = "${restSeconds}s",
        color = PrimaryTeal,
        fontSize = 72.sp,
        fontWeight = FontWeight.Bold
    )

    Text(
        text = "Rest",
        color = TextSecondary,
        fontSize = 18.sp
    )

    Spacer(modifier = Modifier.height(8.dp))

    Text(
        text = "Next: Set $currentSet/$totalSets",
        color = TextSecondary,
        fontSize = 14.sp
    )

    Spacer(modifier = Modifier.height(32.dp))

    // Skip Rest button (primary)
    Button(
        onClick = onSkipRest,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(containerColor = PrimaryTeal)
    ) {
        Text(
            text = "Skip Rest",
            color = Color.Black,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
    }

    Spacer(modifier = Modifier.height(12.dp))

    // Secondary actions
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OutlinedButton(
            onClick = onSkipExercise,
            modifier = Modifier
                .weight(1f)
                .height(48.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Skip Exercise", color = TextPrimary, fontSize = 14.sp)
        }

        OutlinedButton(
            onClick = onFinish,
            modifier = Modifier
                .weight(1f)
                .height(48.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Finish", color = Destructive, fontSize = 14.sp)
        }
    }
}
