package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.ui.viewinterop.AndroidView
import com.example.data.api.AssetResponse
import com.example.data.api.SignalResponse
import com.example.ui.theme.*
import com.example.ui.viewmodel.MainViewModel
import com.example.util.LocalizationHelper
import com.example.util.TradeTimerHelper
import kotlinx.coroutines.delay
import kotlin.math.abs
import kotlin.random.Random

// Candle class representing candlestick data
data class Candle(
    val open: Double,
    val close: Double,
    val high: Double,
    val low: Double
)

@Composable
fun ChartScreen(viewModel: MainViewModel) {
    val assets by viewModel.assets.collectAsState()
    val signals by viewModel.signals.collectAsState()
    val appLanguage by viewModel.appLanguage.collectAsState()

    val selectedAsset by viewModel.selectedChartAsset.collectAsState()
    var candles by remember { mutableStateOf<List<Candle>>(emptyList()) }
    var liveTickerPrice by remember { mutableStateOf(0.0) }
    var chartMode by remember { mutableStateOf("real") }

    val layoutDirection = if (appLanguage == "ar") LayoutDirection.Rtl else LayoutDirection.Ltr

    // Find first active signal/deal if it exists for the selected asset
    val associatedSignal = signals.firstOrNull { signal ->
        selectedAsset?.let { asset -> signal.pair.lowercase() == asset.name.lowercase() } == true
    }

    val tradeStatus = associatedSignal?.let { TradeTimerHelper.getDynamicState(it) }
    val isTradeActive = tradeStatus != null && !tradeStatus.isExpired
    val entryPrice = tradeStatus?.price
    val tradeDirection = tradeStatus?.direction

    // Set default asset once assets are loaded
    LaunchedEffect(assets) {
        if (selectedAsset == null && assets.isNotEmpty()) {
            assets.firstOrNull()?.let { viewModel.selectChartAsset(it) }
        }
    }

    // Generate static historical candlesticks when asset is switched
    LaunchedEffect(selectedAsset) {
        selectedAsset?.let { asset ->
            val startingPrice = associatedSignal?.price ?: when {
                asset.name.contains("BTC") -> 67420.0
                asset.name.contains("ETH") -> 3480.0
                asset.name.contains("BNB") -> 562.0
                asset.name.contains("JPY") || asset.name.contains("XAU") -> 152.0
                else -> 1.0825
            }

            // Create 20 static historical candles
            val list = mutableListOf<Candle>()
            val rand = Random(asset.name.hashCode())
            var currentPrice = startingPrice * 0.994 // start slightly below

            for (i in 0 until 18) {
                // Skew slight up/down
                val change = (rand.nextDouble() - 0.48) * (startingPrice * 0.0012)
                val open = currentPrice
                val close = currentPrice + change
                val high = maxOf(open, close) + rand.nextDouble() * (startingPrice * 0.0006)
                val low = minOf(open, close) - rand.nextDouble() * (startingPrice * 0.0006)
                list.add(Candle(open = open, close = close, high = high, low = low))
                currentPrice = close
            }

            // The 19th candle (last historical)
            val finalChange = (rand.nextDouble() - 0.5) * (startingPrice * 0.001)
            val open = currentPrice
            val close = startingPrice // Close matches exact entry price or base price
            val high = maxOf(open, close) + rand.nextDouble() * (startingPrice * 0.0003)
            val low = minOf(open, close) - rand.nextDouble() * (startingPrice * 0.0003)
            list.add(Candle(open = open, close = close, high = high, low = low))

            candles = list
            liveTickerPrice = startingPrice
        }
    }

    // Dynamic price action loop updating current candle in real-time
    LaunchedEffect(selectedAsset, isTradeActive, tradeDirection) {
        if (selectedAsset == null) return@LaunchedEffect
        while (true) {
            delay(800) // update tick speed
            if (candles.isNotEmpty()) {
                val lastCandle = candles.last()
                val priceStep = liveTickerPrice * 0.00015
                
                // If there's an active trade, skew the fluctuation in that direction to make the chart simulate active results!
                val skewInput = if (isTradeActive) {
                    if (tradeDirection?.uppercase() == "BUY") 0.54 else 0.42
                } else {
                    0.48
                }
                
                val randVal = Random.nextDouble()
                val tickFluc = (randVal - skewInput) * priceStep
                val newClose = (liveTickerPrice + tickFluc).coerceIn(lastCandle.low * 0.999, lastCandle.high * 1.001)
                
                liveTickerPrice = newClose
                
                // Update last candle in position
                val updatedCandles = candles.toMutableList()
                val oldCandle = updatedCandles.removeAt(updatedCandles.lastIndex)
                updatedCandles.add(
                    oldCandle.copy(
                        close = newClose,
                        high = maxOf(oldCandle.high, newClose),
                        low = minOf(oldCandle.low, newClose)
                    )
                )
                candles = updatedCandles
            }
        }
    }

    // Handle RTL and LTR
    CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
        Scaffold(
            topBar = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(PureBlack)
                        .padding(top = 16.dp, bottom = 8.dp)
                ) {
                    // Title and live pulse dot
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = LocalizationHelper.getString(appLanguage, "chart_title"),
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.weight(1f)
                        )
                        LiveIndicatorGlow()
                    }

                    Text(
                        text = LocalizationHelper.getString(appLanguage, "chart_desc"),
                        color = TextGray,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 2.dp)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = LocalizationHelper.getString(appLanguage, "chart_selector_prompt"),
                        color = TextLightGray,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 2.dp)
                    )

                    // Horizontal List of Currency Pairs
                    PairsSelectorRow(
                        assets = assets,
                        signals = signals,
                        selected = selectedAsset,
                        onAssetSelected = { viewModel.selectChartAsset(it) }
                    )
                }
            },
            containerColor = PureBlack
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(bottom = 76.dp) // height reservation for footer navigation tabs
                    .background(PureBlack)
            ) {
                selectedAsset?.let { asset ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp)
                    ) {
                        // Instrument quick panel
                        AssetStatusStrip(
                            appLanguage = appLanguage,
                            asset = asset,
                            livePrice = liveTickerPrice,
                            isTradeActive = isTradeActive,
                            tradeDirection = tradeDirection,
                            tradeStatus = tradeStatus
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Chart Mode Toggle Buttons
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(CardDarkGray, RoundedCornerShape(10.dp))
                                .padding(4.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val activeTabColor = WolfGold
                            val inactiveTabColor = Color.Transparent
                            
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (chartMode == "real") activeTabColor else inactiveTabColor)
                                    .clickable { chartMode = "real" }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (appLanguage == "ar") "📊 شارت كيوتكس الحي" else "📊 Live Quotex Chart",
                                    color = if (chartMode == "real") PureBlack else Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (chartMode == "analysis") activeTabColor else inactiveTabColor)
                                    .clickable { chartMode = "analysis" }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (appLanguage == "ar") "📈 شارت مستويات الصفقة" else "📈 Trade Levels Chart",
                                    color = if (chartMode == "analysis") PureBlack else Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Custom drawn/Live TradingView widget Container
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(320.dp)
                                .testTag("quotex_chart_card"),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = CardDarkGray),
                            border = BorderStroke(1.dp, CardLightGray)
                        ) {
                            Box(modifier = Modifier.fillMaxSize()) {
                                if (chartMode == "real") {
                                    TradingViewHtmlWidget(asset = asset, appLanguage = appLanguage)
                                } else {
                                    if (candles.isEmpty()) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.align(Alignment.Center),
                                            color = WolfGold
                                        )
                                    } else {
                                        // Math boundary limits
                                        val assetHighVal = candles.maxOfOrNull { it.high } ?: 1.0
                                        val assetLowVal = candles.minOfOrNull { it.low } ?: 0.0
                                        
                                        // Make sure entry price is strictly within bounds if trade is active
                                        val finalMin = minOf(assetLowVal, entryPrice ?: assetLowVal)
                                        val finalMax = maxOf(assetHighVal, entryPrice ?: assetHighVal)
                                        
                                        val range = finalMax - finalMin
                                        val pad = if (range == 0.0) 0.02 else range * 0.12
                                        val viewMin = finalMin - pad
                                        val viewMax = finalMax + pad

                                        val density = LocalDensity.current

                                        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                                            val canvasHeight = constraints.maxHeight.toFloat()
                                            
                                            // Draw Candle graphics
                                            Canvas(modifier = Modifier.fillMaxSize()) {
                                                val width = size.width
                                                val height = size.height

                                                // 1. Draw horizontal guide lines
                                                val partitions = 5
                                                for (i in 1..partitions) {
                                                    val y = height * i / (partitions + 1)
                                                    drawLine(
                                                        color = Color.White.copy(alpha = 0.04f),
                                                        start = Offset(0f, y),
                                                        end = Offset(width, y),
                                                        strokeWidth = 1f
                                                    )
                                                }

                                                // 2. Draw Candlesticks
                                                val candleWidth = width / (candles.size + 1)
                                                val spacing = candleWidth * 0.18f
                                                val rectWidth = candleWidth - spacing

                                                fun getPriceY(price: Double): Float {
                                                    return (height - ((price - viewMin) / (viewMax - viewMin)) * height).toFloat()
                                                }

                                                candles.forEachIndexed { idx, candle ->
                                                    val x = idx * candleWidth + (candleWidth / 2f)
                                                    val trendColor = if (candle.close >= candle.open) SignalBuy else SignalSell

                                                    // Wick
                                                    val wickHighY = getPriceY(candle.high)
                                                    val wickLowY = getPriceY(candle.low)
                                                    drawLine(
                                                        color = trendColor,
                                                        start = Offset(x, wickHighY),
                                                        end = Offset(x, wickLowY),
                                                        strokeWidth = 2.5f
                                                    )

                                                    // Body
                                                    val openY = getPriceY(candle.open)
                                                    val closeY = getPriceY(candle.close)
                                                    val bodyTop = minOf(openY, closeY)
                                                    val bodyLength = abs(closeY - openY).coerceAtLeast(3f)

                                                    drawRect(
                                                        color = trendColor,
                                                        topLeft = Offset(x - rectWidth / 2f, bodyTop),
                                                        size = Size(rectWidth, bodyLength)
                                                    )
                                                }

                                                // 3. Draw active entry price line (glowing line with dashed layout)
                                                if (isTradeActive && entryPrice != null) {
                                                    val entryY = getPriceY(entryPrice)
                                                    val entryColor = if (tradeDirection?.uppercase() == "BUY") SignalBuy else SignalSell
                                                    drawLine(
                                                        color = entryColor,
                                                        start = Offset(0f, entryY),
                                                        end = Offset(width, entryY),
                                                        strokeWidth = 4.5f,
                                                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(15f, 15f), 0f)
                                                    )
                                                }

                                                // 4. Draw current ticking live Price Line
                                                val currentY = getPriceY(liveTickerPrice)
                                                drawLine(
                                                    color = Color.White.copy(alpha = 0.28f),
                                                    start = Offset(0f, currentY),
                                                    end = Offset(width, currentY),
                                                    strokeWidth = 2f,
                                                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 5f), 0f)
                                                )
                                            }

                                            // Place Entry Level overlay label dynamically pinned exactly at entry coordinate
                                            if (isTradeActive && entryPrice != null) {
                                                val pxToDp = with(density) { 1.dp.toPx() }
                                                val computedY = (canvasHeight - ((entryPrice - viewMin) / (viewMax - viewMin)) * canvasHeight).toFloat()
                                                val yOffsetDp = (computedY / pxToDp) - 18f
                                                val coercedYOffset = yOffsetDp.coerceIn(4f, (canvasHeight / pxToDp) - 48f)

                                                val directionColor = if (tradeDirection?.uppercase() == "BUY") SignalBuy else SignalSell
                                                val isRtl = layoutDirection == LayoutDirection.Rtl

                                                Box(
                                                    modifier = Modifier
                                                        .offset(y = coercedYOffset.dp)
                                                        .padding(horizontal = 8.dp)
                                                        .align(if (isRtl) Alignment.TopStart else Alignment.TopEnd)
                                                        .background(
                                                            brush = Brush.horizontalGradient(
                                                                colors = listOf(directionColor, directionColor.copy(alpha = 0.75f))
                                                            ),
                                                            shape = RoundedCornerShape(8.dp)
                                                        )
                                                        .border(1.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                                        .padding(horizontal = 8.dp, vertical = 5.dp)
                                                ) {
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                                    ) {
                                                        Icon(
                                                            imageVector = if (tradeDirection?.uppercase() == "BUY") Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                                                            contentDescription = "Arrow",
                                                            tint = Color.White,
                                                            modifier = Modifier.size(14.dp)
                                                        )
                                                        Text(
                                                            text = if (tradeDirection?.uppercase() == "BUY") "صعود (Buy)" else "هبوط (Sell)",
                                                            color = Color.White,
                                                            fontSize = 11.sp,
                                                            fontWeight = FontWeight.Black
                                                        )
                                                        VerticalDivider(
                                                            modifier = Modifier.height(12.dp),
                                                            color = Color.White.copy(alpha = 0.4f)
                                                        )
                                                        Text(
                                                            text = "${entryPrice}",
                                                            color = Color.White,
                                                            fontSize = 11.sp,
                                                            fontWeight = FontWeight.Bold
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Active summary details card lower container
                        AnimatedContent(
                            targetState = Pair(isTradeActive, tradeDirection),
                            label = "deal_summary"
                        ) { (hasDeal, dir) ->
                            if (hasDeal && tradeStatus != null) {
                                ActiveDealBottomPanel(
                                    appLanguage = appLanguage,
                                    direction = dir,
                                    status = tradeStatus,
                                    entryPrice = entryPrice ?: 0.0,
                                    livePrice = liveTickerPrice
                                )
                            } else {
                                NoActiveDealBottomPanel(appLanguage = appLanguage)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LiveIndicatorGlow() {
    val infiniteTransition = rememberInfiniteTransition(label = "opacity")
    val alphaAnim by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "fade"
    )

    Box(
        modifier = Modifier
            .background(Color(0xFF00E676).copy(alpha = 0.15f), CircleShape)
            .padding(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .alpha(alphaAnim)
                .background(Color(0xFF00E676), CircleShape)
        )
    }
}

@Composable
fun PairsSelectorRow(
    assets: List<AssetResponse>,
    signals: List<SignalResponse>,
    selected: AssetResponse?,
    onAssetSelected: (AssetResponse) -> Unit
) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 2.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        items(assets, key = { it.name }) { asset ->
            val isSelected = selected?.name == asset.name
            
            // Check if this pair has a trigger/active deal
            val associatedSignal = signals.firstOrNull { it.pair.lowercase() == asset.name.lowercase() }
            val tradeStatus = associatedSignal?.let { TradeTimerHelper.getDynamicState(it) }
            val hasActiveTrade = tradeStatus != null && !tradeStatus.isExpired
            val tradeDirection = tradeStatus?.direction

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .border(
                        width = 1.dp,
                        color = if (isSelected) WolfGold else (if (hasActiveTrade) (if (tradeDirection == "BUY") SignalBuy.copy(alpha = 0.4f) else SignalSell.copy(alpha = 0.4f)) else Color.Transparent),
                        shape = RoundedCornerShape(8.dp)
                    )
                    .clickable { onAssetSelected(asset) }
                    .background(if (isSelected) CardLightGray else CardDarkGray)
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = asset.name,
                        color = if (isSelected) Color.White else TextLightGray,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )

                    // Placement indicators matching criteria: "ضع عند الزوج الي عنده صفقة علامة هبوط او صعود حسب الصفقة"
                    if (hasActiveTrade) {
                        val color = if (tradeDirection == "BUY") SignalBuy else SignalSell
                        val trendIcon = if (tradeDirection == "BUY") Icons.Default.TrendingUp else Icons.Default.TrendingDown
                        
                        Box(
                            modifier = Modifier
                                .background(color.copy(alpha = 0.15f), CircleShape)
                                .padding(2.dp)
                        ) {
                            Icon(
                                imageVector = trendIcon,
                                contentDescription = "Trade present",
                                tint = color,
                                modifier = Modifier.size(11.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AssetStatusStrip(
    appLanguage: String,
    asset: AssetResponse,
    livePrice: Double,
    isTradeActive: Boolean,
    tradeDirection: String?,
    tradeStatus: TradeTimerHelper.DynamicSignalState?
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CardDarkGray)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = asset.name,
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .background(WolfGold.copy(alpha = 0.12f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = LocalizationHelper.getString(appLanguage, "chart_live_badge"),
                            color = WolfGold,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Text(
                    text = asset.category,
                    color = TextGray,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                val digits = if (asset.name.contains("BTC")) 1 
                             else if (asset.name.contains("ETH") || asset.name.contains("BNB")) 2 
                             else if (asset.name.contains("JPY") || asset.name.contains("XAU")) 3 
                             else 5
                val formattedLive = String.format("%.${digits}f", livePrice)

                Text(
                    text = formattedLive,
                    color = if (isTradeActive) (if (tradeDirection?.uppercase() == "BUY") SignalBuy else SignalSell) else Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = if (isTradeActive) {
                        val durationMins = tradeStatus?.duration ?: 5
                        LocalizationHelper.getString(appLanguage, "minutes", durationMins)
                    } else {
                        "LIVE OTC STOCK FEED"
                    },
                    color = TextGray,
                    fontSize = 9.sp
                )
            }
        }
    }
}

@Composable
fun ActiveDealBottomPanel(
    appLanguage: String,
    direction: String?,
    status: TradeTimerHelper.DynamicSignalState,
    entryPrice: Double,
    livePrice: Double
) {
    val isBuy = direction?.uppercase() == "BUY"
    val accentColor = if (isBuy) SignalBuy else SignalSell
    val trendIcon = if (isBuy) Icons.Default.TrendingUp else Icons.Default.TrendingDown
    
    // Calculate if currently win or loss dynamically comparing liveTicker with entry price
    val isCurrentlyWinning = if (isBuy) livePrice >= entryPrice else livePrice <= entryPrice
    val resultColor = if (isCurrentlyWinning) SignalBuy else SignalSell

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = CardDarkGray),
        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.25f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .background(accentColor, CircleShape)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = LocalizationHelper.getString(
                        appLanguage,
                        if (isBuy) "chart_active_deal_buy" else "chart_active_deal_sell"
                    ),
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = "${status.confidence}% Confidence",
                    color = accentColor,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(PureBlack, RoundedCornerShape(10.dp))
                    .padding(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left metrics: entry price
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = LocalizationHelper.getString(appLanguage, "chart_entry_price"),
                        color = TextGray,
                        fontSize = 10.sp
                    )
                    Text(
                        text = "$entryPrice",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Middle metrics: countdown
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = LocalizationHelper.getString(appLanguage, "remaining"),
                        color = TextGray,
                        fontSize = 10.sp
                    )
                    Text(
                        text = status.formattedTime,
                        color = WolfGold,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black
                    )
                }

                // Right metrics: real-time profit/loss outcome status
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = if (appLanguage == "ar") "الحالة الآن:" else "Live projection:",
                        color = TextGray,
                        fontSize = 10.sp
                    )
                    Box(
                        modifier = Modifier
                            .background(resultColor.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = if (isCurrentlyWinning) {
                                LocalizationHelper.getString(appLanguage, "win_label")
                            } else {
                                LocalizationHelper.getString(appLanguage, "loss_label")
                            },
                            color = resultColor,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun NoActiveDealBottomPanel(appLanguage: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = CardDarkGray)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = "No active trade",
                tint = TextGray,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = LocalizationHelper.getString(appLanguage, "chart_no_active_deal"),
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Text(
                text = if (appLanguage == "ar") "راقب تبويب الإشارت لتعرف متى يصدر الذئب صفقة جديدة وسيعرض مؤشرها ومستواها فورا على هذا الشارت."
                       else "Keep an eye on the Signals tab to spot active trades. When issued by the server, their details will display instantly here.",
                color = TextGray,
                fontSize = 10.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Composable
fun TradingViewHtmlWidget(asset: AssetResponse, appLanguage: String) {
    val symbol = remember(asset.name) {
        val cleanName = asset.name.replace(" OTC", "").replace("/", "").trim().uppercase()
        when {
            cleanName.contains("BTC") || cleanName == "BTCUSD" -> "BINANCE:BTCUSDT"
            cleanName.contains("ETH") || cleanName == "ETHUSD" -> "BINANCE:ETHUSDT"
            cleanName.contains("BNB") || cleanName == "BNBUSD" -> "BINANCE:BNBUSDT"
            cleanName.contains("XAU") || cleanName == "XAUUSD" || cleanName.contains("GOLD") -> "OANDA:XAUUSD"
            else -> {
                // Return forex symbol
                "FX:$cleanName"
            }
        }
    }

    AndroidView(
        factory = { ctx ->
            WebView(ctx).apply {
                webViewClient = WebViewClient()
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                settings.useWideViewPort = true
                settings.loadWithOverviewMode = true
                settings.builtInZoomControls = false
                settings.displayZoomControls = false
                setBackgroundColor(0x131722)
            }
        },
        update = { webView ->
            if (webView.tag != symbol) {
                webView.tag = symbol
                val url = "https://s.tradingview.com/widgetembed/?symbol=$symbol&interval=1&theme=dark&style=1&timezone=Etc/UTC&locale=$appLanguage"
                webView.loadUrl(url)
            }
        },
        modifier = Modifier.fillMaxSize()
    )
}
