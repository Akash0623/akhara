package com.akhara.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.akhara.ui.theme.PrimaryTeal
import com.akhara.ui.theme.TextSecondary

@Composable
fun WorkoutSummaryCard(
    date: String,
    muscleGroups: String,
    exerciseCount: Int,
    totalSets: Int,
    avgRestSeconds: Int,
    modifier: Modifier = Modifier
) {
    GlassCard(modifier = modifier) {
        Text(
            text = "Last Workout",
            style = MaterialTheme.typography.labelMedium,
            color = TextSecondary
        )
        Text(
            text = date,
            fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp
        )
        Text(
            text = muscleGroups,
            fontSize = 13.sp,
            color = PrimaryTeal
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            SummaryItem("Exercises", "$exerciseCount")
            SummaryItem("Sets", "$totalSets")
            SummaryItem("Avg Rest", "${avgRestSeconds}s")
        }
    }
}

@Composable
private fun SummaryItem(label: String, value: String) {
    Column {
        Text(
            text = value,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = PrimaryTeal
        )
        Text(
            text = label,
            fontSize = 11.sp,
            color = TextSecondary
        )
    }
}
