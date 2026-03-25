package com.akhara.ui.screens.workout

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import android.content.Intent
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.akhara.service.WorkoutService
import com.akhara.ui.screens.workout.LockScreenWorkoutActivity
import com.akhara.ui.components.ExerciseCard
import com.akhara.ui.components.GlassCard
import com.akhara.ui.components.MuscleGroupChip
import com.akhara.ui.components.SetInputRow
import com.akhara.ui.theme.BackgroundDark
import com.akhara.ui.theme.Destructive
import com.akhara.ui.theme.PrimaryTeal
import com.akhara.ui.theme.SuccessGreen
import com.akhara.ui.theme.SurfaceCard
import com.akhara.ui.theme.SurfaceVariant
import com.akhara.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun LogWorkoutScreen(
    viewModel: LogWorkoutViewModel,
    onBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val serviceState by WorkoutService.state.collectAsState()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val haptic = LocalHapticFeedback.current
    val context = LocalContext.current

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            if (!viewModel.checkNotificationChannel()) {
                viewModel.showNotificationDialog(NotificationDialogType.LockScreenGuidance)
            }
            viewModel.startWorkoutService()
        } else {
            viewModel.showNotificationDialog(NotificationDialogType.PermissionRationale)
        }
    }

    LaunchedEffect(state.isSaved) {
        if (state.isSaved) onBack()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Rounded.ArrowBack, "Back")
                }
                Text(
                    text = if (state.isEditMode) "Edit Workout" else "Log Workout",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                Button(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        viewModel.saveWorkout()
                    },
                    enabled = state.selectedExercises.isNotEmpty() && !state.isSaving,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryTeal,
                        contentColor = BackgroundDark
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Rounded.Check, null, modifier = Modifier.padding(end = 4.dp))
                    Text(
                        when {
                            state.isEditMode -> "Update"
                            state.hasAnySaved -> "Finish"
                            else -> "Save"
                        },
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Progress indicator
            if (state.selectedExercises.isNotEmpty()) {
                val doneCount = state.selectedExercises.count { it.isDone }
                val totalCount = state.selectedExercises.size
                val progress = if (totalCount > 0) doneCount.toFloat() / totalCount else 0f

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .weight(1f)
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp)),
                        color = if (doneCount == totalCount) SuccessGreen else PrimaryTeal,
                        trackColor = SurfaceVariant,
                    )
                    Text(
                        text = "$doneCount/$totalCount",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (doneCount == totalCount) SuccessGreen else TextSecondary
                    )
                }
            }

            if (state.todayMuscleGroups.isNotEmpty()) {
                FlowRow(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    state.todayMuscleGroups.forEach { muscle ->
                        MuscleGroupChip(label = muscle, selected = true, onClick = {})
                    }
                }
            }

            // Lock screen controller banner
            if (state.selectedExercises.isNotEmpty() && !state.isEditMode) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 6.dp)
                        .background(SurfaceVariant.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    if (serviceState.isActive) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (serviceState.isResting)
                                    "Rest — ${serviceState.restSecondsRemaining}s"
                                else
                                    "${serviceState.exerciseName} · Set ${serviceState.currentSet}/${serviceState.totalSets}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "Lock screen controller active",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }
                        TextButton(onClick = { viewModel.stopWorkoutService() }) {
                            Text("Stop", color = Destructive, fontWeight = FontWeight.Medium)
                        }
                    } else {
                        Text(
                            text = "Control from lock screen",
                            fontSize = 13.sp,
                            color = TextSecondary,
                            modifier = Modifier.weight(1f)
                        )
                        Button(
                            onClick = {
                                if (viewModel.isNotificationPermissionNeeded()) {
                                    try {
                                        notificationPermissionLauncher.launch(
                                            android.Manifest.permission.POST_NOTIFICATIONS
                                        )
                                    } catch (_: Exception) {
                                        // requestCode overflow on some devices — open settings as fallback
                                        val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                                            putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                                        }
                                        context.startActivity(intent)
                                    }
                                } else {
                                    if (!viewModel.checkNotificationChannel()) {
                                        viewModel.showNotificationDialog(NotificationDialogType.LockScreenGuidance)
                                    }
                                    viewModel.startWorkoutService()
                                    // Auto-launch the lock screen workout controller (like Google Maps navigation)
                                    context.startActivity(
                                        Intent(context, LockScreenWorkoutActivity::class.java).apply {
                                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                        }
                                    )
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = PrimaryTeal,
                                contentColor = BackgroundDark
                            ),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Start", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }
            }

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item { Spacer(modifier = Modifier.height(8.dp)) }

                itemsIndexed(state.selectedExercises) { exIdx, entry ->
                    GlassCard {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = entry.exercise.name,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 16.sp
                                )
                                Text(
                                    text = entry.exercise.muscleGroup,
                                    fontSize = 12.sp,
                                    color = if (entry.isDone) SuccessGreen else PrimaryTeal
                                )
                            }
                            if (!entry.isDone) {
                                IconButton(onClick = { viewModel.removeExercise(exIdx) }) {
                                    Icon(Icons.Rounded.Delete, "Remove", tint = Destructive.copy(alpha = 0.7f))
                                }
                            }
                        }

                        if (entry.isDone) {
                            // Collapsed summary
                            val totalSets = entry.sets.size
                            val totalReps = entry.sets.sumOf { it.reps.toIntOrNull() ?: 0 }
                            val totalVol = entry.sets.sumOf {
                                (it.reps.toIntOrNull() ?: 0) * (it.weight.toFloatOrNull() ?: 0f).toDouble()
                            }
                            val volText = if (totalVol > 0) " · ${totalVol.toInt()} kg" else ""
                            Text(
                                text = "$totalSets sets · $totalReps reps$volText",
                                fontSize = 13.sp,
                                color = TextSecondary,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            TextButton(
                                onClick = { viewModel.toggleExerciseDone(exIdx) },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Edit", color = PrimaryTeal, fontWeight = FontWeight.Medium)
                            }
                        } else {
                            // Expanded editing view
                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                Text("SET", fontSize = 14.sp, color = TextSecondary, fontWeight = FontWeight.Medium)
                                Text("REPS", fontSize = 14.sp, color = TextSecondary, fontWeight = FontWeight.Medium)
                                Text("KG", fontSize = 14.sp, color = TextSecondary, fontWeight = FontWeight.Medium)
                                if (entry.sets.size > 1) {
                                    Text("REST", fontSize = 14.sp, color = TextSecondary, fontWeight = FontWeight.Medium)
                                } else {
                                    Text("", fontSize = 11.sp)
                                }
                                Text("", fontSize = 11.sp)
                            }

                            entry.sets.forEachIndexed { setIdx, setData ->
                                SetInputRow(
                                    setData = setData,
                                    onRepsChange = {
                                        viewModel.updateSet(exIdx, setIdx, setData.copy(reps = it))
                                    },
                                    onWeightChange = {
                                        viewModel.updateSet(exIdx, setIdx, setData.copy(weight = it))
                                    },
                                    onRestChange = {
                                        viewModel.updateSet(exIdx, setIdx, setData.copy(restSeconds = it))
                                    },
                                    onDelete = { viewModel.removeSet(exIdx, setIdx) },
                                    isLastSet = setIdx == entry.sets.lastIndex,
                                    modifier = Modifier.padding(vertical = 2.dp)
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                TextButton(
                                    onClick = { viewModel.addSet(exIdx) }
                                ) {
                                    Icon(Icons.Rounded.Add, null, tint = PrimaryTeal)
                                    Text("Add Set", color = PrimaryTeal, fontWeight = FontWeight.Medium)
                                }
                                Button(
                                    onClick = {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        viewModel.toggleExerciseDone(exIdx)
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = PrimaryTeal,
                                        contentColor = BackgroundDark
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(Icons.Rounded.Check, null, modifier = Modifier.padding(end = 4.dp))
                                    Text("Done", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                item {
                    Button(
                        onClick = { viewModel.toggleExercisePicker(true) },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SurfaceCard,
                            contentColor = PrimaryTeal
                        )
                    ) {
                        Icon(Icons.Rounded.Add, null, modifier = Modifier.padding(end = 8.dp))
                        Text("Add Exercise", fontWeight = FontWeight.SemiBold)
                    }
                }

                item { Spacer(modifier = Modifier.height(24.dp)) }
            }
        }

        state.notificationDialogType?.let { dialogType ->
            AlertDialog(
                onDismissRequest = { viewModel.dismissNotificationDialog() },
                title = {
                    Text(
                        when (dialogType) {
                            NotificationDialogType.PermissionRationale ->
                                "Notifications Required"
                            NotificationDialogType.LockScreenGuidance ->
                                "Enable Lock Screen Notifications"
                        }
                    )
                },
                text = {
                    Text(
                        when (dialogType) {
                            NotificationDialogType.PermissionRationale ->
                                "Akhara needs notification permission to show workout controls on your lock screen. Please enable it in Settings."
                            NotificationDialogType.LockScreenGuidance ->
                                "To control your workout from the lock screen, enable lock screen notifications for Akhara in your device settings."
                        }
                    )
                },
                confirmButton = {
                    TextButton(onClick = {
                        val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                            putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                        }
                        context.startActivity(intent)
                        viewModel.dismissNotificationDialog()
                    }) {
                        Text("Open Settings", color = PrimaryTeal, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        viewModel.dismissNotificationDialog()
                        if (dialogType is NotificationDialogType.PermissionRationale) {
                            viewModel.startWorkoutService()
                        }
                    }) {
                        Text(
                            if (dialogType is NotificationDialogType.PermissionRationale) "Start Anyway" else "Later",
                            color = TextSecondary
                        )
                    }
                },
                containerColor = SurfaceCard
            )
        }

        if (state.showExercisePicker) {
            ModalBottomSheet(
                onDismissRequest = { viewModel.toggleExercisePicker(false) },
                sheetState = sheetState,
                containerColor = BackgroundDark
            ) {
                Column(modifier = Modifier.padding(horizontal = 20.dp).padding(bottom = 32.dp)) {
                    Text(
                        text = "Select Exercise",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    OutlinedTextField(
                        value = state.searchQuery,
                        onValueChange = { viewModel.searchExercises(it) },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Search exercises...") },
                        leadingIcon = { Icon(Icons.Rounded.Search, null) },
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryTeal,
                            unfocusedBorderColor = SurfaceCard,
                            focusedContainerColor = SurfaceCard,
                            unfocusedContainerColor = SurfaceCard,
                            cursorColor = PrimaryTeal
                        ),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        MuscleGroupChip(
                            label = "All",
                            selected = state.pickerMuscleFilter == null,
                            onClick = { viewModel.filterPickerByMuscleGroup(null) }
                        )
                        state.allMuscleGroups.forEach { group ->
                            MuscleGroupChip(
                                label = group,
                                selected = state.pickerMuscleFilter == group,
                                onClick = {
                                    viewModel.filterPickerByMuscleGroup(
                                        if (state.pickerMuscleFilter == group) null else group
                                    )
                                }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    LazyColumn(
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val grouped = state.availableExercises.groupBy { it.muscleGroup }
                        grouped.forEach { (group, exercises) ->
                            item {
                                Text(
                                    text = group.uppercase(),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = PrimaryTeal,
                                    modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                                )
                            }
                            items(exercises.size) { idx ->
                                ExerciseCard(
                                    exercise = exercises[idx],
                                    onAdd = { viewModel.addExercise(exercises[idx]) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
