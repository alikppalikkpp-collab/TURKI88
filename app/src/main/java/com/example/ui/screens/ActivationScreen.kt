package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import com.example.util.GlobalKeyManager
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CardDarkGray
import com.example.ui.theme.DeepBlueBackground
import com.example.ui.theme.PureBlack
import com.example.ui.theme.TextGray
import com.example.ui.theme.TextLightGray
import com.example.ui.theme.WolfGold
import com.example.ui.theme.WolfOrange
import com.example.ui.viewmodel.MainViewModel
import com.example.util.LocalizationHelper

@Composable
fun ActivationScreen(viewModel: MainViewModel) {
    val context = LocalContext.current
    val key by viewModel.activationKey.collectAsState()
    val isValidating by viewModel.isValidating.collectAsState()
    val errorMsg by viewModel.activationError.collectAsState()
    val deviceId = viewModel.deviceId
    val apiUrl by viewModel.apiUrl.collectAsState()
    val isDemo by viewModel.isDemoMode.collectAsState()
    val appLanguage by viewModel.appLanguage.collectAsState()

    var showConfigUrl by remember { mutableStateOf(false) }
    var inputUrl by remember { mutableStateOf(apiUrl) }
    var hideKeyText by remember { mutableStateOf(true) }

    // Activation Key Generator States
    var generatorPasscode by remember { mutableStateOf("") }
    var isGeneratorUnlocked by remember { mutableStateOf(false) }
    var generatorError by remember { mutableStateOf(false) }
    var hidePasscodeText by remember { mutableStateOf(true) }
    var refreshKeysTrigger by remember { mutableStateOf(false) }
    var generatedKeyNotification by remember { mutableStateOf<String?>(null) }

    val layoutDirection = if (appLanguage == "ar") LayoutDirection.Rtl else LayoutDirection.Ltr

    // Layout Direction reacts live to App Language state
    CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(PureBlack, DeepBlueBackground)
                    )
                )
                .verticalScroll(rememberScrollState()),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                // High Contrast Language switch pill at the header of the page
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(WolfGold.copy(alpha = 0.15f))
                            .clickable { viewModel.toggleLanguage() }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                            .testTag("auth_lang_toggle")
                    ) {
                        Text(
                            text = LocalizationHelper.getString(appLanguage, "lang_toggle"),
                            color = WolfGold,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Logo/Geometric brand
                GeometricWolfIcon(modifier = Modifier.size(100.dp))

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = LocalizationHelper.getString(appLanguage, "app_title"),
                    color = WolfGold,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = LocalizationHelper.getString(appLanguage, "app_subtitle"),
                    color = TextLightGray,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 4.dp)
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Activation Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = CardDarkGray)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = LocalizationHelper.getString(appLanguage, "activate_license"),
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Start
                        )

                        Text(
                            text = LocalizationHelper.getString(appLanguage, "activate_desc"),
                            color = TextGray,
                            fontSize = 12.sp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp, bottom = 18.dp),
                            textAlign = TextAlign.Start
                        )

                        // Input Field
                        OutlinedTextField(
                            value = key,
                            onValueChange = { viewModel.setActivationKey(it) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("activation_key_input"),
                            label = { Text(LocalizationHelper.getString(appLanguage, "activation_key_label")) },
                            trailingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Key,
                                    contentDescription = "Key Icon",
                                    tint = WolfGold
                                )
                            },
                            leadingIcon = {
                                IconButton(onClick = { hideKeyText = !hideKeyText }) {
                                    Icon(
                                        imageVector = if (hideKeyText) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = "Toggle Visibility",
                                        tint = TextGray
                                    )
                                }
                            },
                            visualTransformation = if (hideKeyText) PasswordVisualTransformation() else VisualTransformation.None,
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = TextLightGray,
                                focusedBorderColor = WolfGold,
                                unfocusedBorderColor = CardDarkGray,
                                focusedLabelColor = WolfGold,
                                unfocusedLabelColor = TextGray,
                                focusedContainerColor = PureBlack,
                                unfocusedContainerColor = PureBlack
                            )
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Device ID support tag
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(PureBlack, RoundedCornerShape(6.dp))
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = LocalizationHelper.getString(appLanguage, "your_device_id"),
                                color = TextGray,
                                fontSize = 11.sp,
                                modifier = Modifier.weight(1f),
                                textAlign = TextAlign.Start
                            )
                            Text(
                                text = deviceId,
                                color = WolfOrange,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.testTag("device_id_text")
                            )
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        // Error state visible layout
                        AnimatedVisibility(visible = errorMsg != null) {
                            val errorTextToShow = errorMsg?.let { LocalizationHelper.getString(appLanguage, it) } ?: ""
                            Text(
                                text = errorTextToShow,
                                color = Color.Red,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 12.dp),
                                textAlign = TextAlign.Center
                            )
                        }

                        // Activate Button
                        Button(
                            onClick = { viewModel.validateActivationKey() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("activate_button"),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = WolfGold),
                            enabled = !isValidating
                        ) {
                            if (isValidating) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = PureBlack,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text(
                                    text = LocalizationHelper.getString(appLanguage, "activate_now_btn"),
                                    color = PureBlack,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Buy info / Helper link details
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = CardDarkGray.copy(alpha = 0.6f))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = LocalizationHelper.getString(appLanguage, "no_key"),
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = LocalizationHelper.getString(appLanguage, "no_key_desc"),
                            color = TextGray,
                            fontSize = 11.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier
                                .clickable {
                                    val intent = Intent(
                                        Intent.ACTION_VIEW,
                                        Uri.parse("https://t.me/Thwolf12345")
                                    )
                                    context.startActivity(intent)
                                }
                                .background(WolfOrange.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 16.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Link,
                                contentDescription = "Buy Link",
                                tint = WolfOrange,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = LocalizationHelper.getString(appLanguage, "buy_key_btn"),
                                color = WolfOrange,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Developer Telegram Link Card (@XJ1KI)
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = CardDarkGray),
                    border = androidx.compose.foundation.BorderStroke(1.dp, WolfGold.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://t.me/XJ1KI"))
                                context.startActivity(intent)
                            }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        GeometricWolfIcon(modifier = Modifier.size(28.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (appLanguage == "ar") "المطور الرسمي دائمًا ⚡" else "Official Developer Always ⚡",
                                color = WolfGold,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Black
                            )
                            Text(
                                text = if (appLanguage == "ar") "اضغط للتواصل الفوري مع المطور @XJ1KI" else "Press to contact official developer @XJ1KI",
                                color = TextGray,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(WolfGold.copy(alpha = 0.15f), RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Link,
                                contentDescription = "Developer Chat",
                                tint = WolfGold,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Activation Key Generator Protected Section (Passcode: turki-wolf)
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = CardDarkGray)
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
                                    imageVector = if (isGeneratorUnlocked) Icons.Default.Key else Icons.Default.Lock,
                                    contentDescription = "Generator Lock",
                                    tint = if (isGeneratorUnlocked) Color.Green else WolfOrange,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (appLanguage == "ar") "مولد مفاتيح التنشيط المطور" else "Premium Key Generator Suite",
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            if (isGeneratorUnlocked) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Color.Red.copy(alpha = 0.15f))
                                        .clickable {
                                            isGeneratorUnlocked = false
                                            generatorPasscode = ""
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

                        if (!isGeneratorUnlocked) {
                            Text(
                                text = if (appLanguage == "ar") "هذا القسم خاص بالمطور ومحمي بكلمة مرور. يرجى إدخال كلمة المرور الصحيحة لفتحه." else "This is a restricted developer suite. Enter password to access.",
                                color = TextGray,
                                fontSize = 11.sp,
                                textAlign = TextAlign.Start,
                                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                            )

                            OutlinedTextField(
                                value = generatorPasscode,
                                onValueChange = {
                                    generatorPasscode = it
                                    generatorError = false
                                },
                                modifier = Modifier.fillMaxWidth(),
                                label = { Text(if (appLanguage == "ar") "كلمة مرور المطور" else "Developer Passcode") },
                                leadingIcon = {
                                    IconButton(onClick = { hidePasscodeText = !hidePasscodeText }) {
                                        Icon(
                                            imageVector = if (hidePasscodeText) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                            contentDescription = "Toggle Passcode Visibility",
                                            tint = TextGray
                                        )
                                    }
                                },
                                singleLine = true,
                                visualTransformation = if (hidePasscodeText) PasswordVisualTransformation() else VisualTransformation.None,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = TextLightGray,
                                    focusedBorderColor = WolfGold,
                                    unfocusedBorderColor = PureBlack,
                                    focusedContainerColor = PureBlack,
                                    unfocusedContainerColor = PureBlack
                                )
                            )

                            AnimatedVisibility(visible = generatorError) {
                                Text(
                                    text = if (appLanguage == "ar") "❌ كلمة المرور غير صحيحة!" else "❌ Incorrect developer passcode!",
                                    color = Color.Red,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Button(
                                onClick = {
                                    if (generatorPasscode.trim() == "turki-wolf") {
                                        isGeneratorUnlocked = true
                                        generatorError = false
                                    } else {
                                        generatorError = true
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = WolfOrange)
                            ) {
                                Text(
                                    text = if (appLanguage == "ar") "إلغاء قفل القسم 🔓" else "Unlock suite 🔓",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        } else {
                            // Unlocked state content
                            Text(
                                text = if (appLanguage == "ar") "تم فتح مولد مفاتيح التنشيط بنجاح! يمكنك الآن توليد مفاتيح تنشيط صالحة للراديكال فوراً." else "Suite unlocked successfully! Generate any number of active keys here.",
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
                                    refreshKeysTrigger = !refreshKeysTrigger
                                    generatedKeyNotification = key
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Refresh, contentDescription = "Gen key", tint = PureBlack)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (appLanguage == "ar") "تـولـيـد مـفـتـاح جـديـد ⚡" else "GENERATE NEW KEY ⚡",
                                        color = PureBlack,
                                        fontWeight = FontWeight.Black
                                    )
                                }
                            }

                            generatedKeyNotification?.let { lastKey ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 10.dp),
                                    colors = CardDefaults.cardColors(containerColor = PureBlack),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.Green.copy(alpha = 0.5f))
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text(
                                            text = if (appLanguage == "ar") "المفتاح المولد حالياً:" else "Currently Generated Key:",
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
                                                    contentDescription = "Copy key",
                                                    tint = WolfGold,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                        }
                                        Text(
                                            text = if (appLanguage == "ar") "✨ انقر على أيقونة النسخ، ثم الصقه في حقل كود التنشيط بالأعلى للولوج!" else "✨ Copy the key and use it in the Activation field above!",
                                            color = TextLightGray,
                                            fontSize = 10.sp,
                                            modifier = Modifier.padding(top = 4.dp)
                                        )
                                    }
                                }
                            }

                            val activeKeys = GlobalKeyManager.getAllKeys()
                            if (activeKeys.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(10.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = if (appLanguage == "ar") "المفاتيح النشطة المتولدة (${activeKeys.size}):" else "Active Registered Keys (${activeKeys.size}):",
                                        color = TextLightGray,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 6.dp)
                                ) {
                                    // Let's list last 5 keys
                                    activeKeys.takeLast(5).reversed().forEach { activeKey ->
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
                                                    contentDescription = "Copy",
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

                Spacer(modifier = Modifier.height(24.dp))

                // Developer Configuration Toggle (Dynamic URL base)
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier
                            .clickable { showConfigUrl = !showConfigUrl }
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Server Settings",
                            tint = TextGray,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = LocalizationHelper.getString(appLanguage, "adv_server_settings"),
                            color = TextGray,
                            fontSize = 11.sp
                        )
                    }

                    if (showConfigUrl) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(containerColor = CardDarkGray)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = LocalizationHelper.getString(appLanguage, "edit_trading_server"),
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                OutlinedTextField(
                                    value = inputUrl,
                                    onValueChange = {
                                        inputUrl = it
                                        viewModel.setApiUrl(it)
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    textStyle = MaterialTheme.typography.bodySmall,
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = TextLightGray,
                                        focusedBorderColor = WolfGold,
                                        focusedContainerColor = PureBlack,
                                        unfocusedContainerColor = PureBlack
                                    )
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { viewModel.toggleDemoMode(!isDemo) }
                                        .background(
                                            if (isDemo) WolfGold.copy(alpha = 0.1f) else Color.Transparent,
                                            RoundedCornerShape(4.dp)
                                        )
                                        .padding(6.dp)
                                ) {
                                    Text(
                                        text = LocalizationHelper.getString(appLanguage, "demo_mode_label"),
                                        fontSize = 11.sp,
                                        color = if (isDemo) WolfGold else TextGray,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Text(
                                        text = if (isDemo) LocalizationHelper.getString(appLanguage, "enabled") else LocalizationHelper.getString(appLanguage, "disabled"),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isDemo) WolfGold else TextGray
                                    )
                                }
                                
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = LocalizationHelper.getString(appLanguage, "demo_hint"),
                                    fontSize = 10.sp,
                                    color = TextGray,
                                    textAlign = TextAlign.Start
                                )
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}
