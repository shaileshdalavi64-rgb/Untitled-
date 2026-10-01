package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddAlert
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Exchange
import com.example.model.SetupType
import com.example.model.SmaSlope
import com.example.model.Stock
import com.example.model.Timeframe
import com.example.ui.components.Sma44CandleChart
import com.example.ui.theme.BearishRed
import com.example.ui.theme.BseBlue
import com.example.ui.theme.BullishGreen
import com.example.ui.theme.NseTeal
import com.example.ui.theme.Sma44Gold
import com.example.viewmodel.ScannerViewModel
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StockDetailScreen(
    stock: Stock,
    viewModel: ScannerViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }

    var selectedTimeframe by remember { mutableStateOf(Timeframe.D1) }
    var showAlertDialog by remember { mutableStateOf(false) }

    val isPositive = stock.change >= 0

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = stock.symbol,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = stock.exchange.name,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (stock.exchange == Exchange.NSE) NseTeal else BseBlue,
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background((if (stock.exchange == Exchange.NSE) NseTeal else BseBlue).copy(alpha = 0.2f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("detail_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showAlertDialog = true },
                        modifier = Modifier.testTag("detail_alert_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.AddAlert,
                            contentDescription = "Set Alert",
                            tint = if (stock.hasActiveAlert) Sma44Gold else MaterialTheme.colorScheme.onSurface
                        )
                    }
                    IconButton(
                        onClick = { viewModel.toggleWatchlist(stock) },
                        modifier = Modifier.testTag("detail_watchlist_button")
                    ) {
                        Icon(
                            imageVector = if (stock.isWatched) Icons.Default.Star else Icons.Default.StarBorder,
                            contentDescription = "Watchlist",
                            tint = if (stock.isWatched) Sma44Gold else MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(bottom = 32.dp)
        ) {
            // Price & Company Info Header
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
            ) {
                Text(
                    text = stock.companyName,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = "₹",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "%,.2f".format(stock.currentPrice),
                            fontSize = 28.sp,
                            fontWeight = FontWeight.ExtraBold,
                            fontFamily = FontFamily.Monospace,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background((if (isPositive) BullishGreen else BearishRed).copy(alpha = 0.16f))
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = if (isPositive) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                            contentDescription = null,
                            tint = if (isPositive) BullishGreen else BearishRed,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${if (isPositive) "+" else ""}₹${stock.change} (${stock.changePercent}%)",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isPositive) BullishGreen else BearishRed
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Timeframe Selector Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Timeframe.entries.forEach { tf ->
                    FilterChip(
                        selected = selectedTimeframe == tf,
                        onClick = { selectedTimeframe = tf },
                        label = { Text(tf.shortName, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                        shape = RoundedCornerShape(8.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Sma44Gold.copy(alpha = 0.2f),
                            selectedLabelColor = Sma44Gold
                        ),
                        modifier = Modifier.testTag("timeframe_${tf.shortName}")
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Interactive Candlestick Chart with 44 SMA
            Sma44CandleChart(stock = stock)

            Spacer(modifier = Modifier.height(14.dp))

            // 44 SMA Diagnostic Card
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .border(1.dp, Sma44Gold.copy(alpha = 0.35f), RoundedCornerShape(14.dp))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(Sma44Gold)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "44 SMA Strategy Engine",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        // Confidence Tag
                        Text(
                            text = "${stock.setupConfidence}% Setup Score",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Sma44Gold,
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Sma44Gold.copy(alpha = 0.15f))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Metrics Grid (SMA 44 Value, Slope, Distance)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        DetailMetric(
                            label = "44 SMA Level",
                            value = "₹%,.2f".format(stock.sma44),
                            valueColor = Sma44Gold
                        )
                        DetailMetric(
                            label = "SMA Slope",
                            value = when (stock.smaSlope) {
                                SmaSlope.RISING -> "Rising (+${stock.slopeRatePercent}%)"
                                SmaSlope.FALLING -> "Falling (${stock.slopeRatePercent}%)"
                                SmaSlope.FLAT -> "Flat (${stock.slopeRatePercent}%)"
                            },
                            valueColor = when (stock.smaSlope) {
                                SmaSlope.RISING -> BullishGreen
                                SmaSlope.FALLING -> BearishRed
                                SmaSlope.FLAT -> MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                        DetailMetric(
                            label = "Distance",
                            value = "${if (stock.distanceToSmaPercent > 0) "+" else ""}${stock.distanceToSmaPercent}%",
                            valueColor = if (abs(stock.distanceToSmaPercent) <= 1.2f) BullishGreen else MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Diagnosis Box
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                            .padding(10.dp)
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = stock.setupType.badgeText,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "• ${stock.setupType.title}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = stock.setupType.description,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Trading Plan & Risk Management
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Calculated Swing Trading Plan (1:2 & 1:3 RR)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        PlanLevel(label = "Ideal Entry", price = stock.entryPrice, color = MaterialTheme.colorScheme.onSurface)
                        PlanLevel(label = "Stop Loss (SL)", price = stock.stopLoss, color = BearishRed)
                        PlanLevel(label = "Target 1 (1:2)", price = stock.target1, color = BullishGreen)
                        PlanLevel(label = "Target 2 (1:3)", price = stock.target2, color = BullishGreen)
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Action Buttons: Send Test Push Notification & Configure Alerts
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { viewModel.triggerStockNotification(stock) },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Sma44Gold),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("test_push_stock_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.NotificationsActive,
                        contentDescription = null,
                        tint = Color.Black,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Trigger Push Notification Now",
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }

                OutlinedButton(
                    onClick = { showAlertDialog = true },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("configure_custom_alert_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.AddAlert,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Set Custom 44 SMA Price Alert Rule")
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Day & 52-Week Range Sliders
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Performance Ranges",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    RangeBar(
                        label = "Day Range",
                        low = stock.dayLow,
                        current = stock.currentPrice,
                        high = stock.dayHigh
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    RangeBar(
                        label = "52-Week Range",
                        low = stock.low52W,
                        current = stock.currentPrice,
                        high = stock.high52W
                    )
                }
            }
        }
    }

    // Set Alert Rule Dialog
    if (showAlertDialog) {
        var selectedType by remember { mutableStateOf("BOUNCE_SMA") }

        AlertDialog(
            onDismissRequest = { showAlertDialog = false },
            title = {
                Text(
                    text = "Configure Push Alert for ${stock.symbol}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            },
            text = {
                Column {
                    Text(
                        text = "Select when you want to receive real-time push notifications for this stock:",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    AlertOption(
                        title = "SMA 44 Bounce Confirmed",
                        subtitle = "Notify when price pulls back to rising 44 SMA and forms a reversal",
                        selected = selectedType == "BOUNCE_SMA",
                        onSelect = { selectedType = "BOUNCE_SMA" }
                    )

                    AlertOption(
                        title = "SMA 44 Breakout",
                        subtitle = "Notify when price crosses above 44 SMA with volume",
                        selected = selectedType == "BREAKOUT_SMA",
                        onSelect = { selectedType = "BREAKOUT_SMA" }
                    )

                    AlertOption(
                        title = "Within 0.5% of SMA 44",
                        subtitle = "Notify as soon as stock approaches near 44 SMA level",
                        selected = selectedType == "PROXIMITY",
                        onSelect = { selectedType = "PROXIMITY" }
                    )

                    AlertOption(
                        title = "SMA 44 Bearish Rejection",
                        subtitle = "Notify when stock is rejected at falling 44 SMA resistance",
                        selected = selectedType == "REJECTION_SMA",
                        onSelect = { selectedType = "REJECTION_SMA" }
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.addCustomAlert(stock, selectedType)
                        showAlertDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Sma44Gold)
                ) {
                    Text("Arm Alert", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAlertDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun DetailMetric(label: String, value: String, valueColor: Color) {
    Column {
        Text(text = label, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            color = valueColor
        )
    }
}

@Composable
private fun PlanLevel(label: String, price: Float, color: Color) {
    Column {
        Text(text = label, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(
            text = "₹%,.1f".format(price),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            color = color
        )
    }
}

@Composable
private fun RangeBar(label: String, low: Float, current: Float, high: Float) {
    val progress = if (high > low) ((current - low) / (high - low)).coerceIn(0f, 1f) else 0.5f

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(
                text = "₹%,.1f".format(current),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = Sma44Gold,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )

        Spacer(modifier = Modifier.height(2.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = "L: ₹%,.1f".format(low), fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(text = "H: ₹%,.1f".format(high), fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun AlertOption(
    title: String,
    subtitle: String,
    selected: Boolean,
    onSelect: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(if (selected) Sma44Gold.copy(alpha = 0.12f) else Color.Transparent)
            .padding(vertical = 4.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = onSelect
        )
        Spacer(modifier = Modifier.width(6.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            Text(text = subtitle, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
