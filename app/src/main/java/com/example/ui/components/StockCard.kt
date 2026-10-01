package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Exchange
import com.example.model.SetupType
import com.example.model.SmaSlope
import com.example.model.Stock
import com.example.ui.theme.BearishRed
import com.example.ui.theme.BseBlue
import com.example.ui.theme.BullishGreen
import com.example.ui.theme.NseTeal
import com.example.ui.theme.Sma44Gold
import kotlin.math.abs

@Composable
fun StockCard(
    stock: Stock,
    onClick: () -> Unit,
    onToggleWatchlist: () -> Unit,
    onQuickAlert: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isPositive = stock.change >= 0
    val isRisingSma = stock.smaSlope == SmaSlope.RISING
    val isFallingSma = stock.smaSlope == SmaSlope.FALLING

    val cardBorderColor by animateColorAsState(
        targetValue = when (stock.setupType) {
            SetupType.BULLISH_BOUNCE -> BullishGreen.copy(alpha = 0.5f)
            SetupType.BULLISH_BREAKOUT -> Sma44Gold.copy(alpha = 0.6f)
            SetupType.BEARISH_REJECTION -> BearishRed.copy(alpha = 0.5f)
            SetupType.BEARISH_BREAKDOWN -> BearishRed.copy(alpha = 0.6f)
            else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
        },
        label = "borderColor"
    )

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, cardBorderColor, RoundedCornerShape(14.dp))
            .clip(RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .testTag("stock_card_${stock.symbol}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Row 1: Symbol, Exchange badge, Sector & Action Icons
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    // Exchange Badge
                    Text(
                        text = stock.exchange.name,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (stock.exchange == Exchange.NSE) NseTeal else BseBlue,
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(
                                (if (stock.exchange == Exchange.NSE) NseTeal else BseBlue).copy(alpha = 0.16f)
                            )
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = stock.symbol,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.width(6.dp))

                    Text(
                        text = "• ${stock.sector}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Action Icons (Watchlist star & Alert bell)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onQuickAlert,
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("quick_alert_button_${stock.symbol}")
                    ) {
                        Icon(
                            imageVector = if (stock.hasActiveAlert) Icons.Default.NotificationsActive else Icons.Default.Notifications,
                            contentDescription = "Set Alert",
                            tint = if (stock.hasActiveAlert) Sma44Gold else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    IconButton(
                        onClick = onToggleWatchlist,
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("watchlist_button_${stock.symbol}")
                    ) {
                        Icon(
                            imageVector = if (stock.isWatched) Icons.Default.Star else Icons.Default.StarBorder,
                            contentDescription = "Watchlist",
                            tint = if (stock.isWatched) Sma44Gold else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Text(
                text = stock.companyName,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Row 2: Live Price & Day Change
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Price in INR
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = "₹",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "%,.2f".format(stock.currentPrice),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                // Change % badge
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(
                            (if (isPositive) BullishGreen else BearishRed).copy(alpha = 0.15f)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = if (isPositive) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                        contentDescription = null,
                        tint = if (isPositive) BullishGreen else BearishRed,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = "${if (isPositive) "+" else ""}${stock.changePercent}%",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isPositive) BullishGreen else BearishRed
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Row 3: SMA 44 Analytics Pill
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // 44 SMA Price
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(Sma44Gold)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "SMA 44:",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "₹%,.2f".format(stock.sma44),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = Sma44Gold
                    )
                }

                // 44 SMA Slope
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = when (stock.smaSlope) {
                            SmaSlope.RISING -> "↗ Slope: Rising"
                            SmaSlope.FALLING -> "↘ Slope: Falling"
                            SmaSlope.FLAT -> "→ Slope: Flat"
                        },
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = when (stock.smaSlope) {
                            SmaSlope.RISING -> BullishGreen
                            SmaSlope.FALLING -> BearishRed
                            SmaSlope.FLAT -> MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }

                // Distance to 44 SMA
                val distSign = if (stock.distanceToSmaPercent > 0) "+" else ""
                Text(
                    text = "$distSign${stock.distanceToSmaPercent}% away",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (abs(stock.distanceToSmaPercent) <= 1.2f) Sma44Gold else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Row 4: Setup Badge & Confidence Rating
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Setup type badge
                val setupBg = when (stock.setupType) {
                    SetupType.BULLISH_BOUNCE -> BullishGreen.copy(alpha = 0.18f)
                    SetupType.BULLISH_BREAKOUT -> Sma44Gold.copy(alpha = 0.2f)
                    SetupType.BEARISH_REJECTION -> BearishRed.copy(alpha = 0.18f)
                    SetupType.BEARISH_BREAKDOWN -> BearishRed.copy(alpha = 0.22f)
                    SetupType.RISING_TREND -> BullishGreen.copy(alpha = 0.12f)
                    SetupType.FALLING_TREND -> BearishRed.copy(alpha = 0.12f)
                    SetupType.CONSOLIDATING -> MaterialTheme.colorScheme.surfaceVariant
                }
                val setupColor = when (stock.setupType) {
                    SetupType.BULLISH_BOUNCE, SetupType.RISING_TREND -> BullishGreen
                    SetupType.BULLISH_BREAKOUT -> Sma44Gold
                    SetupType.BEARISH_REJECTION, SetupType.BEARISH_BREAKDOWN, SetupType.FALLING_TREND -> BearishRed
                    SetupType.CONSOLIDATING -> MaterialTheme.colorScheme.onSurfaceVariant
                }

                Text(
                    text = stock.setupType.badgeText,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = setupColor,
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(setupBg)
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                )

                // Entry / SL / Tgt summary or Confidence
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Confidence:",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${stock.setupConfidence}%",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (stock.setupConfidence >= 85) BullishGreen else Sma44Gold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "SL: ₹${stock.stopLoss.toInt()}",
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        color = BearishRed.copy(alpha = 0.85f)
                    )
                }
            }
        }
    }
}
