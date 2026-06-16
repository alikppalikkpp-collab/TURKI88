package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.example.data.api.AssetResponse
import com.example.data.api.SignalResponse
import com.example.ui.theme.CardDarkGray
import com.example.ui.theme.CardLightGray
import com.example.ui.theme.PureBlack
import com.example.ui.theme.SignalBuy
import com.example.ui.theme.SignalSell
import com.example.ui.theme.TextGray
import com.example.ui.theme.TextLightGray
import com.example.ui.theme.WolfGold
import com.example.ui.theme.WolfOrange
import com.example.ui.viewmodel.MainViewModel
import com.example.util.LocalizationHelper

@Composable
fun MarketsScreen(viewModel: MainViewModel) {
    val assets by viewModel.assets.collectAsState()
    val signals by viewModel.signals.collectAsState()
    val favorites by viewModel.favorites.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val appLanguage by viewModel.appLanguage.collectAsState()

    var showOnlyFavorites by remember { mutableStateOf(false) }

    val layoutDirection = if (appLanguage == "ar") LayoutDirection.Rtl else LayoutDirection.Ltr

    CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
        Scaffold(
            topBar = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(PureBlack)
                        .padding(top = 16.dp, bottom = 8.dp)
                ) {
                    Text(
                        text = LocalizationHelper.getString(appLanguage, "markets_title"),
                        color = Color.White,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.padding(horizontal = 20.dp)
                    )
                    Text(
                        text = LocalizationHelper.getString(appLanguage, "markets_desc"),
                        color = TextGray,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 2.dp)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Search component
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { viewModel.setSearchQuery(it) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .testTag("asset_search_input"),
                        placeholder = {
                            Text(
                                text = LocalizationHelper.getString(appLanguage, "search_placeholder"),
                                color = TextGray,
                                fontSize = 13.sp
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search Market",
                                tint = WolfGold
                            )
                        },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = TextLightGray,
                            focusedBorderColor = WolfGold,
                            unfocusedBorderColor = CardDarkGray,
                            focusedContainerColor = CardDarkGray,
                            unfocusedContainerColor = CardDarkGray
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Filter Options (Categories and Favorites togglers)
                    CategoryFiltersRow(
                        appLanguage = appLanguage,
                        selected = selectedCategory,
                        onSelected = { viewModel.setSelectedCategory(it) },
                        showOnlyFavorites = showOnlyFavorites,
                        onToggleFavorites = { showOnlyFavorites = !showOnlyFavorites }
                    )
                }
            },
            containerColor = PureBlack
        ) { innerPadding ->
            val filteredAssets = assets.filter { asset ->
                // Filter by Category
                val matchesCategory = selectedCategory == "ALL" || asset.category == selectedCategory
                // Filter by Search Query
                val matchesSearch = asset.name.lowercase().contains(searchQuery.lowercase())
                // Filter by Favorites
                val matchesFavorites = !showOnlyFavorites || favorites.contains(asset.name)

                matchesCategory && matchesSearch && matchesFavorites
            }

            if (filteredAssets.isEmpty()) {
                EmptyMarketsView(appLanguage = appLanguage, query = searchQuery, hasFavoritesActive = showOnlyFavorites)
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(bottom = 76.dp), // Height spacing for navigation bar
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredAssets, key = { it.name }) { asset ->
                        // Match current asset with potential active trading signals
                        val associatedSignal = signals.firstOrNull { it.pair.lowercase() == asset.name.lowercase() }
                        val isFavorite = favorites.contains(asset.name)

                        AssetItemCard(
                            appLanguage = appLanguage,
                            asset = asset,
                            associatedSignal = associatedSignal,
                            isFavorite = isFavorite,
                            onFavoriteToggle = { viewModel.togglePairFavorite(asset.name) },
                            onCardClick = { viewModel.navigateToChart(asset) }
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun CategoryFiltersRow(
    appLanguage: String,
    selected: String,
    onSelected: (String) -> Unit,
    showOnlyFavorites: Boolean,
    onToggleFavorites: () -> Unit
) {
    val categories = listOf(
        "ALL" to LocalizationHelper.getString(appLanguage, "cat_all"),
        "FOREX" to LocalizationHelper.getString(appLanguage, "cat_forex"),
        "METALS" to LocalizationHelper.getString(appLanguage, "cat_metals"),
        "CRYPTO" to LocalizationHelper.getString(appLanguage, "cat_crypto"),
        "OTC" to LocalizationHelper.getString(appLanguage, "cat_otc")
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Favorites quick filter pill
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .clickable { onToggleFavorites() }
                .background(if (showOnlyFavorites) WolfOrange else CardDarkGray)
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (showOnlyFavorites) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "Favorites",
                    tint = if (showOnlyFavorites) Color.White else WolfGold,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = LocalizationHelper.getString(appLanguage, "favorites"),
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Category pills
        categories.forEach { (catId, labelText) ->
            val isSelected = selected == catId && !showOnlyFavorites
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onSelected(catId) }
                    .background(if (isSelected) WolfGold else CardDarkGray)
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Text(
                    text = labelText,
                    color = if (isSelected) PureBlack else Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun AssetItemCard(
    appLanguage: String,
    asset: AssetResponse,
    associatedSignal: SignalResponse?,
    isFavorite: Boolean,
    onFavoriteToggle: () -> Unit,
    onCardClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 2.dp)
            .clickable { onCardClick() }
            .testTag("asset_item_${asset.name}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CardDarkGray)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header Row: Star, Name, Subtitle
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Name & Type labels
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = asset.name,
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .background(Color.White.copy(alpha = 0.08f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(text = asset.category, color = TextGray, fontSize = 8.sp, fontWeight = FontWeight.SemiBold)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (appLanguage == "ar") "📊 معاينة" else "📊 Chart",
                            color = WolfGold.copy(alpha = 0.85f),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Favorite button
                IconButton(
                    onClick = onFavoriteToggle,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Toggle Favorite",
                        tint = if (isFavorite) WolfOrange else TextGray,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

             // Inline Signal visual indicators for asset
             AnimatedVisibility(visible = associatedSignal != null) {
                 associatedSignal?.let { signal ->
                     var tradeStatus by remember(signal) { mutableStateOf(TradeTimerHelper.getDynamicState(signal)) }
                     LaunchedEffect(signal) {
                         while (true) {
                             delay(1000)
                             tradeStatus = TradeTimerHelper.getDynamicState(signal)
                         }
                     }

                     val isBuy = tradeStatus.direction.uppercase() == "BUY"
                     val buySellColor = if (tradeStatus.isExpired) Color.Gray else (if (isBuy) SignalBuy else SignalSell)
                     val indicatorIcon = if (isBuy) Icons.Default.TrendingUp else Icons.Default.TrendingDown

                     val directionLabel = if (isBuy) {
                         LocalizationHelper.getString(appLanguage, "buy_direction")
                     } else {
                         LocalizationHelper.getString(appLanguage, "sell_direction")
                     }

                     Row(
                         modifier = Modifier
                             .fillMaxWidth()
                             .padding(top = 8.dp)
                             .background(PureBlack, RoundedCornerShape(8.dp))
                             .padding(8.dp),
                         verticalAlignment = Alignment.CenterVertically
                     ) {
                         Icon(
                             imageVector = indicatorIcon,
                             contentDescription = "Signal Trend",
                             tint = buySellColor,
                             modifier = Modifier.size(16.dp)
                         )
                         Spacer(modifier = Modifier.width(6.dp))
                         Text(
                             text = if (tradeStatus.isExpired) {
                                 val outcomeText = if (tradeStatus.isWin) {
                                     LocalizationHelper.getString(appLanguage, "win_label")
                                 } else {
                                     LocalizationHelper.getString(appLanguage, "loss_label")
                                 }
                                 "${signal.pair} • ${LocalizationHelper.getString(appLanguage, "expired_title")} (${outcomeText})"
                             } else {
                                 LocalizationHelper.getString(appLanguage, "active_signal_label", directionLabel, tradeStatus.confidence)
                             },
                             color = buySellColor,
                             fontSize = 11.sp,
                             fontWeight = FontWeight.Bold,
                             modifier = Modifier.weight(1f)
                         )
                         Column(horizontalAlignment = Alignment.End) {
                             Text(
                                 text = "${LocalizationHelper.getString(appLanguage, "entry_point")} " + com.example.util.PriceSyncHelper.formatPrice(signal.pair, tradeStatus.price),
                                 color = if (tradeStatus.isExpired) Color.Gray else TextLightGray,
                                 fontSize = 11.sp
                             )
                             Text(
                                 text = if (tradeStatus.isExpired) {
                                     "${LocalizationHelper.getString(appLanguage, "duration")} ${LocalizationHelper.getString(appLanguage, "minutes", tradeStatus.duration)}"
                                 } else {
                                     "${LocalizationHelper.getString(appLanguage, "remaining")} ${tradeStatus.formattedTime}"
                                 },
                                 color = if (tradeStatus.isExpired) Color.Gray else WolfGold,
                                 fontSize = 10.sp
                             )
                         }
                     }
                 }
             }

            if (associatedSignal == null) {
                Text(
                    text = LocalizationHelper.getString(appLanguage, "no_active_signals"),
                    color = CardLightGray,
                    fontSize = 10.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

@Composable
fun EmptyMarketsView(appLanguage: String, query: String, hasFavoritesActive: Boolean) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Search,
            contentDescription = "Search",
            tint = TextGray,
            modifier = Modifier.size(48.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = LocalizationHelper.getString(appLanguage, "no_results_found"),
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = if (hasFavoritesActive) {
                LocalizationHelper.getString(appLanguage, "empty_fav_desc")
            } else {
                LocalizationHelper.getString(appLanguage, "empty_search_desc", query)
            },
            color = TextGray,
            fontSize = 11.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 6.dp)
        )
    }
}
