package com.akhara.ui.screens.planner

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.ExpandLess
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.akhara.ui.components.ExerciseCard
import com.akhara.ui.components.GlassCard
import com.akhara.ui.components.MuscleGroupChip
import com.akhara.ui.theme.BackgroundDark
import com.akhara.ui.theme.Destructive
import com.akhara.ui.theme.PrimaryTeal
import com.akhara.ui.theme.SuccessGreen
import com.akhara.ui.theme.SurfaceCard
import com.akhara.ui.theme.TextSecondary

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun WeeklyPlannerScreen(
    viewModel: WeeklyPlannerViewModel,
    onBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Rounded.ArrowBack, "Back")
                }
                Text(
                    text = "Weekly Plan",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
            }

            Text(
                text = "Assign muscle groups and plan exercises for each day",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            state.days.forEach { dayPlan ->
                GlassCard(
                    modifier = Modifier.padding(vertical = 6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = dayPlan.dayName,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (dayPlan.plannedExercises.isNotEmpty()) {
                                Text(
                                    text = "${dayPlan.plannedExercises.size} exercises",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = PrimaryTeal
                                )
                            }
                            IconButton(onClick = { viewModel.toggleDayExpanded(dayPlan.dayOfWeek) }) {
                                Icon(
                                    if (dayPlan.isExpanded) Icons.Rounded.ExpandLess
                                    else Icons.Rounded.ExpandMore,
                                    contentDescription = "Toggle",
                                    tint = TextSecondary
                                )
                            }
                        }
                    }

                    AnimatedVisibility(visible = dayPlan.isExpanded) {
                        Column {
                            Spacer(modifier = Modifier.height(8.dp))

                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                state.allMuscleGroups.forEach { group ->
                                    MuscleGroupChip(
                                        label = group,
                                        selected = group in dayPlan.muscleGroups,
                                        onClick = {
                                            viewModel.toggleMuscleGroup(dayPlan.dayOfWeek, group)
                                        }
                                    )
                                }
                            }

                            if (dayPlan.plannedExercises.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "EXERCISES",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextSecondary
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                            }

                            dayPlan.plannedExercises.forEachIndexed { idx, entry ->
                                PlannedExerciseRow(
                                    entry = entry,
                                    onSetsChange = { viewModel.updateExerciseTarget(dayPlan.dayOfWeek, idx, sets = it, reps = null, weight = null) },
                                    onRepsChange = { viewModel.updateExerciseTarget(dayPlan.dayOfWeek, idx, sets = null, reps = it, weight = null) },
                                    onWeightChange = { viewModel.updateExerciseTarget(dayPlan.dayOfWeek, idx, sets = null, reps = null, weight = it) },
                                    onDelete = { viewModel.removeExerciseFromDay(dayPlan.dayOfWeek, idx) }
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            TextButton(
                                onClick = { viewModel.openExercisePicker(dayPlan.dayOfWeek) },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Rounded.Add, null, tint = PrimaryTeal)
                                Text("Add Exercise", color = PrimaryTeal, fontWeight = FontWeight.Medium)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    viewModel.savePlan()
                    onBack()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryTeal,
                    contentColor = BackgroundDark
                )
            ) {
                Icon(Icons.Rounded.Check, null, modifier = Modifier.padding(end = 8.dp))
                Text("Save Plan", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }

            if (state.isSaved) {
                Text(
                    text = "Plan saved!",
                    color = SuccessGreen,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(top = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        if (state.showExercisePicker) {
            ModalBottomSheet(
                onDismissRequest = { viewModel.closeExercisePicker() },
                sheetState = sheetState,
                containerColor = BackgroundDark
            ) {
                Column(modifier = Modifier.padding(horizontal = 20.dp).padding(bottom = 32.dp)) {
                    Text(
                        text = "Add Exercise",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    OutlinedTextField(
                        value = state.exerciseSearchQuery,
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

                    Spacer(modifier = Modifier.height(12.dp))

                    LazyColumn(
                        modifier = Modifier.height(400.dp),
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
                                    onAdd = { viewModel.addExerciseToDay(exercises[idx]) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PlannedExerciseRow(
    entry: PlannedExerciseEntry,
    onSetsChange: (String) -> Unit,
    onRepsChange: (String) -> Unit,
    onWeightChange: (String) -> Unit,
    onDelete: () -> Unit
) {
    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = PrimaryTeal,
        unfocusedBorderColor = SurfaceCard,
        focusedContainerColor = SurfaceCard,
        unfocusedContainerColor = SurfaceCard,
        cursorColor = PrimaryTeal
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = entry.exercise.name,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp
            )
            Text(
                text = entry.exercise.muscleGroup,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                OutlinedTextField(
                    value = entry.targetSets,
                    onValueChange = onSetsChange,
                    modifier = Modifier.width(52.dp),
                    label = { Text("Sets", fontSize = 9.sp) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodySmall,
                    colors = fieldColors,
                    shape = RoundedCornerShape(8.dp)
                )
                Text("x", color = TextSecondary, fontSize = 12.sp)
                OutlinedTextField(
                    value = entry.targetReps,
                    onValueChange = onRepsChange,
                    modifier = Modifier.width(52.dp),
                    label = { Text("Reps", fontSize = 9.sp) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodySmall,
                    colors = fieldColors,
                    shape = RoundedCornerShape(8.dp)
                )
                Text("@", color = TextSecondary, fontSize = 12.sp)
                OutlinedTextField(
                    value = entry.targetWeight,
                    onValueChange = onWeightChange,
                    modifier = Modifier.width(64.dp),
                    label = { Text("kg", fontSize = 9.sp) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodySmall,
                    colors = fieldColors,
                    shape = RoundedCornerShape(8.dp)
                )
            }
        }
        IconButton(onClick = onDelete) {
            Icon(Icons.Rounded.Delete, "Remove", tint = Destructive)
        }
    }
}
