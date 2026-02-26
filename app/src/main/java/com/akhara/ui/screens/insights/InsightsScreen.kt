package com.akhara.ui.screens.insights

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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.FitnessCenter
import androidx.compose.material.icons.rounded.Healing
import androidx.compose.material.icons.rounded.Insights
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material.icons.rounded.SwapHoriz
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material.icons.rounded.TrendingUp
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.akhara.data.intelligence.Insight
import com.akhara.data.intelligence.InsightType
import com.akhara.ui.components.GlassCard
import com.akhara.ui.theme.BackgroundDark
import com.akhara.ui.theme.PrimaryTeal
import com.akhara.ui.theme.TextSecondary
import com.akhara.ui.theme.WarningAmber

@Composable
fun InsightsScreen(
    viewModel: InsightsViewModel,
    onBack: () -> Unit
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Rounded.ArrowBack, "Back")
                }
                Text(
                    text = "Insights",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (state.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = PrimaryTeal)
                }
            } else if (state.insights.isEmpty()) {
                GlassCard {
                    Text(
                        text = "No insights yet. Log a few workouts with a plan and check back!",
                        color = TextSecondary,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            } else {
                val grouped = state.insights.groupBy { insightCategory(it.type) }
                grouped.forEach { (category, insights) ->
                    Text(
                        text = category.uppercase(),
                        style = MaterialTheme.typography.labelMedium,
                        color = PrimaryTeal,
                        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                    )
                    insights.forEach { insight ->
                        InsightCard(insight = insight)
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
fun InsightCard(insight: Insight, modifier: Modifier = Modifier) {
    GlassCard(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = insightIcon(insight.type),
                contentDescription = null,
                tint = if (insight.priority == 1) WarningAmber else PrimaryTeal,
                modifier = Modifier.size(24.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = insight.title,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                )
                Text(
                    text = insight.message,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
    }
}

private fun insightIcon(type: InsightType): ImageVector = when (type) {
    InsightType.PR_COUNTDOWN, InsightType.PR_ACHIEVED -> Icons.Rounded.TrendingUp
    InsightType.MUSCLE_RECOVERY -> Icons.Rounded.Healing
    InsightType.FATIGUE_SCORE, InsightType.DELOAD_SUGGESTION -> Icons.Rounded.LocalFireDepartment
    InsightType.DURATION_PREDICTION -> Icons.Rounded.Timer
    InsightType.REST_ADVISOR -> Icons.Rounded.Speed
    InsightType.VOLUME_BALANCE -> Icons.Rounded.FitnessCenter
    InsightType.PLATEAU_DETECTED -> Icons.Rounded.TrendingUp
    InsightType.PROGRESSIVE_OVERLOAD -> Icons.Rounded.TrendingUp
    InsightType.COMPLETION_RATE, InsightType.ADHERENCE -> Icons.Rounded.Insights
    InsightType.EXERCISE_SUBSTITUTION -> Icons.Rounded.SwapHoriz
    InsightType.ML_PREDICTION, InsightType.LLM_SUMMARY -> Icons.Rounded.Insights
}

private fun insightCategory(type: InsightType): String = when (type) {
    InsightType.DURATION_PREDICTION, InsightType.MUSCLE_RECOVERY -> "Today"
    InsightType.COMPLETION_RATE, InsightType.ADHERENCE -> "This Week"
    InsightType.PROGRESSIVE_OVERLOAD, InsightType.PLATEAU_DETECTED,
    InsightType.FATIGUE_SCORE, InsightType.DELOAD_SUGGESTION,
    InsightType.VOLUME_BALANCE, InsightType.REST_ADVISOR,
    InsightType.EXERCISE_SUBSTITUTION -> "Trends"
    InsightType.PR_COUNTDOWN, InsightType.PR_ACHIEVED -> "Personal Records"
    InsightType.ML_PREDICTION, InsightType.LLM_SUMMARY -> "AI"
}
