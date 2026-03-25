package com.akhara.ui.screens.calendar

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.FitnessCenter
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.akhara.data.db.entity.Exercise
import com.akhara.data.db.entity.WorkoutSet
import com.akhara.data.repository.WorkoutRepository
import com.akhara.ui.components.GlassCard
import com.akhara.ui.theme.BackgroundDark
import com.akhara.ui.theme.PrimaryTeal
import com.akhara.ui.theme.SurfaceCard
import com.akhara.ui.theme.SurfaceVariant
import com.akhara.ui.theme.TextPrimary
import com.akhara.ui.theme.TextSecondary
import com.akhara.ui.theme.TextTertiary
import com.akhara.ui.theme.SuccessGreen
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class ExerciseWithSets(
    val exercise: Exercise,
    val sets: List<WorkoutSet>
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViewWorkoutScreen(
    sessionId: Int,
    repository: WorkoutRepository,
    onBack: () -> Unit
) {
    var exercisesWithSets by remember { mutableStateOf<List<ExerciseWithSets>>(emptyList()) }
    var sessionDate by remember { mutableStateOf("") }
    var totalVolume by remember { mutableStateOf(0f) }
    var totalSets by remember { mutableStateOf(0) }

    LaunchedEffect(sessionId) {
        val session = repository.getSessionById(sessionId)
        if (session != null) {
            val dateFormat = SimpleDateFormat("EEEE, MMM d", Locale.getDefault())
            sessionDate = dateFormat.format(Date(session.date))
        }

        val sets = repository.getSetsForSessionSync(sessionId)
        totalSets = sets.size
        totalVolume = sets.sumOf { (it.weight * it.reps).toDouble() }.toFloat()

        val grouped = sets.groupBy { it.exerciseId }
        val result = mutableListOf<ExerciseWithSets>()
        for ((exerciseId, exerciseSets) in grouped) {
            val exercise = repository.getExerciseByIdSync(exerciseId)
            if (exercise != null) {
                result.add(ExerciseWithSets(exercise, exerciseSets.sortedBy { it.setNumber }))
            }
        }
        exercisesWithSets = result
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Workout Summary",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        if (sessionDate.isNotBlank()) {
                            Text(
                                text = sessionDate,
                                fontSize = 13.sp,
                                color = TextSecondary
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BackgroundDark,
                    titleContentColor = TextPrimary,
                    navigationIconContentColor = TextPrimary
                )
            )

            // Stats summary row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatChip(
                    label = "Exercises",
                    value = "${exercisesWithSets.size}",
                    modifier = Modifier.weight(1f)
                )
                StatChip(
                    label = "Sets",
                    value = "$totalSets",
                    modifier = Modifier.weight(1f)
                )
                StatChip(
                    label = "Volume",
                    value = formatVolume(totalVolume),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(exercisesWithSets) { item ->
                    ExerciseViewCard(item)
                }
                item { Spacer(modifier = Modifier.height(24.dp)) }
            }
        }
    }
}

@Composable
private fun ExerciseViewCard(item: ExerciseWithSets) {
    GlassCard {
        Column {
            // Exercise header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                Icon(
                    Icons.Rounded.FitnessCenter,
                    contentDescription = null,
                    tint = PrimaryTeal,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = item.exercise.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = TextPrimary
                    )
                    Text(
                        text = item.exercise.muscleGroup,
                        fontSize = 12.sp,
                        color = PrimaryTeal
                    )
                }
            }

            // Column headers
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("SET", fontSize = 12.sp, color = TextTertiary, fontWeight = FontWeight.Medium,
                    modifier = Modifier.width(40.dp))
                Text("KG", fontSize = 12.sp, color = TextTertiary, fontWeight = FontWeight.Medium,
                    modifier = Modifier.weight(1f))
                Text("REPS", fontSize = 12.sp, color = TextTertiary, fontWeight = FontWeight.Medium,
                    modifier = Modifier.weight(1f))
                if (item.sets.any { it.restSeconds > 0 }) {
                    Text("REST", fontSize = 12.sp, color = TextTertiary, fontWeight = FontWeight.Medium,
                        modifier = Modifier.weight(1f))
                }
            }

            // Set rows
            item.sets.forEach { set ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SurfaceVariant.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${set.setNumber}",
                        fontSize = 14.sp,
                        color = PrimaryTeal,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.width(40.dp)
                    )
                    Text(
                        text = formatWeight(set.weight),
                        fontSize = 16.sp,
                        color = TextPrimary,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "${set.reps}",
                        fontSize = 16.sp,
                        color = TextPrimary,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.weight(1f)
                    )
                    if (item.sets.any { it.restSeconds > 0 }) {
                        Text(
                            text = if (set.restSeconds > 0) "${set.restSeconds}s" else "—",
                            fontSize = 14.sp,
                            color = TextSecondary,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
            }

            // Exercise totals
            val totalVol = item.sets.sumOf { (it.weight * it.reps).toDouble() }.toFloat()
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${item.sets.size} sets",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
                Text(
                    text = "Volume: ${formatVolume(totalVol)}",
                    fontSize = 12.sp,
                    color = SuccessGreen
                )
            }
        }
    }
}

@Composable
private fun StatChip(label: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .background(SurfaceCard, RoundedCornerShape(12.dp))
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = PrimaryTeal
        )
        Text(
            text = label,
            fontSize = 11.sp,
            color = TextSecondary,
            fontWeight = FontWeight.Medium
        )
    }
}

private fun formatWeight(weight: Float): String {
    return if (weight == weight.toInt().toFloat()) "${weight.toInt()}" else "$weight"
}

private fun formatVolume(volume: Float): String {
    return when {
        volume >= 1000 -> "${String.format("%.1f", volume / 1000)}t"
        else -> "${volume.toInt()}kg"
    }
}
