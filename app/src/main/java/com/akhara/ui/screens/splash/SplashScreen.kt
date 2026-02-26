package com.akhara.ui.screens.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseInOutCubic
import androidx.compose.animation.core.EaseOutBack
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.akhara.R
import com.akhara.ui.theme.BackgroundDark
import com.akhara.ui.theme.PrimaryTeal
import com.akhara.ui.theme.TextSecondary
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onFinished: () -> Unit) {
    val logoScale = remember { Animatable(0f) }
    val logoAlpha = remember { Animatable(0f) }
    val ringAlpha = remember { Animatable(0f) }
    val textAlpha = remember { Animatable(0f) }
    val textOffset = remember { Animatable(30f) }
    val taglineAlpha = remember { Animatable(0f) }
    val glowScale = remember { Animatable(0.6f) }
    val exitAlpha = remember { Animatable(1f) }
    val exitScale = remember { Animatable(1f) }

    val infiniteTransition = rememberInfiniteTransition(label = "splash")
    val ringRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ring_rotation"
    )
    val glowPulse by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_pulse"
    )

    LaunchedEffect(Unit) {
        // Phase 1: Glow background emerges
        glowScale.animateTo(1.2f, tween(800, easing = EaseOutCubic))
    }
    LaunchedEffect(Unit) {
        delay(200)
        // Phase 2: Logo bounces in
        logoAlpha.animateTo(1f, tween(500, easing = EaseOutCubic))
    }
    LaunchedEffect(Unit) {
        delay(200)
        logoScale.animateTo(1f, tween(700, easing = EaseOutBack))
    }
    LaunchedEffect(Unit) {
        delay(500)
        // Phase 3: Ring appears
        ringAlpha.animateTo(1f, tween(400, easing = EaseOutCubic))
    }
    LaunchedEffect(Unit) {
        delay(800)
        // Phase 4: Text slides up
        textAlpha.animateTo(1f, tween(500, easing = EaseOutCubic))
    }
    LaunchedEffect(Unit) {
        delay(800)
        textOffset.animateTo(0f, tween(500, easing = EaseOutCubic))
    }
    LaunchedEffect(Unit) {
        delay(1100)
        // Phase 5: Tagline fades in
        taglineAlpha.animateTo(1f, tween(400, easing = EaseOutCubic))
    }
    LaunchedEffect(Unit) {
        delay(2200)
        // Phase 6: Exit — scale up and fade out
        exitScale.animateTo(1.15f, tween(500, easing = EaseInOutCubic))
    }
    LaunchedEffect(Unit) {
        delay(2200)
        exitAlpha.animateTo(0f, tween(500, easing = EaseInOutCubic))
        onFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .scale(exitScale.value)
            .alpha(exitAlpha.value),
        contentAlignment = Alignment.Center
    ) {
        // Background glow orb
        Box(
            modifier = Modifier
                .size(280.dp)
                .scale(glowScale.value)
                .alpha(glowPulse)
                .blur(80.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            PrimaryTeal.copy(alpha = 0.4f),
                            PrimaryTeal.copy(alpha = 0.1f),
                            Color.Transparent
                        )
                    )
                )
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(contentAlignment = Alignment.Center) {
                // Spinning arc ring
                Canvas(
                    modifier = Modifier
                        .size(160.dp)
                        .alpha(ringAlpha.value)
                ) {
                    rotate(ringRotation) {
                        drawArc(
                            brush = Brush.sweepGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    PrimaryTeal.copy(alpha = 0.1f),
                                    PrimaryTeal.copy(alpha = 0.6f),
                                    PrimaryTeal,
                                    Color.Transparent
                                )
                            ),
                            startAngle = 0f,
                            sweepAngle = 270f,
                            useCenter = false,
                            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                        )
                    }
                }

                // Outer subtle ring
                Canvas(
                    modifier = Modifier
                        .size(160.dp)
                        .alpha(ringAlpha.value * 0.3f)
                ) {
                    drawCircle(
                        color = PrimaryTeal,
                        style = Stroke(width = 1.dp.toPx())
                    )
                }

                // Logo icon
                Image(
                    painter = painterResource(id = R.mipmap.ic_launcher_foreground),
                    contentDescription = "Akhara",
                    modifier = Modifier
                        .size(120.dp)
                        .scale(logoScale.value)
                        .alpha(logoAlpha.value)
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            Text(
                text = "AKHARA",
                fontSize = 36.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 8.sp,
                color = PrimaryTeal,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .alpha(textAlpha.value)
                    .offset(y = textOffset.value.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Train. Track. Transform.",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 2.sp,
                color = TextSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.alpha(taglineAlpha.value)
            )
        }

        // Bottom subtle particles / dots
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .alpha(ringAlpha.value * 0.4f)
        ) {
            val w = size.width
            val h = size.height
            val dotPositions = listOf(
                Offset(w * 0.15f, h * 0.75f),
                Offset(w * 0.85f, h * 0.8f),
                Offset(w * 0.25f, h * 0.2f),
                Offset(w * 0.78f, h * 0.18f),
                Offset(w * 0.5f, h * 0.88f),
                Offset(w * 0.1f, h * 0.45f),
                Offset(w * 0.92f, h * 0.5f)
            )
            dotPositions.forEachIndexed { i, pos ->
                val radius = if (i % 2 == 0) 2.dp.toPx() else 1.5.dp.toPx()
                drawCircle(
                    color = PrimaryTeal.copy(alpha = 0.3f + (i * 0.05f)),
                    radius = radius,
                    center = pos
                )
            }
        }
    }
}
