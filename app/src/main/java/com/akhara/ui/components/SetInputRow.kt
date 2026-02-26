package com.akhara.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.akhara.ui.theme.Destructive
import com.akhara.ui.theme.PrimaryTeal
import com.akhara.ui.theme.SurfaceVariant
import com.akhara.ui.theme.TextPrimary
import com.akhara.ui.theme.TextTertiary

data class SetData(
    val setNumber: Int = 1,
    val reps: String = "",
    val weight: String = "",
    val restSeconds: String = "",
    val plannedReps: Int? = null,
    val plannedWeight: Float? = null
)

@Composable
fun SetInputRow(
    setData: SetData,
    onRepsChange: (String) -> Unit,
    onWeightChange: (String) -> Unit,
    onRestChange: (String) -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        if (setData.plannedReps != null || setData.plannedWeight != null) {
            val targetParts = mutableListOf<String>()
            setData.plannedReps?.let { targetParts.add("$it reps") }
            setData.plannedWeight?.let {
                val w = if (it == it.toInt().toFloat()) it.toInt().toString() else it.toString()
                targetParts.add("${w}kg")
            }
            Text(
                text = "Target: ${targetParts.joinToString(" x ")}",
                fontSize = 10.sp,
                color = PrimaryTeal.copy(alpha = 0.6f),
                modifier = Modifier.padding(start = 36.dp, bottom = 2.dp)
            )
        }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(SurfaceVariant.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = "${setData.setNumber}",
            modifier = Modifier.width(28.dp),
            color = PrimaryTeal,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            textAlign = TextAlign.Center
        )

        CompactField(
            value = setData.reps,
            onValueChange = onRepsChange,
            placeholder = "Reps",
            modifier = Modifier.weight(1f)
        )

        CompactField(
            value = setData.weight,
            onValueChange = onWeightChange,
            placeholder = "Kg",
            modifier = Modifier.weight(1f)
        )

        CompactField(
            value = setData.restSeconds,
            onValueChange = onRestChange,
            placeholder = "Rest(s)",
            modifier = Modifier.weight(1f)
        )

        IconButton(onClick = onDelete, modifier = Modifier.width(32.dp)) {
            Icon(
                Icons.Rounded.Close,
                contentDescription = "Remove set",
                tint = Destructive.copy(alpha = 0.7f)
            )
        }
    }
    }
}

@Composable
private fun CompactField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        placeholder = {
            Text(placeholder, fontSize = 12.sp, color = TextTertiary)
        },
        singleLine = true,
        textStyle = androidx.compose.ui.text.TextStyle(
            fontSize = 14.sp,
            color = TextPrimary,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Medium
        ),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent,
            focusedIndicatorColor = PrimaryTeal.copy(alpha = 0.5f),
            unfocusedIndicatorColor = Color.Transparent,
            cursorColor = PrimaryTeal
        )
    )
}
