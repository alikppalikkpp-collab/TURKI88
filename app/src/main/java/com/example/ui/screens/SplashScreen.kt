package com.example.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.DeepBlueBackground
import com.example.ui.theme.PureBlack
import com.example.ui.theme.WolfGold
import com.example.ui.theme.WolfOrange
import com.example.ui.viewmodel.MainViewModel
import com.example.util.LocalizationHelper
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue

@Composable
fun SplashScreen(viewModel: MainViewModel) {
    val scale = remember { Animatable(0.7f) }
    val opacity = remember { Animatable(0f) }
    val appLanguage by viewModel.appLanguage.collectAsState()

    LaunchedEffect(Unit) {
        scale.animateTo(1.0f, animationSpec = tween(1200))
    }
    LaunchedEffect(Unit) {
        opacity.animateTo(1.0f, animationSpec = tween(1500))
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(PureBlack, DeepBlueBackground)
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.scale(scale.value)
        ) {
            // Draw a high-contrast geometric vector of an aggressive Wolf
            GeometricWolfIcon(modifier = Modifier.size(160.dp))

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "WOLF TRADER PRO",
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                color = WolfGold,
                textAlign = TextAlign.Center
            )

            Text(
                text = LocalizationHelper.getString(appLanguage, "splash_subtitle"),
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(40.dp))

            CircularProgressIndicator(
                color = WolfOrange,
                modifier = Modifier.size(36.dp),
                strokeWidth = 3.dp
            )
        }
    }
}

@Composable
fun GeometricWolfIcon(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height

        val goldBrush = Brush.linearGradient(
            colors = listOf(WolfGold, WolfOrange),
            start = Offset(0f, 0f),
            end = Offset(width, height)
        )

        // Draw wolf head using low-poly triangles for aggressive geometric modern aesthetic
        val mainPath = Path().apply {
            // Nose / Snout
            moveTo(width * 0.5f, height * 0.85f)
            lineTo(width * 0.42f, height * 0.7f)
            lineTo(width * 0.58f, height * 0.7f)
            close()

            // Snout Bridge
            moveTo(width * 0.5f, height * 0.45f)
            lineTo(width * 0.45f, height * 0.7f)
            lineTo(width * 0.55f, height * 0.7f)
            close()

            // Left Cheek
            moveTo(width * 0.45f, height * 0.7f)
            lineTo(width * 0.2f, height * 0.6f)
            lineTo(width * 0.35f, height * 0.45f)
            close()

            // Right Cheek
            moveTo(width * 0.55f, height * 0.7f)
            lineTo(width * 0.8f, height * 0.6f)
            lineTo(width * 0.65f, height * 0.45f)
            close()

            // Forehead / Eyes Area
            moveTo(width * 0.5f, height * 0.45f)
            lineTo(width * 0.35f, height * 0.45f)
            lineTo(width * 0.42f, height * 0.35f)
            close()

            moveTo(width * 0.5f, height * 0.45f)
            lineTo(width * 0.65f, height * 0.45f)
            lineTo(width * 0.58f, height * 0.35f)
            close()

            // Left Ear
            moveTo(width * 0.3f, height * 0.4f)
            lineTo(width * 0.15f, height * 0.15f)
            lineTo(width * 0.45f, height * 0.28f)
            close()

            // Right Ear
            moveTo(width * 0.7f, height * 0.4f)
            lineTo(width * 0.85f, height * 0.15f)
            lineTo(width * 0.55f, height * 0.28f)
            close()

            // Outer Jaws
            moveTo(width * 0.2f, height * 0.6f)
            lineTo(width * 0.35f, height * 0.8f)
            lineTo(width * 0.5f, height * 0.9f)
            lineTo(width * 0.65f, height * 0.8f)
            lineTo(width * 0.8f, height * 0.6f)
        }

        // Draw background shadow glow
        drawCircle(
            color = WolfOrange.copy(alpha = 0.15f),
            radius = width * 0.45f,
            center = Offset(width * 0.5f, height * 0.5f)
        )

        // Draw main lines
        drawPath(
            path = mainPath,
            brush = goldBrush,
            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
        )

        // Aggressive glowing wolf eyes (Triangular glowing red/orange dots)
        val leftEye = Path().apply {
            moveTo(width * 0.39f, height * 0.48f)
            lineTo(width * 0.44f, height * 0.49f)
            lineTo(width * 0.41f, height * 0.52f)
            close()
        }
        val rightEye = Path().apply {
            moveTo(width * 0.61f, height * 0.48f)
            lineTo(width * 0.56f, height * 0.49f)
            lineTo(width * 0.59f, height * 0.52f)
            close()
        }

        drawPath(path = leftEye, color = Color.Red)
        drawPath(path = rightEye, color = Color.Red)
    }
}
