package com.example.ui.screens

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
import androidx.compose.material.icons.filled.AutoGraph
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Rule
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.BearishRed
import com.example.ui.theme.BullishGreen
import com.example.ui.theme.Sma44Gold

@Composable
fun StrategyGuideScreen(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .padding(bottom = 80.dp)
    ) {
        // Hero Header
        Column(modifier = Modifier.padding(bottom = 16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(Sma44Gold)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "44 SMA Masterclass",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Text(
                text = "The proven swing trading system for Indian stock markets (NSE & BSE)",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Introduction Card
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 14.dp)
                .border(1.dp, Sma44Gold.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Lightbulb,
                        contentDescription = null,
                        tint = Sma44Gold,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Why the 44 SMA in Indian Markets?",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "The 44-period Simple Moving Average captures approximately two full trading months (22 trading days per month) of institutional price memory. In Indian equities, leading institutional desks, domestic mutual funds, and HNIs frequently use 44 SMA as dynamic support to accumulate trending stocks.",
                    fontSize = 12.sp,
                    lineHeight = 18.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Setup 1: Rising 44 SMA Bounce
        GuideSection(
            title = "1. Rising 44 SMA Bullish Bounce (Primary Swing Setup)",
            badge = "🟢 High Probability",
            badgeColor = BullishGreen,
            rules = listOf(
                "Condition 1 (Slope): The 44 SMA line MUST have a clear upward rising slope (SMA today > SMA 5 bars ago).",
                "Condition 2 (Pullback): Stock must have experienced an uptrend, followed by an orderly pullback testing the 44 SMA zone (within 1% range).",
                "Condition 3 (Reversal Trigger): Look for a bullish reversal candle (Hammer, Bullish Engulfing, or strong green closing candle above SMA 44).",
                "Entry: Buy above the high of the reversal trigger candle.",
                "Stop Loss (SL): Place strictly below the low of the trigger candle or 1% below the 44 SMA line.",
                "Target: Aim for 1:2 Minimum Risk-to-Reward ratio, trailing to 1:3 on strong volume."
            )
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Setup 2: 44 SMA Breakout
        GuideSection(
            title = "2. 44 SMA Bullish Breakout",
            badge = "🚀 Momentum Setup",
            badgeColor = Sma44Gold,
            rules = listOf(
                "Stock consolidates below or tight against the 44 SMA line.",
                "A decisive expansion candle closes above the 44 SMA with volume > 1.5x of 20-day average.",
                "Confirms institutional accumulation and early-stage momentum shift.",
                "SL is placed below the breakout candle's low."
            )
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Setup 3: Falling 44 SMA Rejection
        GuideSection(
            title = "3. Falling 44 SMA Bearish Rejection (Exit / Short)",
            badge = "🔴 Defensive / Short",
            badgeColor = BearishRed,
            rules = listOf(
                "Condition 1 (Slope): The 44 SMA line is pointing downwards in a steady descent.",
                "Condition 2: Counter-trend dead cat bounce rises to test the falling 44 SMA resistance.",
                "Condition 3: Rejection printed via Shooting Star / Inverted Hammer / Bearish Engulfing.",
                "Action: Immediate exit on long positions or short initiation with SL above falling 44 SMA."
            )
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Golden Rules for Indian Markets
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = null,
                        tint = BullishGreen,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Golden Execution Rules for NSE & BSE",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                ExecutionRule(
                    number = "1",
                    title = "Respect the 9:15 - 9:30 AM Volatility Filter",
                    description = "Do not take hasty breakout trades in the first 15 minutes of Indian market open. Let the opening price discovery settle."
                )

                ExecutionRule(
                    number = "2",
                    title = "Align with NIFTY 50 & Sector Indices",
                    description = "Take bullish 44 SMA bounces in stocks whose sectoral index (e.g. Nifty Bank, Nifty IT) is also holding above its 44 SMA."
                )

                ExecutionRule(
                    number = "3",
                    title = "Strict 1-2% Position Risk Sizing",
                    description = "Never risk more than 1% to 2% of total trading capital on any single 44 SMA setup."
                )
            }
        }
    }
}

@Composable
fun GuideSection(
    title: String,
    badge: String,
    badgeColor: Color,
    rules: List<String>
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = badge,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = badgeColor,
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(badgeColor.copy(alpha = 0.15f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            rules.forEach { rule ->
                Row(
                    modifier = Modifier.padding(vertical = 3.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = Sma44Gold,
                        modifier = Modifier
                            .size(14.dp)
                            .padding(top = 2.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = rule,
                        fontSize = 12.sp,
                        lineHeight = 17.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun ExecutionRule(number: String, title: String, description: String) {
    Row(
        modifier = Modifier.padding(vertical = 6.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(20.dp)
                .clip(CircleShape)
                .background(Sma44Gold.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Text(text = number, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Sma44Gold)
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(text = title, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = description, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
