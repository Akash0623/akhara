package com.akhara.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.support.v4.media.MediaMetadataCompat
import com.akhara.ui.screens.workout.LockScreenWorkoutActivity
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.core.app.NotificationCompat
import androidx.media.app.NotificationCompat as MediaNotificationCompat
import com.akhara.R

object WorkoutNotificationManager {

    const val CHANNEL_ID = "akhara_workout"
    const val NOTIFICATION_ID = 2001

    private const val RC_DONE_SET = 1
    private const val RC_SKIP = 2
    private const val RC_FINISH = 3
    private const val RC_REP_MINUS = 4
    private const val RC_REP_PLUS = 5
    private const val RC_OPEN_CONTROLS = 100

    private var mediaSession: MediaSessionCompat? = null

    fun createChannel(context: Context) {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Workout Controller",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Active workout controls on lock screen"
            setSound(null, null)
            enableVibration(false)
            lockscreenVisibility = Notification.VISIBILITY_PUBLIC
        }
        val manager = context.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }

    fun getOrCreateMediaSession(context: Context): MediaSessionCompat {
        return mediaSession ?: MediaSessionCompat(context, "AkharaWorkout").also {
            @Suppress("DEPRECATION")
            it.setFlags(
                MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS or
                MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS
            )
            it.isActive = true
            mediaSession = it
        }
    }

    fun releaseMediaSession() {
        mediaSession?.isActive = false
        mediaSession?.release()
        mediaSession = null
    }

    fun updateMediaSession(session: MediaSessionCompat, state: WorkoutServiceState) {
        val title = if (state.isResting) {
            "Rest — ${state.restSecondsRemaining}s"
        } else {
            state.exerciseName
        }

        val subtitle = if (state.isResting) {
            "Next: Set ${state.currentSet}/${state.totalSets}"
        } else {
            buildString {
                append("Set ${state.currentSet}/${state.totalSets}")
                append(" · ${state.adjustedReps} reps")
                if (state.targetWeight.isNotBlank()) append(" · ${state.targetWeight}kg")
            }
        }

        session.setMetadata(
            MediaMetadataCompat.Builder()
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, title)
                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, subtitle)
                .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, "Akhara Workout")
                .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, -1L)
                .build()
        )

        val playbackState = PlaybackStateCompat.Builder()
            .setState(PlaybackStateCompat.STATE_PLAYING, 0L, 1f)
            .setActions(
                PlaybackStateCompat.ACTION_PLAY or
                PlaybackStateCompat.ACTION_PAUSE or
                PlaybackStateCompat.ACTION_PLAY_PAUSE or
                PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS or
                PlaybackStateCompat.ACTION_SKIP_TO_NEXT or
                PlaybackStateCompat.ACTION_STOP
            )
            .build()
        session.setPlaybackState(playbackState)
    }

    fun buildNotification(context: Context, state: WorkoutServiceState, session: MediaSessionCompat): Notification {
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

        // Tapping notification opens the lock screen workout controller
        val openControlsIntent = PendingIntent.getActivity(
            context,
            RC_OPEN_CONTROLS,
            Intent(context, LockScreenWorkoutActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(text)
            .setContentIntent(openControlsIntent)
            .setOngoing(true)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setSilent(true)
            .setCategory(NotificationCompat.CATEGORY_TRANSPORT)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setStyle(
                MediaNotificationCompat.MediaStyle()
                    .setMediaSession(session.sessionToken)
                    .setShowActionsInCompactView(0, 1, 2)
            )

        if (state.isResting) {
            builder.addAction(NotificationCompat.Action(R.drawable.ic_skip_rest, "Skip Rest", doneSetIntent))
            builder.addAction(NotificationCompat.Action(R.drawable.ic_skip_exercise, "Skip Exercise", skipIntent))
            builder.addAction(NotificationCompat.Action(R.drawable.ic_finish_workout, "Finish", finishIntent))
        } else {
            val repMinusIntent = buildActionIntent(context, WorkoutService.ACTION_REP_MINUS, RC_REP_MINUS)
            val repPlusIntent = buildActionIntent(context, WorkoutService.ACTION_REP_PLUS, RC_REP_PLUS)
            builder.addAction(NotificationCompat.Action(R.drawable.ic_rep_minus, "−1", repMinusIntent))
            builder.addAction(NotificationCompat.Action(R.drawable.ic_done_set, "Done (${state.adjustedReps})", doneSetIntent))
            builder.addAction(NotificationCompat.Action(R.drawable.ic_rep_plus, "+1", repPlusIntent))
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
