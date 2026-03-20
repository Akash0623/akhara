package com.akhara.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat

object WorkoutNotificationManager {

    const val CHANNEL_ID = "akhara_workout"
    const val NOTIFICATION_ID = 2001

    private const val RC_DONE_SET = 1
    private const val RC_SKIP = 2
    private const val RC_FINISH = 3
    private const val RC_REP_MINUS = 4
    private const val RC_REP_PLUS = 5

    fun createChannel(context: Context) {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Workout Controller",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Active workout controls on lock screen"
            setSound(null, null)
            enableVibration(false)
            lockscreenVisibility = Notification.VISIBILITY_PUBLIC
        }
        val manager = context.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }

    fun buildNotification(context: Context, state: WorkoutServiceState): Notification {
        val title = if (state.isResting) {
            "Rest — ${state.restSecondsRemaining}s"
        } else {
            "${state.exerciseName} (${state.exerciseIndex + 1}/${state.totalExercises})"
        }

        val text = if (state.isResting) {
            "Next: Set ${state.currentSet}/${state.totalSets}"
        } else {
            buildString {
                append("Set ${state.currentSet}/${state.totalSets}")
                append(" · ${state.adjustedReps} reps")
                if (state.targetWeight.isNotBlank()) append(" · ${state.targetWeight}kg")
            }
        }

        val doneSetIntent = buildActionIntent(context, WorkoutService.ACTION_DONE_SET, RC_DONE_SET)
        val skipIntent = buildActionIntent(context, WorkoutService.ACTION_SKIP, RC_SKIP)
        val finishIntent = buildActionIntent(context, WorkoutService.ACTION_FINISH, RC_FINISH)

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(com.akhara.R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(text)
            .setOngoing(true)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setCategory(Notification.CATEGORY_WORKOUT)
            .setSilent(true)

        if (state.isResting) {
            builder.addAction(0, "Skip Rest", doneSetIntent)
            builder.addAction(0, "Skip Exercise", skipIntent)
            builder.addAction(0, "Finish", finishIntent)
        } else {
            val repMinusIntent = buildActionIntent(context, WorkoutService.ACTION_REP_MINUS, RC_REP_MINUS)
            val repPlusIntent = buildActionIntent(context, WorkoutService.ACTION_REP_PLUS, RC_REP_PLUS)
            builder.addAction(0, "−1", repMinusIntent)
            builder.addAction(0, "Done (${state.adjustedReps})", doneSetIntent)
            builder.addAction(0, "+1", repPlusIntent)
        }

        return builder.build()
    }

    private fun buildActionIntent(context: Context, action: String, requestCode: Int): PendingIntent {
        val intent = Intent(context, WorkoutService::class.java).apply {
            this.action = action
        }
        return PendingIntent.getService(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}
