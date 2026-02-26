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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.akhara.ui.components.ExerciseCard
import com.akhara.ui.components.GlassCard
import com.akhara.ui.components.MuscleGroupChip
import com.akhara.ui.components.SetInputRow
import com.akhara.ui.theme.BackgroundDark
import com.akhara.ui.theme.Destructive
import com.akhara.ui.theme.PrimaryTeal
import com.akhara.ui.theme.SurfaceCard
import com.akhara.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun LogWorkoutScreen(
    viewModel: LogWorkoutViewModel,
    onBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

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
                    onClick = { viewModel.saveWorkout() },
                    enabled = state.selectedExercises.isNotEmpty() && !state.isSaving,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryTeal,
                        contentColor = BackgroundDark
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Rounded.Check, null, modifier = Modifier.padding(end = 4.dp))
                    Text(if (state.isEditMode) "Update" else "Save", fontWeight = FontWeight.Bold)
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
                                    color = PrimaryTeal
                                )
                            }
                            IconButton(onClick = { viewModel.removeExercise(exIdx) }) {
                                Icon(Icons.Rounded.Delete, "Remove", tint = Destructive.copy(alpha = 0.7f))
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            Text("SET", fontSize = 11.sp, color = TextSecondary, fontWeight = FontWeight.Medium)
                            Text("REPS", fontSize = 11.sp, color = TextSecondary, fontWeight = FontWeight.Medium)
                            Text("KG", fontSize = 11.sp, color = TextSecondary, fontWeight = FontWeight.Medium)
                            Text("REST", fontSize = 11.sp, color = TextSecondary, fontWeight = FontWeight.Medium)
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
                                modifier = Modifier.padding(vertical = 2.dp)
                            )
                        }

                        TextButton(
                            onClick = { viewModel.addSet(exIdx) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Rounded.Add, null, tint = PrimaryTeal)
                            Text("Add Set", color = PrimaryTeal, fontWeight = FontWeight.Medium)
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
