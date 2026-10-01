package com.example.model

enum class Exchange {
    NSE, BSE
}

enum class SmaSlope {
    RISING, FALLING, FLAT
}

enum class SetupType(
    val title: String,
    val isBullish: Boolean,
    val badgeText: String,
    val description: String
) {
    BULLISH_BOUNCE(
        title = "SMA 44 Bounce",
        isBullish = true,
        badgeText = "🟢 44 Bounce",
        description = "Price pulled back to rising 44 SMA and formed a bullish reversal candle."
    ),
    BULLISH_BREAKOUT(
        title = "SMA 44 Breakout",
        isBullish = true,
        badgeText = "🚀 44 Breakout",
        description = "Price sliced above 44 SMA with elevated volume and momentum."
    ),
    BEARISH_REJECTION(
        title = "SMA 44 Rejection",
        isBullish = false,
        badgeText = "🔴 44 Rejection",
        description = "Price pulled back up into falling 44 SMA resistance and faced sharp selling."
    ),
    BEARISH_BREAKDOWN(
        title = "SMA 44 Breakdown",
        isBullish = false,
        badgeText = "💥 44 Breakdown",
        description = "Price cracked below falling 44 SMA with selling pressure."
    ),
    RISING_TREND(
        title = "Riding Rising 44",
        isBullish = true,
        badgeText = "📈 Trend Ride",
        description = "Strong sustained uptrend riding above the ascending 44 SMA."
    ),
    FALLING_TREND(
        title = "Falling Below 44",
        isBullish = false,
        badgeText = "📉 Downtrend",
        description = "Sustained weakness riding beneath the descending 44 SMA."
    ),
    CONSOLIDATING(
        title = "Near 44 SMA",
        isBullish = true,
        badgeText = "⚖️ Hovering 44",
        description = "Consolidating near 44 SMA awaiting directional trigger."
    )
}

enum class Timeframe(val label: String, val shortName: String) {
    M15("15 Min (Intraday)", "15m"),
    H1("1 Hour (Swing)", "1h"),
    D1("Daily (Primary)", "1D"),
    W1("Weekly (Positional)", "1W")
}

data class Candle(
    val timestamp: Long,
    val dateLabel: String,
    val open: Float,
    val high: Float,
    val low: Float,
    val close: Float,
    val volume: Long,
    val sma44: Float? = null
)

data class Stock(
    val symbol: String,
    val exchange: Exchange,
    val companyName: String,
    val sector: String,
    val currentPrice: Float,
    val change: Float,
    val changePercent: Float,
    val volume: Long,
    val dayHigh: Float,
    val dayLow: Float,
    val high52W: Float,
    val low52W: Float,
    val sma44: Float,
    val smaSlope: SmaSlope,
    val slopeRatePercent: Float, // Rate of SMA ascent/descent over past 5 bars
    val distanceToSmaPercent: Float, // ((Price - SMA44) / SMA44) * 100
    val setupType: SetupType,
    val setupConfidence: Int, // 1-100
    val entryPrice: Float,
    val stopLoss: Float,
    val target1: Float, // 1:2 R:R
    val target2: Float, // 1:3 R:R
    val candles: List<Candle>,
    val isWatched: Boolean = false,
    val hasActiveAlert: Boolean = false
) {
    val fullSymbol: String get() = "$symbol.${exchange.name}"
}

data class MarketIndex(
    val name: String,
    val exchange: Exchange,
    val value: Float,
    val change: Float,
    val changePercent: Float
)

enum class ExchangeFilter {
    ALL, NSE, BSE
}

enum class SignalFilter(val label: String) {
    ALL("All Signals"),
    BULLISH_BOUNCE("Bullish Bounce (Rising 44)"),
    BULLISH_BREAKOUT("Bullish Breakout (Above 44)"),
    BEARISH_REJECTION("Bearish Rejection (Falling 44)"),
    BEARISH_BREAKDOWN("Bearish Breakdown (Below 44)"),
    RISING_SLOPE("Rising 44 SMA Only"),
    FALLING_SLOPE("Falling 44 SMA Only")
}

enum class SortOption(val label: String) {
    DISTANCE_TO_SMA("Closest to 44 SMA"),
    CHANGE_DESC("Top Gainers %"),
    CHANGE_ASC("Top Losers %"),
    CONFIDENCE_DESC("Highest Confidence"),
    VOLUME_DESC("Highest Volume")
}

data class ScannerFilter(
    val exchange: ExchangeFilter = ExchangeFilter.ALL,
    val signal: SignalFilter = SignalFilter.ALL,
    val timeframe: Timeframe = Timeframe.D1,
    val sector: String = "All",
    val sortOption: SortOption = SortOption.DISTANCE_TO_SMA,
    val searchQuery: String = ""
)

data class MarketStatus(
    val isOpen: Boolean,
    val statusText: String,
    val istTime: String,
    val nextSessionCountdown: String
)
