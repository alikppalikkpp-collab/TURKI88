package com.example.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = WolfGold,
    secondary = WolfOrange,
    tertiary = SignalBuy,
    background = PureBlack,
    surface = CardDarkGray,
    onPrimary = PureBlack,
    onSecondary = TextWhite,
    onTertiary = TextWhite,
    onBackground = TextWhite,
    onSurface = TextLightGray
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Force dark mode for premium fintech trading feel
    dynamicColor: Boolean = false, // Disable dynamic colors to preserve bespoke branding
    content: @Composable () -> Unit
) {
    val colorScheme = DarkColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            var context = view.context
            while (context is android.content.ContextWrapper && context !is Activity) {
                context = context.baseContext
            }
            (context as? Activity)?.window?.let { window ->
                window.statusBarColor = PureBlack.toArgb()
                window.navigationBarColor = PureBlack.toArgb()
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
