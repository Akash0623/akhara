package com.akhara.ui.screens.home

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.EditCalendar
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.MonitorWeight
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.foundation.clickable
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.akhara.ui.components.GlassCard
import com.akhara.ui.components.MuscleGroupChip
import com.akhara.ui.components.StatMiniCard
import com.akhara.ui.components.StreakRing
import com.akhara.ui.screens.insights.InsightCard
import com.akhara.ui.theme.BackgroundDark
import com.akhara.ui.theme.PrimaryTeal
import com.akhara.ui.theme.SurfaceCard
import com.akhara.ui.theme.SurfaceVariant
import com.akhara.ui.theme.TextSecondary
import com.akhara.ui.theme.WarningAmber

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onStartWorkout: () -> Unit,
    onOpenPlanner: () -> Unit,
    onOpenSecurity: () -> Unit = {}
) {
    val state by viewModel.uiState.collectAsState()
    var showGoalDialog by remember { mutableStateOf(false) }
    var selectedGoal by remember { mutableIntStateOf(state.weeklyGoal) }

    if (showGoalDialog) {
        AlertDialog(
            onDismissRequest = { showGoalDialog = false },
            containerColor = SurfaceCard,
            title = {
                Text("Weekly Workout Goal", fontWeight = FontWeight.Bold)
            },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "How many workouts per week?",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        (1..7).forEach { num ->
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(
                                        if (num == selectedGoal) PrimaryTeal else SurfaceVariant,
                                        RoundedCornerShape(10.dp)
                                    )
                                    .clickable { selectedGoal = num },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "$num",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = if (num == selectedGoal) BackgroundDark else TextSecondary
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.updateWeeklyGoal(selectedGoal)
                        showGoalDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryTeal,
                        contentColor = BackgroundDark
                    )
                ) {
                    Text("Save", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showGoalDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = state.greeting,
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = state.dateText,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }
                IconButton(onClick = onOpenSecurity) {
                    Icon(
                        Icons.Rounded.Shield,
                        contentDescription = "Security Settings",
                        tint = PrimaryTeal
                    )
                }
            }

            GlassCard(glowBorder = true) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                Icons.Rounded.LocalFireDepartment,
                                contentDescription = null,
                                tint = if (state.currentStreak > 0) WarningAmber else TextSecondary,
                                modifier = Modifier.padding(bottom = 2.dp)
                            )
                            Text(
                                text = "${state.currentStreak} week streak",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 16.sp
                            )
                        }
                        Text(
                            text = "Best: ${state.longestStreak} weeks",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }

                    Box(modifier = Modifier.clickable {
                        selectedGoal = state.weeklyGoal
                        showGoalDialog = true
                    }) {
                        StreakRing(
                            current = state.weeklyWorkoutCount,
                            goal = state.weeklyGoal,
                            size = 120.dp,
                            strokeWidth = 10.dp
                        )
                    }
                }
            }

            GlassCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Today's Plan",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    IconButton(onClick = onOpenPlanner) {
                        Icon(
                            Icons.Rounded.EditCalendar,
                            contentDescription = "Edit plan",
                            tint = PrimaryTeal
                        )
                    }
                }

                if (state.todayMuscleGroups.isEmpty()) {
                    Text(
                        text = "No muscles planned for today. Rest day or set up your plan.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                } else {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        state.todayMuscleGroups.forEach { muscle ->
                            MuscleGroupChip(
                                label = muscle,
                                selected = true,
                                onClick = {}
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = onStartWorkout,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryTeal,
                        contentColor = BackgroundDark
                    )
                ) {
                    Text(
                        text = "Start Workout",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatMiniCard(
                    label = "THIS WEEK",
                    value = "${state.weeklyWorkoutCount}",
                    modifier = Modifier.weight(1f)
                )
                StatMiniCard(
                    label = "VOLUME",
                    value = state.weeklyVolume,
                    modifier = Modifier.weight(1f)
                )
                StatMiniCard(
                    label = "AVG REST",
                    value = state.avgRestTime,
                    modifier = Modifier.weight(1f)
                )
            }

            GlassCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        Icons.Rounded.MonitorWeight,
                        contentDescription = null,
                        tint = PrimaryTeal,
                        modifier = Modifier.size(24.dp)
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Body Weight",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        )
                        if (state.latestBodyWeight.isNotEmpty()) {
                            Text(
                                text = "Current: ${state.latestBodyWeight}",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = state.bodyWeightInput,
                        onValueChange = viewModel::onBodyWeightInputChange,
                        placeholder = { Text("kg", color = TextSecondary) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.weight(1f).height(52.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryTeal,
                            unfocusedBorderColor = TextSecondary.copy(alpha = 0.3f),
                            cursorColor = PrimaryTeal,
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                        )
                    )
                    Button(
                        onClick = viewModel::logBodyWeight,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.height(52.dp).width(80.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PrimaryTeal,
                            contentColor = BackgroundDark
                        )
                    ) {
                        Text("Log", fontWeight = FontWeight.Bold)
                    }
                }
            }

            if (state.topInsights.isNotEmpty()) {
                Text(
                    text = "INSIGHTS",
                    style = MaterialTheme.typography.labelMedium,
                    color = TextSecondary
                )
                state.topInsights.forEach { insight ->
                    InsightCard(insight = insight)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
