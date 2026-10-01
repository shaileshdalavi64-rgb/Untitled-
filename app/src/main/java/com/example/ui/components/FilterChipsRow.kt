package com.example.ui.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ExchangeFilter
import com.example.model.ScannerFilter
import com.example.model.SignalFilter
import com.example.model.SortOption
import com.example.model.Timeframe
import com.example.ui.theme.BearishRed
import com.example.ui.theme.BseBlue
import com.example.ui.theme.BullishGreen
import com.example.ui.theme.NseTeal
import com.example.ui.theme.Sma44Gold

@Composable
fun FilterChipsRow(
    filter: ScannerFilter,
    onExchangeChange: (ExchangeFilter) -> Unit,
    onSignalChange: (SignalFilter) -> Unit,
    onTimeframeChange: (Timeframe) -> Unit,
    onSortChange: (SortOption) -> Unit,
    modifier: Modifier = Modifier
) {
    var showSortMenu by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxWidth()) {
        // Row 1: Exchange Tabs & Timeframes & Sort
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Exchange Segment
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                ExchangeFilter.entries.forEach { ex ->
                    val selected = filter.exchange == ex
                    val chipColor = when (ex) {
                        ExchangeFilter.ALL -> Sma44Gold
                        ExchangeFilter.NSE -> NseTeal
                        ExchangeFilter.BSE -> BseBlue
                    }

                    FilterChip(
                        selected = selected,
                        onClick = { onExchangeChange(ex) },
                        label = {
                            Text(
                                text = when (ex) {
                                    ExchangeFilter.ALL -> "All Markets"
                                    ExchangeFilter.NSE -> "NSE"
                                    ExchangeFilter.BSE -> "BSE"
                                },
                                fontSize = 11.sp,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = chipColor.copy(alpha = 0.2f),
                            selectedLabelColor = chipColor
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("filter_exchange_${ex.name}")
                    )
                }
            }

            // Sort button
            Row(verticalAlignment = Alignment.CenterVertically) {
                SuggestionChip(
                    onClick = { showSortMenu = true },
                    label = {
                        Text(
                            text = filter.sortOption.label,
                            fontSize = 10.sp,
                            maxLines = 1
                        )
                    },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Sort,
                            contentDescription = "Sort",
                            modifier = Modifier.size(12.dp)
                        )
                    },
                    shape = RoundedCornerShape(8.dp),
                    colors = SuggestionChipDefaults.suggestionChipColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    modifier = Modifier.testTag("sort_dropdown_button")
                )

                DropdownMenu(
                    expanded = showSortMenu,
                    onDismissRequest = { showSortMenu = false }
                ) {
                    SortOption.entries.forEach { option ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = option.label,
                                    fontSize = 12.sp,
                                    fontWeight = if (filter.sortOption == option) FontWeight.Bold else FontWeight.Normal,
                                    color = if (filter.sortOption == option) Sma44Gold else MaterialTheme.colorScheme.onSurface
                                )
                            },
                            onClick = {
                                onSortChange(option)
                                showSortMenu = false
                            },
                            modifier = Modifier.testTag("sort_option_${option.name}")
                        )
                    }
                }
            }
        }

        // Row 2: Signal Strategy Filters (Scrollable)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SignalFilter.entries.forEach { sig ->
                val selected = filter.signal == sig
                val activeColor = when (sig) {
                    SignalFilter.BULLISH_BOUNCE -> BullishGreen
                    SignalFilter.BULLISH_BREAKOUT -> Sma44Gold
                    SignalFilter.BEARISH_REJECTION, SignalFilter.BEARISH_BREAKDOWN -> BearishRed
                    SignalFilter.RISING_SLOPE -> BullishGreen
                    SignalFilter.FALLING_SLOPE -> BearishRed
                    SignalFilter.ALL -> Sma44Gold
                }

                FilterChip(
                    selected = selected,
                    onClick = { onSignalChange(sig) },
                    label = {
                        Text(
                            text = sig.label,
                            fontSize = 11.sp,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = activeColor.copy(alpha = 0.2f),
                        selectedLabelColor = activeColor
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("filter_signal_${sig.name}")
                )
            }
        }
    }
}
