package com.example.data.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.MainActivity
import com.example.R
import com.example.model.SetupType
import com.example.model.Stock
import java.util.concurrent.atomic.AtomicInteger

class PushNotificationHelper(private val context: Context) {

    companion object {
        const val CHANNEL_ID = "sma44_signals_channel"
        const val CHANNEL_NAME = "SMA 44 Scanner Real-Time Alerts"
        const val CHANNEL_DESC = "Instant notifications for Rising & Falling 44 SMA bounces, breakouts and rejections on NSE & BSE"
        private val notificationIdCounter = AtomicInteger(1001)

        const val EXTRA_STOCK_SYMBOL = "extra_stock_symbol"
        const val EXTRA_STOCK_EXCHANGE = "extra_stock_exchange"
    }

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = CHANNEL_DESC
                enableLights(true)
                lightColor = Color.parseColor("#FFB300")
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 250, 150, 250)
                setShowBadge(true)
            }

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun canSendNotifications(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            NotificationManagerCompat.from(context).areNotificationsEnabled()
        }
    }

    fun sendStockAlertNotification(
        stock: Stock,
        customMessage: String? = null
    ): Int {
        if (!canSendNotifications()) {
            return -1
        }

        val notificationId = notificationIdCounter.incrementAndGet()

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_STOCK_SYMBOL, stock.symbol)
            putExtra(EXTRA_STOCK_EXCHANGE, stock.exchange.name)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val isBullish = stock.setupType.isBullish
        val iconEmoji = if (isBullish) "🟢" else "🔴"
        val title = "$iconEmoji [${stock.exchange}] ${stock.symbol}: ${stock.setupType.title}"

        val message = customMessage ?: when (stock.setupType) {
            SetupType.BULLISH_BOUNCE ->
                "Price ₹${stock.currentPrice} pulled back to Rising 44 SMA (₹${stock.sma44}) & bounced! SL: ₹${stock.stopLoss} | Tgt: ₹${stock.target1}"
            SetupType.BULLISH_BREAKOUT ->
                "Strong Breakout! ₹${stock.currentPrice} surged through 44 SMA (₹${stock.sma44}) with volume. SL: ₹${stock.stopLoss} | Tgt: ₹${stock.target1}"
            SetupType.BEARISH_REJECTION ->
                "Price ₹${stock.currentPrice} rejected at Falling 44 SMA resistance (₹${stock.sma44}). Downside risk! SL: ₹${stock.stopLoss} | Tgt: ₹${stock.target1}"
            SetupType.BEARISH_BREAKDOWN ->
                "Support Breakdown! ₹${stock.currentPrice} cracked below 44 SMA (₹${stock.sma44}). SL: ₹${stock.stopLoss} | Tgt: ₹${stock.target1}"
            SetupType.RISING_TREND ->
                "Uptrend momentum continues above Rising 44 SMA (₹${stock.sma44}). CMP: ₹${stock.currentPrice} (+${stock.changePercent}%)"
            SetupType.FALLING_TREND ->
                "Downtrend continuation below Falling 44 SMA (₹${stock.sma44}). CMP: ₹${stock.currentPrice} (${stock.changePercent}%)"
            SetupType.CONSOLIDATING ->
                "Testing 44 SMA zone at ₹${stock.currentPrice} (${stock.distanceToSmaPercent}% from SMA). Watch for breakout!"
        }

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_EVENT)
            .setColor(if (isBullish) Color.parseColor("#00C853") else Color.parseColor("#FF3B30"))
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        try {
            NotificationManagerCompat.from(context).notify(notificationId, builder.build())
        } catch (e: SecurityException) {
            return -1
        }

        return notificationId
    }

    fun sendTestPushNotification(): Int {
        if (!canSendNotifications()) {
            return -1
        }

        val notificationId = notificationIdCounter.incrementAndGet()
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_STOCK_SYMBOL, "TRENT")
            putExtra(EXTRA_STOCK_EXCHANGE, "NSE")
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val title = "🚀 [NSE] TRENT: SMA 44 Bullish Bounce Triggered!"
        val message = "TRENT just tested rising 44 SMA at ₹7,240 and formed a Bullish Hammer! CMP: ₹7,315 (+3.8%). SL: ₹7,160 | Tgt 1: ₹7,620. Volume 1.8x avg."

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setColor(Color.parseColor("#00C853"))
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        try {
            NotificationManagerCompat.from(context).notify(notificationId, builder.build())
        } catch (e: SecurityException) {
            return -1
        }

        return notificationId
    }
}
