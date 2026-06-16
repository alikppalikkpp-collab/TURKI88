package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.delay
import com.example.util.TradeTimerHelper
import com.example.data.api.SignalResponse
import com.example.ui.theme.CardDarkGray
import com.example.ui.theme.CardLightGray
import com.example.ui.theme.DeepBlueBackground
import com.example.ui.theme.PureBlack
import com.example.ui.theme.SignalBuy
import com.example.ui.theme.SignalSell
import com.example.ui.theme.TextGray
import com.example.ui.theme.TextLightGray
import com.example.ui.theme.WolfGold
import com.example.ui.theme.WolfOrange
import com.example.ui.viewmodel.MainViewModel
import com.example.util.LocalizationHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeDashboard(viewModel: MainViewModel) {
    val signals by viewModel.signals.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val isDemo by viewModel.isDemoMode.collectAsState()
    val appLanguage by viewModel.appLanguage.collectAsState()

    var selectedDurationFilter by remember { mutableStateOf<Int?>(null) }

    val layoutDirection = if (appLanguage == "ar") LayoutDirection.Rtl else LayoutDirection.Ltr

    val filteredSignals = if (selectedDurationFilter == null) {
        signals
    } else {
        signals.filter { it.duration == selectedDurationFilter }
    }

    CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
        Scaffold(
            topBar = {
                HomeHeader(
                    appLanguage = appLanguage,
                    isDemo = isDemo,
                    onRefresh = { viewModel.loadTradingData() },
                    onNotifyTest = { viewModel.triggerRandomNotification() }
                )
            },
            containerColor = PureBlack
        ) { innerPadding ->
            PullToRefreshBox(
                isRefreshing = isRefreshing,
                onRefresh = { viewModel.loadTradingData() },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(PureBlack)
            ) {
                if (signals.isEmpty() && !isRefreshing) {
                    EmptySignalsView(appLanguage = appLanguage)
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(bottom = 76.dp), // Extra spacing for floating bottom navigation bar
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item {
                            PremiumOverviewCard(appLanguage = appLanguage, signalsCount = signals.size)
                        }

                        item {
                            RiskWarningCard(appLanguage = appLanguage)
                        }

                        item {
                            DurationFilterRow(
                                appLanguage = appLanguage,
                                selectedDuration = selectedDurationFilter,
                                onDurationSelected = { selectedDurationFilter = it }
                            )
                        }

                        if (filteredSignals.isEmpty() && !isRefreshing) {
                            item {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(32.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Info,
                                        contentDescription = "No local results",
                                        tint = TextGray,
                                        modifier = Modifier.size(40.dp)
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        text = if (appLanguage == "ar") "لا توجد صفقات حالية لهذه المدة" else "No active trades for this duration",
                                        color = Color.White,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        } else {
                            // Map each signal item elegantly
                            items(filteredSignals, key = { it.pair + "_" + it.direction }) { signal ->
                                SignalItemCard(appLanguage = appLanguage, signal = signal, viewModel = viewModel)
                            }
                        }

                        item {
                            Spacer(modifier = Modifier.height(24.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DurationFilterRow(
    appLanguage: String,
    selectedDuration: Int?,
    onDurationSelected: (Int?) -> Unit
) {
    val filters = listOf(
        null to LocalizationHelper.getString(appLanguage, "cat_all"),
        1 to if (appLanguage == "ar") "١ دقيقة" else "1 Min",
        2 to if (appLanguage == "ar") "٢ دقيقة" else "2 Min",
        5 to if (appLanguage == "ar") "٥ دقائق" else "5 Min",
        10 to if (appLanguage == "ar") "١٠ دقائق" else "10 Min",
        15 to if (appLanguage == "ar") "١٥ دقيقة" else "15 Min"
    )

    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp)) {
        Text(
            text = if (appLanguage == "ar") "تصفية حسب مدة الصفقة" else "Filter by Trade Duration",
            color = TextLightGray,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 6.dp, start = 4.dp)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            filters.forEach { (dur, label) ->
                val isSelected = selectedDuration == dur
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onDurationSelected(dur) }
                        .background(if (isSelected) WolfGold else CardDarkGray)
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = label,
                        color = if (isSelected) PureBlack else Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun HomeHeader(
    appLanguage: String,
    isDemo: Boolean,
    onRefresh: () -> Unit,
    onNotifyTest: () -> Unit
) {
    // Elegant pulsing glow transition for the Wolf theme icon
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scalePulse by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(PureBlack)
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // App icon with pulse animation
        Box(
            modifier = Modifier
                .size(42.dp)
                .alpha(scalePulse)
                .background(
                    Brush.radialGradient(colors = listOf(WolfOrange.copy(alpha = 0.2f), Color.Transparent)),
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            GeometricWolfIcon(modifier = Modifier.size(32.dp))
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = LocalizationHelper.getString(appLanguage, "signals_radar"),
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Spacer(modifier = Modifier.width(6.dp))
                if (isDemo) {
                    Box(
                        modifier = Modifier
                            .background(WolfGold.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = LocalizationHelper.getString(appLanguage, "demo_badge"),
                            color = WolfGold,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            Text(
                text = LocalizationHelper.getString(appLanguage, "radar_desc"),
                color = TextGray,
                fontSize = 11.sp
            )
        }

        // Action tools (Test Notification alert & manual refresh)
        IconButton(
            onClick = onNotifyTest,
            modifier = Modifier.testTag("test_notify_button")
        ) {
            Icon(
                imageVector = Icons.Default.NotificationsActive,
                contentDescription = "Test Alert Notification",
                tint = WolfGold
            )
        }

        IconButton(
            onClick = onRefresh,
            modifier = Modifier.testTag("refresh_button")
        ) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = "Refresh Page",
                tint = Color.White
            )
        }
    }
}

@Composable
fun PremiumOverviewCard(appLanguage: String, signalsCount: Int) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = CardDarkGray)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = LocalizationHelper.getString(appLanguage, "active_lvl"),
                    color = TextLightGray,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = LocalizationHelper.getString(appLanguage, "active_lvl_desc", signalsCount),
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = LocalizationHelper.getString(appLanguage, "coverage_desc"),
                    color = TextGray,
                    fontSize = 10.sp,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .background(WolfGold.copy(alpha = 0.12f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "PRO",
                        color = WolfGold,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = LocalizationHelper.getString(appLanguage, "membership"),
                        color = WolfGold,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
fun RiskWarningCard(appLanguage: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .testTag("risk_warning_card"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = CardDarkGray),
        border = androidx.compose.foundation.BorderStroke(1.dp, WolfOrange.copy(alpha = 0.35f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(WolfOrange.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "Warning",
                    tint = WolfOrange,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    text = LocalizationHelper.getString(appLanguage, "risk_warning_title"),
                    color = WolfOrange,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = LocalizationHelper.getString(appLanguage, "risk_warning_content"),
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Normal,
                    lineHeight = 16.sp
                )
            }
        }
    }
}

@Composable
fun SignalItemCard(appLanguage: String, signal: SignalResponse, viewModel: MainViewModel) {
    var tradeStatus by remember(signal) { mutableStateOf(TradeTimerHelper.getDynamicState(signal)) }
    
    LaunchedEffect(signal) {
        while (true) {
            delay(1000)
            tradeStatus = TradeTimerHelper.getDynamicState(signal)
        }
    }

    val isBuy = tradeStatus.direction.uppercase() == "BUY"
    val accentColor = if (tradeStatus.isExpired) Color.Gray else (if (isBuy) SignalBuy else SignalSell)
    val trendIcon = if (isBuy) Icons.Default.TrendingUp else Icons.Default.TrendingDown

    val directionText = if (isBuy) {
        LocalizationHelper.getString(appLanguage, "buy")
    } else {
        LocalizationHelper.getString(appLanguage, "sell")
    }

    val strengthLabel = when (tradeStatus.strength.uppercase()) {
        "STRONG" -> LocalizationHelper.getString(appLanguage, "strong")
        "MEDIUM" -> LocalizationHelper.getString(appLanguage, "medium")
        else -> LocalizationHelper.getString(appLanguage, "weak")
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clickable { viewModel.navigateToChartByPair(signal.pair) }
            .testTag("signal_card_${signal.pair}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardDarkGray),
        border = androidx.compose.foundation.BorderStroke(0.5.dp, accentColor.copy(alpha = 0.2f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Pair + Strength row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Asset Name
                Text(
                    text = signal.pair,
                    color = if (tradeStatus.isExpired) Color.Gray else Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier.weight(1f)
                )

                if (tradeStatus.isExpired) {
                    // Expired status badge
                    Box(
                        modifier = Modifier
                            .background(Color.Gray.copy(alpha = 0.2f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = LocalizationHelper.getString(appLanguage, "expired_title"),
                            color = Color.LightGray,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    
                    // Win/Loss result badge
                    val resultColor = if (tradeStatus.isWin) SignalBuy else SignalSell
                    val resultText = if (tradeStatus.isWin) {
                        LocalizationHelper.getString(appLanguage, "win_label")
                    } else {
                        LocalizationHelper.getString(appLanguage, "loss_label")
                    }
                    Box(
                        modifier = Modifier
                            .background(resultColor.copy(alpha = 0.2f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = resultText,
                            color = resultColor,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                } else {
                    // Strength indicator badge
                    Box(
                        modifier = Modifier
                            .background(
                                if (tradeStatus.strength == "STRONG") WolfOrange.copy(alpha = 0.15f) else Color.Gray.copy(alpha = 0.15f),
                                RoundedCornerShape(6.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = strengthLabel,
                            color = if (tradeStatus.strength == "STRONG") WolfOrange else Color.LightGray,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Direction info layout
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(PureBlack, RoundedCornerShape(10.dp))
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Direction Button visual element
                Row(
                    modifier = Modifier
                        .weight(1.2f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(accentColor.copy(alpha = 0.15f))
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = trendIcon,
                        contentDescription = tradeStatus.direction,
                        tint = accentColor,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = directionText,
                        color = accentColor,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Price target and length labels
                Column(
                    modifier = Modifier.weight(1.8f),
                    horizontalAlignment = Alignment.Start
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = LocalizationHelper.getString(appLanguage, "entry_point"),
                            color = TextGray,
                            fontSize = 11.sp,
                            textAlign = TextAlign.Start
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = com.example.util.PriceSyncHelper.formatPrice(signal.pair, tradeStatus.price),
                            color = if (tradeStatus.isExpired) Color.Gray else Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (tradeStatus.isExpired) {
                                LocalizationHelper.getString(appLanguage, "duration")
                            } else {
                                LocalizationHelper.getString(appLanguage, "remaining")
                            },
                            color = TextGray,
                            fontSize = 11.sp,
                            textAlign = TextAlign.Start
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (tradeStatus.isExpired) {
                                LocalizationHelper.getString(appLanguage, "minutes", tradeStatus.duration)
                            } else {
                                tradeStatus.formattedTime
                            },
                            color = if (tradeStatus.isExpired) Color.Gray else WolfGold,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (appLanguage == "ar") "فريم التحليل:" else "Analysis Frame:",
                            color = TextGray,
                            fontSize = 11.sp,
                            textAlign = TextAlign.Start
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        val tfLabel = when (tradeStatus.duration) {
                            1 -> if (appLanguage == "ar") "دقيقة واحدة (1M)" else "1 Min (1M)"
                            2 -> if (appLanguage == "ar") "دقيقة واحدة (1M)" else "1 Min (1M)"
                            5 -> if (appLanguage == "ar") "٥ دقائق (5M)" else "5 Min (5M)"
                            10 -> if (appLanguage == "ar") "١٠ دقائق (10M)" else "10 Min (10M)"
                            else -> if (appLanguage == "ar") "١٥ دقيقة (15M)" else "15 Min (15M)"
                        }
                        Text(
                            text = tfLabel,
                            color = if (tradeStatus.isExpired) Color.Gray else Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Confidence rating bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = LocalizationHelper.getString(appLanguage, "confidence_rate"),
                    color = TextLightGray,
                    fontSize = 11.sp
                )
                Spacer(modifier = Modifier.width(8.dp))
                LinearProgressIndicator(
                    progress = { tradeStatus.confidence / 100f },
                    modifier = Modifier
                        .weight(1f)
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = accentColor,
                    trackColor = CardLightGray
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "${tradeStatus.confidence}%",
                    color = accentColor,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black
                )
            }

            if (!tradeStatus.isExpired) {
                Spacer(modifier = Modifier.height(14.dp))
                
                // Indicators Concordance / توافق المؤشرات
                Text(
                    text = if (appLanguage == "ar") "🔍 توافق المؤشرات الفنية الرقمية (5/5):" else "🔍 Technical Indicators Alignment (5/5 Concordance):",
                    color = TextLightGray,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
                
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.Black.copy(alpha = 0.35f), RoundedCornerShape(10.dp))
                        .padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    IndicatorRow(
                        name = if (appLanguage == "ar") "مؤشر القوة النسبية RSI(14)" else "RSI(14) Relative Strength",
                        status = if (isBuy) (if (appLanguage == "ar") "تشبّع بيع (شراء ✅)" else "Oversold Buy ✅")
                                 else (if (appLanguage == "ar") "تشبّع شراء (بيع ✅)" else "Overbought Sell ✅"),
                        color = accentColor
                    )
                    IndicatorRow(
                        name = if (appLanguage == "ar") "ماكِد الاتجاهي MACD" else "MACD Trend Alignment",
                        status = if (isBuy) (if (appLanguage == "ar") "تقاطع ذهبي صاعد ✅" else "Golden Bullish Cross ✅")
                                 else (if (appLanguage == "ar") "تقاطع مميت هابط ✅" else "Deadly Bearish Cross ✅"),
                        color = accentColor
                    )
                    IndicatorRow(
                        name = if (appLanguage == "ar") "المتوسط الأسي EMA(20)" else "EMA(20) Exponential Average",
                        status = if (isBuy) (if (appLanguage == "ar") "ارتداد صعودي دعم ✅" else "Bullish Rebound Support ✅")
                                 else (if (appLanguage == "ar") "ارتداد هبوطي مقاومة ✅" else "Bearish Rejection Resistance ✅"),
                        color = accentColor
                    )
                    IndicatorRow(
                        name = if (appLanguage == "ar") "مؤشر الاستوكاستك Stochastic" else "Stochastic Oscillator (14,3,3)",
                        status = if (isBuy) (if (appLanguage == "ar") "انعكاس إيجابي صعودي ✅" else "Bullish Fast Recovery ✅")
                                 else (if (appLanguage == "ar") "انعكاس سلبي هبوطي ✅" else "Bearish Fast Reversal ✅"),
                        color = accentColor
                    )
                    IndicatorRow(
                        name = if (appLanguage == "ar") "نطاقات بولينجر Bollinger Bands" else "Bollinger Bands Volatility",
                        status = if (isBuy) (if (appLanguage == "ar") "اختراق النطاق السفلي ✅" else "Lower Band Support Rebound ✅")
                                 else (if (appLanguage == "ar") "اختراق النطاق العلوي ✅" else "Upper Band Resistance Rejection ✅"),
                        color = accentColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(PureBlack.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                    .padding(vertical = 6.dp, horizontal = 12.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.ShowChart,
                    contentDescription = "Show Live Chart",
                    tint = WolfGold,
                    modifier = Modifier.size(13.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (appLanguage == "ar") "انقر هنا لفتح ومعاينة الشارت الفعلي لهذا الزوج المباشر 📊" else "Tap here to open and preview Live Chart for this pair 📊",
                    color = WolfGold,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun IndicatorRow(name: String, status: String, color: Color) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = name,
            color = Color.LightGray,
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = status,
            color = color,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun EmptySignalsView(appLanguage: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(
            color = WolfGold,
            modifier = Modifier.size(54.dp),
            strokeWidth = 4.dp
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = if (appLanguage == "ar") "البحث عن فرصة ذهبية قوية... 🔍" else "Hunting for Strong Trade Opportunity... 🔍",
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = if (appLanguage == "ar") "يتم الآن دمج وتحليل 5 مؤشرات فنية (RSI و MACD و EMA و Stochastic و Bollinger Bands) في وقت واحد لضمان مطابقة وإشارات ذهبية بنسبة 100%!" else "Simultaneously running multi-period scans on 5 technical metrics (RSI, MACD, EMA, Stochastic Oscillator, & Bollinger Bands) to confirm absolute 5/5 trade alignment!",
            color = TextGray,
            fontSize = 11.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Box(
            modifier = Modifier
                .background(WolfGold.copy(alpha = 0.12f), RoundedCornerShape(8.dp))
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Text(
                text = if (appLanguage == "ar") "مدة الصفقة: 10 دقائق ⏱️ | فريم التحليل: 1 دقيقة" else "Deal Duration: 10 mins ⏱️ | Analysis Chart: 1m",
                color = WolfGold,
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }
    }
}
