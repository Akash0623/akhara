package com.akhara.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.akhara.data.db.entity.Exercise
import com.akhara.ui.theme.CardBorderLight
import com.akhara.ui.theme.PrimaryTeal
import com.akhara.ui.theme.SurfaceCard
import com.akhara.ui.theme.TextSecondary

@Composable
fun ExerciseCard(
    exercise: Exercise,
    onAdd: (() -> Unit)? = null,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        onClick = { onClick?.invoke() },
        shape = RoundedCornerShape(14.dp),
        color = SurfaceCard.copy(alpha = 0.5f),
        border = BorderStroke(1.dp, CardBorderLight)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = exercise.name,
                    fontWeight = FontWeight.Medium,
                    fontSize = 15.sp
                )
                Text(
                    text = exercise.muscleGroup,
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }
            if (onAdd != null) {
                IconButton(onClick = onAdd) {
                    Icon(
                        Icons.Rounded.Add,
                        contentDescription = "Add exercise",
                        tint = PrimaryTeal
                    )
                }
            }
        }
    }
}
