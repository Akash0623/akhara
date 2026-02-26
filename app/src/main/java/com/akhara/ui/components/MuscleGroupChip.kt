package com.akhara.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.akhara.ui.theme.ChipActiveBg
import com.akhara.ui.theme.ChipInactiveBg
import com.akhara.ui.theme.PrimaryTeal
import com.akhara.ui.theme.SurfaceBorder
import com.akhara.ui.theme.TextSecondary

@Composable
fun MuscleGroupChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bgColor by animateColorAsState(
        targetValue = if (selected) ChipActiveBg else ChipInactiveBg,
        label = "chip_bg"
    )
    val borderColor by animateColorAsState(
        targetValue = if (selected) PrimaryTeal.copy(alpha = 0.4f) else SurfaceBorder.copy(alpha = 0.1f),
        label = "chip_border"
    )
    val textColor by animateColorAsState(
        targetValue = if (selected) PrimaryTeal else TextSecondary,
        label = "chip_text"
    )

    Surface(
        modifier = modifier,
        onClick = onClick,
        shape = RoundedCornerShape(999.dp),
        color = bgColor,
        border = BorderStroke(1.dp, borderColor)
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            color = textColor,
            fontSize = 13.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}
