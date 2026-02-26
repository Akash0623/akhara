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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.Alignment
import com.akhara.ui.components.CalendarGrid
import com.akhara.ui.components.GlassCard
import com.akhara.ui.theme.BackgroundDark
import com.akhara.ui.theme.PrimaryTeal
import com.akhara.ui.theme.TextSecondary
import java.time.format.DateTimeFormatter

@Composable
fun CalendarScreen(
    viewModel: CalendarViewModel,
    onLogWorkout: () -> Unit,
    onEditWorkout: (Int) -> Unit = {}
) {
    val state by viewModel.uiState.collectAsState()

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
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Calendar",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))

            GlassCard(innerPadding = 12.dp) {
                CalendarGrid(
                    yearMonth = state.yearMonth,
                    workoutDays = state.workoutDays,
                    selectedDate = state.selectedDate,
                    onDateClick = { viewModel.selectDate(it) },
                    onPreviousMonth = { viewModel.previousMonth() },
                    onNextMonth = { viewModel.nextMonth() }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (state.selectedDate != null) {
                Text(
                    text = state.selectedDate!!.format(DateTimeFormatter.ofPattern("EEEE, MMM d")),
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                if (state.selectedDaySessions.isEmpty()) {
                    GlassCard {
                        Text(
                            text = "No workouts on this day",
                            color = TextSecondary,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                } else {
                    state.selectedDaySessions.forEach { session ->
                        val sets = state.selectedSessionSets[session.id] ?: emptyList()
                        GlassCard(modifier = Modifier.padding(bottom = 8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "${sets.size} sets logged",
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 14.sp
                                    )
                                    val muscles = sets.map { it.exerciseId }.distinct()
                                    Text(
                                        text = "${muscles.size} exercises",
                                        fontSize = 13.sp,
                                        color = PrimaryTeal
                                    )
                                    if (sets.any { it.restSeconds > 0 }) {
                                        val avgRest = sets.filter { it.restSeconds > 0 }
                                            .map { it.restSeconds }.average().toInt()
                                        Text(
                                            text = "Avg rest: ${avgRest}s",
                                            fontSize = 12.sp,
                                            color = TextSecondary
                                        )
                                    }
                                }
                                Button(
                                    onClick = { onEditWorkout(session.id) },
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = PrimaryTeal,
                                        contentColor = BackgroundDark
                                    )
                                ) {
                                    Icon(
                                        Icons.Rounded.Edit,
                                        contentDescription = "Edit",
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.size(4.dp))
                                    Text("Edit", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}
