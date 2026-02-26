package com.akhara.ui.screens.exercises

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
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
import androidx.compose.ui.unit.dp
import com.akhara.ui.components.ExerciseCard
import com.akhara.ui.components.MuscleGroupChip
import com.akhara.ui.theme.BackgroundDark
import com.akhara.ui.theme.PrimaryTeal
import com.akhara.ui.theme.SurfaceCard
import com.akhara.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseLibraryScreen(viewModel: ExerciseLibraryViewModel) {
    val state by viewModel.uiState.collectAsState()
    val sheetState = rememberModalBottomSheetState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Exercise Library",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = state.searchQuery,
                onValueChange = { viewModel.search(it) },
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

            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MuscleGroupChip(
                    label = "All",
                    selected = state.selectedMuscleGroup == null,
                    onClick = { viewModel.filterByMuscleGroup(null) }
                )
                state.muscleGroups.forEach { group ->
                    MuscleGroupChip(
                        label = group,
                        selected = state.selectedMuscleGroup == group,
                        onClick = { viewModel.filterByMuscleGroup(group) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            val grouped = state.exercises.groupBy { it.muscleGroup }

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                grouped.forEach { (group, exercises) ->
                    item {
                        Text(
                            text = group.uppercase(),
                            style = MaterialTheme.typography.labelMedium,
                            color = PrimaryTeal,
                            modifier = Modifier.padding(top = 12.dp, bottom = 4.dp)
                        )
                    }
                    items(exercises.size) { idx ->
                        ExerciseCard(exercise = exercises[idx])
                    }
                }
                item { Spacer(modifier = Modifier.height(80.dp)) }
            }
        }

        FloatingActionButton(
            onClick = { viewModel.showAddDialog(true) },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
                .padding(bottom = 72.dp),
            containerColor = PrimaryTeal,
            contentColor = BackgroundDark,
            shape = RoundedCornerShape(16.dp)
        ) {
            Icon(Icons.Rounded.Add, "Add exercise")
        }

        if (state.showAddDialog) {
            ModalBottomSheet(
                onDismissRequest = { viewModel.showAddDialog(false) },
                sheetState = sheetState,
                containerColor = BackgroundDark
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 20.dp).padding(bottom = 32.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Add Custom Exercise",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )

                    OutlinedTextField(
                        value = state.newExerciseName,
                        onValueChange = { viewModel.updateNewExerciseName(it) },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Exercise name") },
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryTeal,
                            unfocusedBorderColor = SurfaceCard,
                            cursorColor = PrimaryTeal
                        ),
                        singleLine = true
                    )

                    Text(
                        text = "Muscle Group",
                        style = MaterialTheme.typography.labelMedium,
                        color = TextSecondary
                    )

                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        state.muscleGroups.forEach { group ->
                            MuscleGroupChip(
                                label = group,
                                selected = state.newExerciseMuscleGroup == group,
                                onClick = { viewModel.updateNewExerciseMuscleGroup(group) }
                            )
                        }
                    }

                    TextButton(
                        onClick = { viewModel.addCustomExercise() },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text("Add", color = PrimaryTeal, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
