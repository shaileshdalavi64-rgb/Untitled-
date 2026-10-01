package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Candle
import com.example.model.Stock
import com.example.ui.theme.BearishRed
import com.example.ui.theme.BullishGreen
import com.example.ui.theme.Sma44Gold
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

@Composable
fun Sma44CandleChart(
    stock: Stock,
    modifier: Modifier = Modifier
) {
    var isCandleMode by remember { mutableStateOf(true) }
    var showSma44 by remember { mutableStateOf(true) }
    var selectedCandleIndex by remember { mutableIntStateOf(-1) }

    val candles = stock.candles
    if (candles.isEmpty()) return

    val activeCandle = if (selectedCandleIndex in candles.indices) {
        candles[selectedCandleIndex]
    } else {
        candles.last()
    }

    Column(modifier = modifier.fillMaxWidth()) {
        // Chart Header Controls: Chart Type, SMA toggle, and Active Candle Tooltip
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left: Indicator pills
            Row(verticalAlignment = Alignment.CenterVertically) {
                // SMA 44 Legend toggle
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (showSma44) Sma44Gold.copy(alpha = 0.16f) else MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .testTag("toggle_sma_button")
                ) {
                    Row(
                        modifier = Modifier
                            .clickable { showSma44 = !showSma44 }
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(if (showSma44) Sma44Gold else MaterialTheme.colorScheme.onSurfaceVariant)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "SMA 44",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (showSma44) Sma44Gold else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Chart mode toggle (Candle vs Line)
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.clip(RoundedCornerShape(12.dp))
                ) {
                    Row(
                        modifier = Modifier
                            .clickable { isCandleMode = !isCandleMode }
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isCandleMode) Icons.Default.BarChart else Icons.Default.ShowChart,
                            contentDescription = "Chart Type",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isCandleMode) "Candles" else "Line",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Right: Selected candle date
            Text(
                text = activeCandle.dateLabel,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Active Candle Details Strip
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                .padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            PriceParam(label = "O", value = activeCandle.open)
            PriceParam(label = "H", value = activeCandle.high)
            PriceParam(label = "L", value = activeCandle.low)
            PriceParam(
                label = "C",
                value = activeCandle.close,
                color = if (activeCandle.close >= activeCandle.open) BullishGreen else BearishRed
            )
            activeCandle.sma44?.let { sma ->
                Column(horizontalAlignment = Alignment.End) {
                    Text(text = "44 SMA", fontSize = 9.sp, color = Sma44Gold)
                    Text(
                        text = "₹%,.1f".format(sma),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = Sma44Gold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Main Chart Canvas
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp)
                .padding(horizontal = 16.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surface)
                .testTag("sma44_interactive_chart")
        ) {
            val gridColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
            val bullColor = BullishGreen
            val bearColor = BearishRed
            val goldColor = Sma44Gold

            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(candles) {
                        detectDragGestures(
                            onDragEnd = { selectedCandleIndex = -1 },
                            onDragCancel = { selectedCandleIndex = -1 },
                            onDrag = { change, _ ->
                                change.consume()
                                val x = change.position.x
                                val candleWidth = size.width / candles.size
                                val index = (x / candleWidth).toInt().coerceIn(0, candles.size - 1)
                                selectedCandleIndex = index
                            }
                        )
                    }
                    .pointerInput(candles) {
                        detectTapGestures(
                            onTap = { offset ->
                                val candleWidth = size.width / candles.size
                                val index = (offset.x / candleWidth).toInt().coerceIn(0, candles.size - 1)
                                selectedCandleIndex = index
                            }
                        )
                    }
            ) {
                val canvasWidth = size.width
                val canvasHeight = size.height
                val priceAreaHeight = canvasHeight * 0.78f
                val volumeAreaHeight = canvasHeight * 0.22f

                // Find global min & max prices
                var minPrice = Float.MAX_VALUE
                var maxPrice = Float.MIN_VALUE
                var maxVolume = 1L

                candles.forEach { c ->
                    if (c.low < minPrice) minPrice = c.low
                    if (c.high > maxPrice) maxPrice = c.high
                    c.sma44?.let { sma ->
                        if (sma < minPrice) minPrice = sma
                        if (sma > maxPrice) maxPrice = sma
                    }
                    if (c.volume > maxVolume) maxVolume = c.volume
                }

                // Add 4% padding to price bounds
                val priceSpan = max(maxPrice - minPrice, 1f)
                val paddedMin = minPrice - (priceSpan * 0.04f)
                val paddedMax = maxPrice + (priceSpan * 0.04f)
                val paddedSpan = paddedMax - paddedMin

                fun priceToY(price: Float): Float {
                    val ratio = (price - paddedMin) / paddedSpan
                    return priceAreaHeight - (ratio * priceAreaHeight)
                }

                // Draw Horizontal Price Grid Lines & Labels
                val gridSteps = 4
                for (i in 0..gridSteps) {
                    val y = (priceAreaHeight / gridSteps) * i
                    drawLine(
                        color = gridColor,
                        start = Offset(0f, y),
                        end = Offset(canvasWidth, y),
                        strokeWidth = 1f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f))
                    )
                }

                // Target & Stop Loss Levels
                if (stock.stopLoss > 0) {
                    val slY = priceToY(stock.stopLoss)
                    if (slY in 0f..priceAreaHeight) {
                        drawLine(
                            color = bearColor.copy(alpha = 0.7f),
                            start = Offset(0f, slY),
                            end = Offset(canvasWidth, slY),
                            strokeWidth = 1.5f,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 6f))
                        )
                    }
                }
                if (stock.target1 > 0) {
                    val t1Y = priceToY(stock.target1)
                    if (t1Y in 0f..priceAreaHeight) {
                        drawLine(
                            color = bullColor.copy(alpha = 0.7f),
                            start = Offset(0f, t1Y),
                            end = Offset(canvasWidth, t1Y),
                            strokeWidth = 1.5f,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 6f))
                        )
                    }
                }

                val count = candles.size
                val stepX = canvasWidth / count
                val candleBarWidth = max(stepX * 0.7f, 2f)

                // 1. Draw Volume Bars at the bottom
                candles.forEachIndexed { i, c ->
                    val x = (i + 0.5f) * stepX
                    val volHeight = (c.volume.toFloat() / maxVolume) * (volumeAreaHeight - 4f)
                    val topY = canvasHeight - volHeight
                    val isGreen = c.close >= c.open
                    drawRect(
                        color = (if (isGreen) bullColor else bearColor).copy(alpha = 0.35f),
                        topLeft = Offset(x - candleBarWidth / 2f, topY),
                        size = Size(candleBarWidth, volHeight)
                    )
                }

                // 2. Draw Price (Candlesticks or Line)
                if (isCandleMode) {
                    candles.forEachIndexed { i, c ->
                        val x = (i + 0.5f) * stepX
                        val isGreen = c.close >= c.open
                        val barColor = if (isGreen) bullColor else bearColor

                        val highY = priceToY(c.high)
                        val lowY = priceToY(c.low)
                        val openY = priceToY(c.open)
                        val closeY = priceToY(c.close)

                        // Wick
                        drawLine(
                            color = barColor,
                            start = Offset(x, highY),
                            end = Offset(x, lowY),
                            strokeWidth = 1.5f
                        )

                        // Body
                        val bodyTop = min(openY, closeY)
                        val bodyHeight = max(abs(closeY - openY), 2f)
                        drawRect(
                            color = barColor,
                            topLeft = Offset(x - candleBarWidth / 2f, bodyTop),
                            size = Size(candleBarWidth, bodyHeight)
                        )
                    }
                } else {
                    // Line chart
                    val linePath = Path()
                    candles.forEachIndexed { i, c ->
                        val x = (i + 0.5f) * stepX
                        val y = priceToY(c.close)
                        if (i == 0) linePath.moveTo(x, y) else linePath.lineTo(x, y)
                    }
                    drawPath(
                        path = linePath,
                        color = if (stock.change >= 0) bullColor else bearColor,
                        style = Stroke(width = 2.5f, cap = StrokeCap.Round)
                    )
                }

                // 3. Draw SMA 44 Curve
                if (showSma44) {
                    val smaPath = Path()
                    var hasStarted = false

                    candles.forEachIndexed { i, c ->
                        c.sma44?.let { sma ->
                            val x = (i + 0.5f) * stepX
                            val y = priceToY(sma)
                            if (!hasStarted) {
                                smaPath.moveTo(x, y)
                                hasStarted = true
                            } else {
                                smaPath.lineTo(x, y)
                            }
                        }
                    }

                    if (hasStarted) {
                        // Outer glowing line
                        drawPath(
                            path = smaPath,
                            color = goldColor.copy(alpha = 0.35f),
                            style = Stroke(width = 6f, cap = StrokeCap.Round)
                        )
                        // Core golden line
                        drawPath(
                            path = smaPath,
                            color = goldColor,
                            style = Stroke(width = 2.5f, cap = StrokeCap.Round)
                        )
                    }
                }

                // 4. Draw Crosshair when scrubbing
                if (selectedCandleIndex in 0 until count) {
                    val crossX = (selectedCandleIndex + 0.5f) * stepX
                    val candleY = priceToY(candles[selectedCandleIndex].close)

                    // Vertical dashed crosshair
                    drawLine(
                        color = Color.White.copy(alpha = 0.6f),
                        start = Offset(crossX, 0f),
                        end = Offset(crossX, canvasHeight),
                        strokeWidth = 1f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f))
                    )

                    // Circle indicator on selected candle close
                    drawCircle(
                        color = Color.White,
                        radius = 4f,
                        center = Offset(crossX, candleY)
                    )
                }
            }
        }
    }
}

@Composable
private fun PriceParam(label: String, value: Float, color: Color = MaterialTheme.colorScheme.onSurface) {
    Column {
        Text(text = label, fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(
            text = "₹%,.1f".format(value),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            color = color
        )
    }
}
