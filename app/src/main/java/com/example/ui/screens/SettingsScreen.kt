package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AlternateEmail
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ContactSupport
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Web
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import com.example.util.GlobalKeyManager
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CardDarkGray
import com.example.ui.theme.CardLightGray
import com.example.ui.theme.PureBlack
import com.example.ui.theme.SignalSell
import com.example.ui.theme.TextGray
import com.example.ui.theme.TextLightGray
import com.example.ui.theme.WolfGold
import com.example.ui.theme.WolfOrange
import com.example.ui.viewmodel.MainViewModel
import com.example.util.LocalizationHelper

@Composable
fun SettingsScreen(viewModel: MainViewModel) {
    val context = LocalContext.current
    val isDemo by viewModel.isDemoMode.collectAsState()
    val deviceId = viewModel.deviceId
    val vipPlan by viewModel.vipPlan.collectAsState()
    val appLanguage by viewModel.appLanguage.collectAsState()

    // Key generator panel states
    var adminPasscode by remember { mutableStateOf("") }
    var isAdminUnlocked by remember { mutableStateOf(false) }
    var adminError by remember { mutableStateOf(false) }
    var hideAdminPasscode by remember { mutableStateOf(true) }
    var refreshAdminKeysTrigger by remember { mutableStateOf(false) }
    var generatedAdminKeyNotification by remember { mutableStateOf<String?>(null) }

    val layoutDirection = if (appLanguage == "ar") LayoutDirection.Rtl else LayoutDirection.Ltr

    CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
        Scaffold(
            topBar = {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(PureBlack)
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = LocalizationHelper.getString(appLanguage, "settings_support_title"),
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.weight(1f)
                    )
                }
            },
            containerColor = PureBlack
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp)
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = 84.dp) // extra space for bottom navbar
            ) {
                // Subscription diagnostics card
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = CardDarkGray)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = LocalizationHelper.getString(appLanguage, "account_license_details"),
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        InfoRowDetail(
                            appLanguage = appLanguage,
                            label = LocalizationHelper.getString(appLanguage, "sub_type"),
                            value = LocalizationHelper.getString(appLanguage, "sub_pro_value", vipPlan),
                            iconColor = WolfGold
                        )
                        InfoRowDetail(
                            appLanguage = appLanguage,
                            label = LocalizationHelper.getString(appLanguage, "device_id_label"),
                            value = deviceId,
                            iconColor = WolfOrange
                        )
                        InfoRowDetail(
                            appLanguage = appLanguage,
                            label = LocalizationHelper.getString(appLanguage, "feed_health_label"),
                            value = if (isDemo) {
                                LocalizationHelper.getString(appLanguage, "local_sim_active")
                            } else {
                                LocalizationHelper.getString(appLanguage, "connected_to_active_server")
                            },
                            iconColor = if (isDemo) WolfGold else Color.Green
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Interactive Language selection settings card
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = CardDarkGray)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.toggleLanguage() }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Language,
                            contentDescription = "Language",
                            tint = WolfGold,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (appLanguage == "ar") "اللغة (Language)" else "Language (اللغة)",
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (appLanguage == "ar") "العربية النشطة (تبديل إلى الانجليزية)" else "English Active (Switch to Arabic)",
                                color = TextGray,
                                fontSize = 10.sp
                            )
                        }
                        Box(
                            modifier = Modifier
                                .background(WolfGold.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = if (appLanguage == "ar") "EN" else "AR",
                                color = WolfGold,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Key Generator Panel (Passcode Lock: turki-wolf)
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = CardDarkGray),
                    border = androidx.compose.foundation.BorderStroke(1.dp, if (isAdminUnlocked) Color.Green.copy(alpha = 0.4f) else WolfOrange.copy(alpha = 0.4f))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (isAdminUnlocked) Icons.Default.Key else Icons.Default.Lock,
                                    contentDescription = "Admin Lock Icon",
                                    tint = if (isAdminUnlocked) Color.Green else WolfOrange,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (appLanguage == "ar") "لوحة تحكم توليد المفاتيح 🛠️" else "Key Generation Control Panel 🛠️",
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            if (isAdminUnlocked) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Color.Red.copy(alpha = 0.15f))
                                        .clickable {
                                            isAdminUnlocked = false
                                            adminPasscode = ""
                                        }
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = if (appLanguage == "ar") "قيد القفل" else "Lock Suite",
                                        color = Color.Red,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        if (!isAdminUnlocked) {
                            Text(
                                text = if (appLanguage == "ar") "هذه اللوحة خاصة بإدارة التطبيق لإنشاء أكواد التنشيط الخاصة بالمستخدمين. يرجى إدخال الرمز السري." else "This panel is reserved for app administration. Enter admin credentials to proceed.",
                                color = TextGray,
                                fontSize = 11.sp,
                                textAlign = TextAlign.Start,
                                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                            )

                            OutlinedTextField(
                                value = adminPasscode,
                                onValueChange = {
                                    adminPasscode = it
                                    adminError = false
                                },
                                modifier = Modifier.fillMaxWidth(),
                                label = { Text(if (appLanguage == "ar") "أدخل رمز الإدارة (turki-wolf)" else "Admin Passcode") },
                                leadingIcon = {
                                    IconButton(onClick = { hideAdminPasscode = !hideAdminPasscode }) {
                                        Icon(
                                            imageVector = if (hideAdminPasscode) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                            contentDescription = "Toggle Passcode Visibility",
                                            tint = TextGray
                                        )
                                    }
                                },
                                singleLine = true,
                                visualTransformation = if (hideAdminPasscode) PasswordVisualTransformation() else VisualTransformation.None,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = TextLightGray,
                                    focusedBorderColor = WolfGold,
                                    unfocusedBorderColor = PureBlack,
                                    focusedContainerColor = PureBlack,
                                    unfocusedContainerColor = PureBlack
                                )
                            )

                            if (adminError) {
                                Text(
                                    text = if (appLanguage == "ar") "❌ الرمز السري غير صحيح!" else "❌ Incorrect security passcode!",
                                    color = Color.Red,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Button(
                                onClick = {
                                    if (adminPasscode.trim() == "turki-wolf") {
                                        isAdminUnlocked = true
                                        adminError = false
                                    } else {
                                        adminError = true
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = WolfOrange)
                            ) {
                                Text(
                                    text = if (appLanguage == "ar") "فتح لوحة مفاتيح التنشيط 🔓" else "Unlock Admin Panel 🔓",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        } else {
                            // Unlocked state layout
                            Text(
                                text = if (appLanguage == "ar") "أهلاً بك في لوحة تحكم التنشيط! قم بتوليد مفاتيح عشوائية صالحة لتسجيل الدخول." else "Welcome back admin! You are now authorized to issue lifetime license keys.",
                                color = Color.Green,
                                fontSize = 11.sp,
                                textAlign = TextAlign.Start,
                                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                            )

                            Button(
                                onClick = {
                                    val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
                                    val random = (1..8).map { chars.random() }.joinToString("")
                                    val key = "WOLF-KEY-$random"
                                    GlobalKeyManager.addKey(key)
                                    refreshAdminKeysTrigger = !refreshAdminKeysTrigger
                                    generatedAdminKeyNotification = key
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Refresh, contentDescription = "Gen Key", tint = PureBlack)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (appLanguage == "ar") "تـولـيـد كـود تـنـشـيـط جـديـد ⚡" else "GENERATE NEW KEY ⚡",
                                        color = PureBlack,
                                        fontWeight = FontWeight.Black
                                    )
                                }
                            }

                            generatedAdminKeyNotification?.let { lastKey ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 10.dp),
                                    colors = CardDefaults.cardColors(containerColor = PureBlack),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.Green.copy(alpha = 0.5f))
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text(
                                            text = if (appLanguage == "ar") "كود التنشيط الجديد:" else "New Activation Key Issued:",
                                            color = TextGray,
                                            fontSize = 11.sp
                                        )
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = lastKey,
                                                color = Color.Green,
                                                fontSize = 16.sp,
                                                fontWeight = FontWeight.Black
                                            )

                                            val clipboardManager = LocalClipboardManager.current
                                            IconButton(
                                                onClick = {
                                                    clipboardManager.setText(AnnotatedString(lastKey))
                                                }
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.ContentCopy,
                                                    contentDescription = "Copy Issued Key",
                                                    tint = WolfGold,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                        }
                                        Text(
                                            text = if (appLanguage == "ar") "✨ تم تسجيل الكود في قواعد تفعيل التطبيق بنجاح!" else "✨ Successfully loaded and recorded into app local keys database!",
                                            color = TextLightGray,
                                            fontSize = 10.sp,
                                            modifier = Modifier.padding(top = 4.dp)
                                        )
                                    }
                                }
                            }

                            val activeRegisteredKeys = GlobalKeyManager.getAllKeys()
                            if (activeRegisteredKeys.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = if (appLanguage == "ar") "المفاتيح النشطة الحالية (${activeRegisteredKeys.size}):" else "Currently Active Keys (${activeRegisteredKeys.size}):",
                                    color = TextLightGray,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.fillMaxWidth(),
                                    textAlign = TextAlign.Start
                                )

                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 6.dp)
                                ) {
                                    activeRegisteredKeys.takeLast(5).reversed().forEach { activeKey ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 4.dp)
                                                .background(PureBlack, RoundedCornerShape(6.dp))
                                                .padding(horizontal = 8.dp, vertical = 6.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = activeKey,
                                                color = Color.White,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            val clipboardManager = LocalClipboardManager.current
                                            IconButton(
                                                modifier = Modifier.size(24.dp),
                                                onClick = { clipboardManager.setText(AnnotatedString(activeKey)) }
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.ContentCopy,
                                                    contentDescription = "Copy key",
                                                    tint = WolfGold,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Text(
                    text = LocalizationHelper.getString(appLanguage, "social_channels"),
                    color = TextLightGray,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(vertical = 12.dp, horizontal = 4.dp),
                    fontWeight = FontWeight.Bold
                )

                // Channels linking list
                TelegramLinkItem(
                    title = LocalizationHelper.getString(appLanguage, "telegram_channel_title"),
                    subtitle = LocalizationHelper.getString(appLanguage, "telegram_channel_desc"),
                    url = "https://t.me/ThwolfTrader12345"
                )

                TelegramLinkItem(
                    title = LocalizationHelper.getString(appLanguage, "telegram_support_title"),
                    subtitle = LocalizationHelper.getString(appLanguage, "telegram_support_desc"),
                    url = "https://t.me/Thwolf12345"
                )

                TelegramLinkItem(
                    title = LocalizationHelper.getString(appLanguage, "telegram_dev_title"),
                    subtitle = LocalizationHelper.getString(appLanguage, "telegram_dev_desc"),
                    url = "https://t.me/XJ1KI"
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Logout button
                Button(
                    onClick = { viewModel.logout() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("logout_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = SignalSell),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ExitToApp,
                        contentDescription = "Logout",
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = LocalizationHelper.getString(appLanguage, "logout_btn"),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }

                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}

@Composable
fun InfoRowDetail(appLanguage: String, label: String, value: String, iconColor: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .background(iconColor, CircleShape)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = "$label:",
            color = TextLightGray,
            fontSize = 12.sp,
            modifier = Modifier.weight(1.2f)
        )
        Text(
            text = value,
            color = Color.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.weight(1.8f),
            textAlign = TextAlign.Start
        )
    }
}

@Composable
fun TelegramLinkItem(title: String, subtitle: String, url: String) {
    val context = LocalContext.current
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                context.startActivity(intent)
            },
        colors = CardDefaults.cardColors(containerColor = CardDarkGray)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = subtitle,
                    color = TextGray,
                    fontSize = 10.sp,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(WolfGold.copy(alpha = 0.12f), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Link,
                    contentDescription = "Open Link",
                    tint = WolfGold,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}
