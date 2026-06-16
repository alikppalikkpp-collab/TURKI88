package com.example.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.data.repository.SessionManager
import com.example.util.LocalizationHelper

object NotificationHelper {
    private const val CHANNEL_ID = "trading_signals_channel"
    private const val CHANNEL_NAME = "Wolf Trading Alerts"
    private const val NOTIFICATION_ID = 404

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = "Live premium trading signals from Wolf Trader Pro"
                enableLights(true)
                enableVibration(true)
            }
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun showSignalNotification(
        context: Context,
        pair: String,
        direction: String, // BUY or SELL
        price: Double,
        duration: Int,
        confidence: Int
    ) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Intent to open MainActivity when tapped
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val session = SessionManager(context)
        val lang = session.appLanguage

        val isBuy = direction.uppercase() == "BUY"
        val dirLabel = if (isBuy) {
            LocalizationHelper.getString(lang, "buy")
        } else {
            LocalizationHelper.getString(lang, "sell")
        }

        val entryPointLabel = LocalizationHelper.getString(lang, "entry_point")
        val durationLabel = LocalizationHelper.getString(lang, "duration")
        val minutesLabel = LocalizationHelper.getString(lang, "minutes", duration)
        val confidenceLabel = LocalizationHelper.getString(lang, "confidence_rate")

        // Mapping duration to relevant analysis timeframe
        val timeframeText = when (duration) {
            10 -> if (lang == "ar") "١ دقيقة (1M)" else "1 Min (1M)"
            else -> if (lang == "ar") "١ دقيقة (1M)" else "1 Min (1M)"
        }
        val timeframePrefix = if (lang == "ar") "فريم التحليل" else "Analysis Frame"

        val emoji = if (isBuy) "🔥" else "❄️"
        
        val title = "🐺 " + LocalizationHelper.getString(lang, "splash_subtitle")
        val message = "$pair → $dirLabel\n" +
                      "$entryPointLabel $price\n" +
                      "$durationLabel $minutesLabel\n" +
                      "⏱️ $timeframePrefix: $timeframeText\n" +
                      "$confidenceLabel $confidence%"

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_popup_reminder) // High-contrast system icon
            .setContentTitle(title)
            .setContentText("$pair → $dirLabel ($confidence%)")
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(NOTIFICATION_ID + System.currentTimeMillis().toInt() % 1000, notification)
    }
}
