package com.akhara.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.akhara.ui.theme.BackgroundDark
import com.akhara.ui.theme.PrimaryTeal
import com.akhara.ui.theme.SurfaceVariant
import com.akhara.ui.theme.TextPrimary
import com.akhara.ui.theme.TextSecondary
import com.akhara.ui.theme.TextTertiary
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun CalendarGrid(
    yearMonth: YearMonth,
    workoutDays: Set<LocalDate>,
    selectedDate: LocalDate?,
    onDateClick: (LocalDate) -> Unit,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = onPreviousMonth) {
                Icon(Icons.Rounded.ChevronLeft, "Previous month", tint = TextPrimary)
            }
            Text(
                text = "${yearMonth.month.getDisplayName(TextStyle.FULL, Locale.getDefault())} ${yearMonth.year}",
                fontWeight = FontWeight.SemiBold,
                fontSize = 18.sp,
                color = TextPrimary
            )
            IconButton(onClick = onNextMonth) {
                Icon(Icons.Rounded.ChevronRight, "Next month", tint = TextPrimary)
            }
        }

        Row(modifier = Modifier.fillMaxWidth()) {
            val days = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
            days.forEach { day ->
                Text(
                    text = day,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextTertiary
                )
            }
        }

        val firstDay = yearMonth.atDay(1)
        val dayOfWeekOffset = (firstDay.dayOfWeek.value - 1)
        val daysInMonth = yearMonth.lengthOfMonth()
        val totalCells = dayOfWeekOffset + daysInMonth
        val rows = (totalCells + 6) / 7

        for (row in 0 until rows) {
            Row(modifier = Modifier.fillMaxWidth()) {
                for (col in 0 until 7) {
                    val cellIndex = row * 7 + col
                    val dayNum = cellIndex - dayOfWeekOffset + 1

                    if (dayNum in 1..daysInMonth) {
                        val date = yearMonth.atDay(dayNum)
                        val isWorkoutDay = date in workoutDays
                        val isSelected = date == selectedDate
                        val isToday = date == LocalDate.now()

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .padding(2.dp)
                                .clip(CircleShape)
                                .then(
                                    when {
                                        isSelected -> Modifier.background(PrimaryTeal, CircleShape)
                                        isWorkoutDay -> Modifier.background(PrimaryTeal.copy(alpha = 0.15f), CircleShape)
                                        else -> Modifier
                                    }
                                )
                                .then(
                                    if (isToday && !isSelected) Modifier.border(1.5.dp, PrimaryTeal, CircleShape)
                                    else Modifier
                                )
                                .clickable { onDateClick(date) },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "$dayNum",
                                fontSize = 14.sp,
                                fontWeight = if (isToday || isWorkoutDay) FontWeight.Bold else FontWeight.Normal,
                                color = when {
                                    isSelected -> BackgroundDark
                                    isWorkoutDay -> PrimaryTeal
                                    else -> TextSecondary
                                }
                            )
                            if (isWorkoutDay && !isSelected) {
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.BottomCenter)
                                        .padding(bottom = 4.dp)
                                        .size(4.dp)
                                        .background(PrimaryTeal, CircleShape)
                                )
                            }
                        }
                    } else {
                        Box(modifier = Modifier.weight(1f).aspectRatio(1f))
                    }
                }
            }
        }
    }
}
