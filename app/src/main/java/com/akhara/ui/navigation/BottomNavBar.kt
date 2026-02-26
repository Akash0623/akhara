package com.akhara.ui.navigation

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.akhara.ui.theme.BackgroundDark
import com.akhara.ui.theme.NavBarSurface
import com.akhara.ui.theme.PrimaryTeal
import com.akhara.ui.theme.TextTertiary

@Composable
fun BottomNavBar(
    currentRoute: String?,
    onTabSelected: (Screen) -> Unit,
    onLogWorkout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val tabs = listOf(Screen.Home, Screen.Calendar, Screen.Library, Screen.Stats)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(NavBarSurface)
            .windowInsetsPadding(WindowInsets.navigationBars)
            .padding(horizontal = 8.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            tabs.take(2).forEach { screen ->
                NavItem(
                    screen = screen,
                    isSelected = currentRoute == screen.route,
                    onClick = { onTabSelected(screen) },
                    modifier = Modifier.weight(1f)
                )
            }

            FloatingActionButton(
                onClick = onLogWorkout,
                modifier = Modifier.size(52.dp),
                shape = RoundedCornerShape(16.dp),
                containerColor = PrimaryTeal,
                elevation = FloatingActionButtonDefaults.elevation(
                    defaultElevation = 0.dp,
                    pressedElevation = 0.dp
                )
            ) {
                Icon(
                    Icons.Rounded.Add,
                    contentDescription = "Log Workout",
                    tint = BackgroundDark,
                    modifier = Modifier.size(28.dp)
                )
            }

            tabs.drop(2).forEach { screen ->
                NavItem(
                    screen = screen,
                    isSelected = currentRoute == screen.route,
                    onClick = { onTabSelected(screen) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun NavItem(
    screen: Screen,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val iconColor by animateColorAsState(
        targetValue = if (isSelected) PrimaryTeal else TextTertiary,
        label = "nav_icon"
    )

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) { onClick() }
            .padding(vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Icon(
            imageVector = screen.icon,
            contentDescription = screen.title,
            tint = iconColor,
            modifier = Modifier.size(24.dp)
        )
        Text(
            text = screen.title,
            fontSize = 10.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            color = iconColor
        )
    }
}
