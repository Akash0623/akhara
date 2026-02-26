package com.akhara.ui.screens.stats

import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.akhara.ui.components.GlassCard
import com.akhara.ui.components.MuscleGroupChip
import com.akhara.ui.components.StatMiniCard
import com.akhara.ui.screens.insights.InsightCard
import com.akhara.ui.theme.BackgroundDark
import com.akhara.ui.theme.PrimaryTeal
import com.akhara.ui.theme.SurfaceVariant
import com.akhara.ui.theme.TextSecondary
import com.akhara.ui.theme.TextTertiary
import com.akhara.ui.theme.WarningAmber

@Composable
fun StatsScreen(viewModel: StatsViewModel, onSeeAllInsights: () -> Unit = {}) {
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
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Stats",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatsPeriod.entries.forEach { period ->
                    MuscleGroupChip(
                        label = period.label,
                        selected = state.selectedPeriod == period,
                        onClick = { viewModel.selectPeriod(period) }
                    )
                }
            }

            GlassCard(glowBorder = true) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                Icons.Rounded.LocalFireDepartment,
                                null,
                                tint = if (state.currentStreak > 0) WarningAmber else TextSecondary
                            )
                            Text(
                                text = "${state.currentStreak}",
                                fontSize = 36.sp,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryTeal
                            )
                        }
                        Text(
                            text = "week streak",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "Best: ${state.longestStreak}",
                            fontWeight = FontWeight.Medium,
                            color = TextSecondary
                        )
                        Text(
                            text = "Goal: ${state.weeklyGoal}/week",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatMiniCard(
                    label = "WORKOUTS",
                    value = "${state.totalWorkouts}",
                    modifier = Modifier.weight(1f)
                )
                StatMiniCard(
                    label = "VOLUME",
                    value = state.totalVolume,
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatMiniCard(
                    label = "AVG REST",
                    value = state.avgRestTime,
                    modifier = Modifier.weight(1f)
                )
                StatMiniCard(
                    label = "TOP MUSCLE",
                    value = state.mostTrainedMuscle,
                    modifier = Modifier.weight(1f)
                )
            }

            if (state.dailyVolumes.isNotEmpty()) {
                val barColors = state.dailyStatuses.map { status ->
                    when (status) {
                        DayStatus.COMPLETED -> PrimaryTeal
                        DayStatus.MISSED -> WarningAmber
                        DayStatus.REST, DayStatus.FUTURE -> Color.Transparent
                    }
                }.ifEmpty { null }
                val labelColors = state.dailyStatuses.map { status ->
                    when (status) {
                        DayStatus.COMPLETED -> PrimaryTeal
                        DayStatus.MISSED -> WarningAmber
                        DayStatus.REST -> TextTertiary
                        DayStatus.FUTURE -> TextTertiary.copy(alpha = 0.4f)
                    }
                }.ifEmpty { null }

                GlassCard {
                    Text(
                        text = "THIS WEEK",
                        style = MaterialTheme.typography.labelMedium,
                        color = TextSecondary,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    LabeledBarChart(
                        data = state.dailyVolumes,
                        labels = state.dailyLabels,
                        barColors = barColors,
                        labelColors = labelColors,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp)
                    )
                }
            }

            if (state.weeklyVolumes.isNotEmpty()) {
                GlassCard {
                    Text(
                        text = "8-WEEK TREND",
                        style = MaterialTheme.typography.labelMedium,
                        color = TextSecondary,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    LabeledBarChart(
                        data = state.weeklyVolumes,
                        labels = state.weeklyLabels,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp)
                    )
                }
            }

            if (state.bodyWeightData.isNotEmpty()) {
                GlassCard {
                    Text(
                        text = "BODY WEIGHT",
                        style = MaterialTheme.typography.labelMedium,
                        color = TextSecondary,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    LabeledBarChart(
                        data = state.bodyWeightData,
                        labels = state.bodyWeightLabels,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp)
                    )
                }
            }

            if (state.topInsights.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "INSIGHTS",
                        style = MaterialTheme.typography.labelMedium,
                        color = TextSecondary
                    )
                    androidx.compose.material3.TextButton(onClick = onSeeAllInsights) {
                        Text("See All", color = PrimaryTeal, fontSize = 12.sp)
                    }
                }
                state.topInsights.forEach { insight ->
                    InsightCard(insight = insight)
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
private fun LabeledBarChart(
    data: List<Float>,
    labels: List<String>,
    modifier: Modifier = Modifier,
    barColors: List<Color>? = null,
    labelColors: List<Color>? = null
) {
    val defaultBarColor = PrimaryTeal
    val trackColor = SurfaceVariant
    val missedIndicatorHeight = 0.06f

    Column(modifier = modifier) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            if (data.isEmpty()) return@Canvas
            val maxVal = data.max().coerceAtLeast(1f)
            val barCount = data.size
            val spacing = 8.dp.toPx()
            val barWidth = (size.width - spacing * (barCount - 1)) / barCount
            val cornerRad = CornerRadius(6f, 6f)

            data.forEachIndexed { i, value ->
                val x = i * (barWidth + spacing)
                val color = barColors?.getOrNull(i) ?: defaultBarColor
                val trackHeight = size.height * 0.9f

                drawRoundRect(
                    color = trackColor,
                    topLeft = Offset(x, size.height - trackHeight),
                    size = Size(barWidth, trackHeight),
                    cornerRadius = cornerRad
                )

                val barHeight = if (value > 0f) {
                    (value / maxVal) * size.height * 0.9f
                } else if (color != Color.Transparent && color != defaultBarColor) {
                    size.height * missedIndicatorHeight
                } else {
                    0f
                }

                if (barHeight > 0f) {
                    drawRoundRect(
                        color = color,
                        topLeft = Offset(x, size.height - barHeight),
                        size = Size(barWidth, barHeight),
                        cornerRadius = cornerRad
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            labels.forEachIndexed { i, label ->
                Text(
                    text = label,
                    fontSize = 10.sp,
                    color = labelColors?.getOrNull(i) ?: TextSecondary,
                    fontWeight = if (labelColors?.getOrNull(i) == PrimaryTeal) FontWeight.Bold else FontWeight.Normal,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}
