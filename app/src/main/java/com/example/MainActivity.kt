package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.QueryStats
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.Troubleshoot
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.ui.screens.ActivationScreen
import com.example.ui.screens.ChartScreen
import com.example.ui.screens.HomeDashboard
import com.example.ui.screens.MarketsScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SignalGeneratorScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.theme.CardDarkGray
import com.example.ui.theme.CardLightGray
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.PureBlack
import com.example.ui.theme.WolfGold
import com.example.ui.theme.WolfOrange
import com.example.ui.viewmodel.MainViewModel
import com.example.util.LocalizationHelper

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Request runtime permission for push alerts on Android 13+
        requestPostNotificationsPermission()

        setContent {
            MyApplicationTheme {
                MainAppShell(viewModel)
            }
        }
    }

    private fun requestPostNotificationsPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val hasPermission = ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED

            if (!hasPermission) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    1001
                )
            }
        }
    }
}

@Composable
fun MainAppShell(viewModel: MainViewModel) {
    val isSplashLoading by viewModel.isSplashLoading.collectAsState()
    val isActivated by viewModel.isActivated.collectAsState()
    val appLanguage by viewModel.appLanguage.collectAsState()

    val layoutDirection = if (appLanguage == "ar") LayoutDirection.Rtl else LayoutDirection.Ltr

    CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
        Crossfade(targetState = Pair(isSplashLoading, isActivated), label = "root_navigation") { (isSplash, active) ->
            when {
                isSplash -> {
                    SplashScreen(viewModel = viewModel)
                }
                !active -> {
                    ActivationScreen(viewModel = viewModel)
                }
                else -> {
                    AuthenticatedWorkspaceShell(viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun AuthenticatedWorkspaceShell(viewModel: MainViewModel) {
    val activeTabIndex by viewModel.activeTab.collectAsState()
    val appLanguage by viewModel.appLanguage.collectAsState()
 
    val layoutDirection = if (appLanguage == "ar") LayoutDirection.Rtl else LayoutDirection.Ltr
 
    CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            bottomBar = {
                CustomBottomNavigation(
                    appLanguage = appLanguage,
                    selectedIndex = activeTabIndex,
                    onTabSelected = { viewModel.setTab(it) }
                )
            },
            containerColor = PureBlack
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(PureBlack)
            ) {
                when (activeTabIndex) {
                    0 -> SignalGeneratorScreen(viewModel = viewModel)
                    1 -> ChartScreen(viewModel = viewModel)
                    2 -> MarketsScreen(viewModel = viewModel)
                    3 -> SettingsScreen(viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun CustomBottomNavigation(
    appLanguage: String,
    selectedIndex: Int,
    onTabSelected: (Int) -> Unit
) {
    val tabItems = listOf(
        Triple(LocalizationHelper.getString(appLanguage, "tab_signals"), Icons.Default.Troubleshoot, "dashboard_tab"),
        Triple(LocalizationHelper.getString(appLanguage, "tab_chart"), Icons.Default.ShowChart, "chart_tab"),
        Triple(LocalizationHelper.getString(appLanguage, "tab_markets"), Icons.Default.QueryStats, "markets_tab"),
        Triple(LocalizationHelper.getString(appLanguage, "tab_settings"), Icons.Default.Settings, "settings_tab")
    )

    NavigationBar(
        modifier = Modifier
            .height(68.dp)
            .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
            .testTag("main_bottom_nav"),
        containerColor = CardDarkGray,
        tonalElevation = 8.dp
    ) {
        tabItems.forEachIndexed { index, (label, icon, tag) ->
            val isSelected = selectedIndex == index
            NavigationBarItem(
                selected = isSelected,
                onClick = { onTabSelected(index) },
                icon = {
                    Icon(
                        imageVector = icon,
                        contentDescription = label,
                        modifier = Modifier.size(24.dp),
                        tint = if (isSelected) PureBlack else Color.White
                    )
                },
                label = {
                    Text(
                        text = label,
                        fontSize = 10.sp,
                        color = if (isSelected) WolfGold else Color.White,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = WolfGold,
                    selectedIconColor = PureBlack,
                    unselectedIconColor = Color.White,
                    selectedTextColor = WolfGold,
                    unselectedTextColor = Color.White
                ),
                modifier = Modifier.testTag(tag)
            )
        }
    }
}
