package com.example.data.repository

import com.example.data.database.AlertRuleEntity
import com.example.data.database.NotificationHistoryEntity
import com.example.data.database.ScannerDao
import com.example.data.database.WatchlistEntity
import com.example.data.notification.PushNotificationHelper
import com.example.model.Candle
import com.example.model.Exchange
import com.example.model.MarketIndex
import com.example.model.MarketStatus
import com.example.model.ScannerFilter
import com.example.model.SetupType
import com.example.model.SignalFilter
import com.example.model.SmaSlope
import com.example.model.SortOption
import com.example.model.Stock
import com.example.model.Timeframe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.random.Random

class StockRepository(
    private val scannerDao: ScannerDao,
    private val notificationHelper: PushNotificationHelper
) {

    private val _stocksState = MutableStateFlow<List<Stock>>(emptyList())
    val stocksState = _stocksState.asStateFlow()

    private val _marketIndices = MutableStateFlow<List<MarketIndex>>(emptyList())
    val marketIndices = _marketIndices.asStateFlow()

    val watchlist: Flow<List<WatchlistEntity>> = scannerDao.getAllWatchlist()
    val alertRules: Flow<List<AlertRuleEntity>> = scannerDao.getAllAlertRules()
    val notificationHistory: Flow<List<NotificationHistoryEntity>> = scannerDao.getAllNotifications()
    val unreadNotificationsCount: Flow<Int> = scannerDao.getUnreadCount()

    init {
        initializeStockUniverse()
        refreshMarketIndices()
    }

    suspend fun refreshStockData() {
        withContext(Dispatchers.Default) {
            val currentList = _stocksState.value
            val updated = currentList.map { stock ->
                val changeJitter = (Random.nextFloat() - 0.48f) * (stock.currentPrice * 0.003f)
                val newPrice = ((stock.currentPrice + changeJitter) * 100).roundToInt() / 100f
                val newChange = ((newPrice - (stock.currentPrice - stock.change)) * 100).roundToInt() / 100f
                val baseRef = stock.currentPrice - stock.change
                val newChangePercent = if (baseRef > 0) ((newChange / baseRef) * 10000).roundToInt() / 100f else 0f

                val distanceToSma = (((newPrice - stock.sma44) / stock.sma44) * 10000).roundToInt() / 100f

                stock.copy(
                    currentPrice = newPrice,
                    change = newChange,
                    changePercent = newChangePercent,
                    distanceToSmaPercent = distanceToSma,
                    dayHigh = maxOf(stock.dayHigh, newPrice),
                    dayLow = minOf(stock.dayLow, newPrice)
                )
            }
            _stocksState.value = updated
            checkAlertRulesAndNotify(updated)
            refreshMarketIndices()
        }
    }

    private suspend fun checkAlertRulesAndNotify(stocks: List<Stock>) {
        val activeRules = scannerDao.getActiveRulesList()
        val now = System.currentTimeMillis()

        for (rule in activeRules) {
            // cooldown of 5 minutes between triggers for same rule
            if (now - rule.lastTriggeredAt < 300_000) continue

            val matchingStock = stocks.find { it.symbol.equals(rule.symbol, ignoreCase = true) } ?: continue

            var triggered = false
            var alertReason = ""

            when (rule.alertType) {
                "BOUNCE_SMA" -> {
                    if (matchingStock.setupType == SetupType.BULLISH_BOUNCE) {
                        triggered = true
                        alertReason = "44 SMA Bullish Bounce confirmed at ₹${matchingStock.currentPrice}!"
                    }
                }
                "BREAKOUT_SMA" -> {
                    if (matchingStock.setupType == SetupType.BULLISH_BREAKOUT) {
                        triggered = true
                        alertReason = "Strong Breakout above 44 SMA at ₹${matchingStock.currentPrice}!"
                    }
                }
                "REJECTION_SMA" -> {
                    if (matchingStock.setupType == SetupType.BEARISH_REJECTION) {
                        triggered = true
                        alertReason = "Bearish Rejection at Falling 44 SMA at ₹${matchingStock.currentPrice}!"
                    }
                }
                else -> { // PROXIMITY
                    if (abs(matchingStock.distanceToSmaPercent) <= rule.tolerancePercent) {
                        triggered = true
                        alertReason = "Price is within ${rule.tolerancePercent}% of 44 SMA (₹${matchingStock.sma44})"
                    }
                }
            }

            if (triggered) {
                // Send push notification
                notificationHelper.sendStockAlertNotification(matchingStock, alertReason)

                // Log into Room database
                scannerDao.insertNotification(
                    NotificationHistoryEntity(
                        title = "[${matchingStock.exchange}] ${matchingStock.symbol}: 44 SMA Alert",
                        message = alertReason,
                        symbol = matchingStock.symbol,
                        exchange = matchingStock.exchange.name,
                        setupType = matchingStock.setupType.name,
                        price = matchingStock.currentPrice,
                        sma44 = matchingStock.sma44,
                        timestamp = now,
                        isRead = false
                    )
                )

                scannerDao.updateAlertRule(rule.copy(lastTriggeredAt = now))
            }
        }
    }

    suspend fun toggleWatchlist(symbol: String, exchange: Exchange, companyName: String) {
        val isWatched = scannerDao.isWatched(symbol)
        if (isWatched) {
            scannerDao.removeFromWatchlist(symbol)
        } else {
            scannerDao.addToWatchlist(
                WatchlistEntity(
                    symbol = symbol,
                    exchange = exchange.name,
                    companyName = companyName
                )
            )
        }
        updateWatchedFlags()
    }

    suspend fun addAlertRule(symbol: String, exchange: Exchange, alertType: String, tolerancePercent: Float = 0.5f) {
        scannerDao.insertAlertRule(
            AlertRuleEntity(
                symbol = symbol,
                exchange = exchange.name,
                alertType = alertType,
                tolerancePercent = tolerancePercent,
                isEnabled = true
            )
        )
    }

    suspend fun deleteAlertRule(ruleId: Long) {
        scannerDao.deleteAlertRule(ruleId)
    }

    suspend fun markNotificationRead(id: Long) {
        scannerDao.markNotificationRead(id)
    }

    suspend fun markAllNotificationsRead() {
        scannerDao.markAllNotificationsRead()
    }

    suspend fun clearAllNotifications() {
        scannerDao.clearAllNotifications()
    }

    suspend fun triggerTestPushNotification(): Int {
        val res = notificationHelper.sendTestPushNotification()
        scannerDao.insertNotification(
            NotificationHistoryEntity(
                title = "🚀 [NSE] TRENT: SMA 44 Bullish Bounce Triggered!",
                message = "TRENT just tested rising 44 SMA at ₹7,240 and formed a Bullish Hammer! CMP: ₹7,315 (+3.8%). SL: ₹7,160 | Tgt 1: ₹7,620.",
                symbol = "TRENT",
                exchange = "NSE",
                setupType = SetupType.BULLISH_BOUNCE.name,
                price = 7315.0f,
                sma44 = 7240.0f,
                timestamp = System.currentTimeMillis(),
                isRead = false
            )
        )
        return res
    }

    fun triggerStockPushNotification(stock: Stock): Int {
        val res = notificationHelper.sendStockAlertNotification(stock)
        return res
    }

    private suspend fun updateWatchedFlags() {
        val watchedSymbols = withContext(Dispatchers.IO) {
            // grab watched list
            mutableSetOf<String>()
        }
        // reactive flows will also bind this in the ViewModel
    }

    fun getFilteredStocks(
        allStocks: List<Stock>,
        watchedList: List<WatchlistEntity>,
        activeRules: List<AlertRuleEntity>,
        filter: ScannerFilter
    ): List<Stock> {
        val watchedSet = watchedList.map { it.symbol }.toSet()
        val rulesSet = activeRules.map { it.symbol }.toSet()

        return allStocks.asSequence()
            .map { stock ->
                stock.copy(
                    isWatched = watchedSet.contains(stock.symbol),
                    hasActiveAlert = rulesSet.contains(stock.symbol)
                )
            }
            .filter { stock ->
                // Exchange Filter
                when (filter.exchange) {
                    com.example.model.ExchangeFilter.ALL -> true
                    com.example.model.ExchangeFilter.NSE -> stock.exchange == Exchange.NSE
                    com.example.model.ExchangeFilter.BSE -> stock.exchange == Exchange.BSE
                }
            }
            .filter { stock ->
                // Signal Filter
                when (filter.signal) {
                    SignalFilter.ALL -> true
                    SignalFilter.BULLISH_BOUNCE -> stock.setupType == SetupType.BULLISH_BOUNCE
                    SignalFilter.BULLISH_BREAKOUT -> stock.setupType == SetupType.BULLISH_BREAKOUT
                    SignalFilter.BEARISH_REJECTION -> stock.setupType == SetupType.BEARISH_REJECTION
                    SignalFilter.BEARISH_BREAKDOWN -> stock.setupType == SetupType.BEARISH_BREAKDOWN
                    SignalFilter.RISING_SLOPE -> stock.smaSlope == SmaSlope.RISING
                    SignalFilter.FALLING_SLOPE -> stock.smaSlope == SmaSlope.FALLING
                }
            }
            .filter { stock ->
                // Sector filter
                if (filter.sector == "All") true else stock.sector.equals(filter.sector, ignoreCase = true)
            }
            .filter { stock ->
                // Search query
                if (filter.searchQuery.isBlank()) true
                else {
                    val q = filter.searchQuery.trim().lowercase()
                    stock.symbol.lowercase().contains(q) ||
                            stock.companyName.lowercase().contains(q) ||
                            stock.sector.lowercase().contains(q)
                }
            }
            .sortedWith { a, b ->
                when (filter.sortOption) {
                    SortOption.DISTANCE_TO_SMA -> abs(a.distanceToSmaPercent).compareTo(abs(b.distanceToSmaPercent))
                    SortOption.CHANGE_DESC -> b.changePercent.compareTo(a.changePercent)
                    SortOption.CHANGE_ASC -> a.changePercent.compareTo(b.changePercent)
                    SortOption.CONFIDENCE_DESC -> b.setupConfidence.compareTo(a.setupConfidence)
                    SortOption.VOLUME_DESC -> b.volume.compareTo(a.volume)
                }
            }
            .toList()
    }

    fun getIndianMarketStatus(): MarketStatus {
        val cal = Calendar.getInstance(TimeZone.getTimeZone("Asia/Kolkata"))
        val dayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
        val hour = cal.get(Calendar.HOUR_OF_DAY)
        val minute = cal.get(Calendar.MINUTE)
        val totalMinutes = hour * 60 + minute

        val timeFormat = SimpleDateFormat("hh:mm:ss a", Locale.ENGLISH)
        timeFormat.timeZone = TimeZone.getTimeZone("Asia/Kolkata")
        val istTimeStr = timeFormat.format(cal.time) + " IST"

        val isWeekday = dayOfWeek != Calendar.SATURDAY && dayOfWeek != Calendar.SUNDAY

        // Indian Market Trading Hours (IST):
        // Pre-market: 09:00 - 09:15 (540 - 555 min)
        // Normal Trading: 09:15 - 15:30 (555 - 930 min)
        val isOpen: Boolean
        val statusText: String
        val countdown: String

        if (!isWeekday) {
            isOpen = false
            statusText = "Weekend Closed"
            countdown = "Opens Monday 09:15 AM"
        } else if (totalMinutes in 540 until 555) {
            isOpen = true
            statusText = "Pre-Market Session"
            countdown = "Normal trade in ${555 - totalMinutes}m"
        } else if (totalMinutes in 555..930) {
            isOpen = true
            statusText = "Market Live"
            val remainingMins = 930 - totalMinutes
            val hrs = remainingMins / 60
            val mins = remainingMins % 60
            countdown = "Closes in ${hrs}h ${mins}m"
        } else if (totalMinutes < 540) {
            isOpen = false
            statusText = "Market Closed"
            val untilOpen = 555 - totalMinutes
            val hrs = untilOpen / 60
            val mins = untilOpen % 60
            countdown = "Opens in ${hrs}h ${mins}m"
        } else {
            isOpen = false
            statusText = "Market Closed"
            countdown = "Opens tomorrow 09:15 AM"
        }

        return MarketStatus(
            isOpen = isOpen,
            statusText = statusText,
            istTime = istTimeStr,
            nextSessionCountdown = countdown
        )
    }

    private fun refreshMarketIndices() {
        val indices = listOf(
            MarketIndex("NIFTY 50", Exchange.NSE, 25842.10f, 168.45f, 0.66f),
            MarketIndex("BSE SENSEX", Exchange.BSE, 84520.80f, 521.15f, 0.62f),
            MarketIndex("BANK NIFTY", Exchange.NSE, 53980.50f, 310.25f, 0.58f),
            MarketIndex("NIFTY IT", Exchange.NSE, 42150.30f, -145.80f, -0.34f)
        )
        _marketIndices.value = indices
    }

    private fun initializeStockUniverse() {
        // Build authentic Indian market tickers for NSE and BSE
        val rawStocks = listOf(
            // NSE High Flyers & Bluechips
            RawStockData("TRENT", Exchange.NSE, "Trent Limited (Tata Retail)", "Consumer", 7315.50f, 268.0f, 3.81f, 1850000L, 7360f, 7080f, 7420f, 2010f, SetupType.BULLISH_BOUNCE, 92),
            RawStockData("RELIANCE", Exchange.NSE, "Reliance Industries Ltd", "Energy", 2984.70f, 42.50f, 1.44f, 4200000L, 2998f, 2930f, 3217f, 2220f, SetupType.BULLISH_BREAKOUT, 88),
            RawStockData("HAL", Exchange.NSE, "Hindustan Aeronautics Ltd", "Defense", 4860.20f, 155.0f, 3.29f, 2400000L, 4895f, 4680f, 5675f, 1920f, SetupType.BULLISH_BOUNCE, 94),
            RawStockData("HDFCBANK", Exchange.NSE, "HDFC Bank Limited", "Banking", 1682.40f, -8.20f, -0.49f, 9800000L, 1698f, 1675f, 1794f, 1363f, SetupType.CONSOLIDATING, 70),
            RawStockData("TCS", Exchange.NSE, "Tata Consultancy Services", "IT", 4275.80f, -48.20f, -1.11f, 1450000L, 4340f, 4260f, 4592f, 3313f, SetupType.BEARISH_REJECTION, 85),
            RawStockData("DIXON", Exchange.NSE, "Dixon Technologies Ltd", "Consumer", 13420.0f, 480.0f, 3.71f, 890000L, 13540f, 12900f, 14200f, 4850f, SetupType.RISING_TREND, 89),
            RawStockData("BEL", Exchange.NSE, "Bharat Electronics Ltd", "Defense", 308.65f, 9.40f, 3.14f, 8400000L, 312f, 298f, 340f, 130f, SetupType.BULLISH_BOUNCE, 91),
            RawStockData("INFY", Exchange.NSE, "Infosys Limited", "IT", 1912.30f, -24.50f, -1.26f, 5200000L, 1945f, 1905f, 1991f, 1351f, SetupType.BEARISH_BREAKDOWN, 83),
            RawStockData("TATAMOTORS", Exchange.NSE, "Tata Motors Ltd", "Auto", 985.40f, 16.80f, 1.73f, 6200000L, 992f, 965f, 1179f, 615f, SetupType.BULLISH_BREAKOUT, 86),
            RawStockData("ZOMATO", Exchange.NSE, "Zomato Limited", "Consumer", 278.40f, 8.90f, 3.30f, 14500000L, 282f, 268f, 298f, 102f, SetupType.RISING_TREND, 88),
            RawStockData("ICICIBANK", Exchange.NSE, "ICICI Bank Limited", "Banking", 1276.50f, 14.20f, 1.12f, 7100000L, 1282f, 1258f, 1332f, 910f, SetupType.BULLISH_BOUNCE, 90),
            RawStockData("SUZLON", Exchange.NSE, "Suzlon Energy Ltd", "Energy", 84.60f, 3.90f, 4.83f, 38000000L, 85.5f, 80.2f, 86f, 27f, SetupType.BULLISH_BOUNCE, 95),
            RawStockData("BHARTIARTL", Exchange.NSE, "Bharti Airtel Ltd", "Telecom", 1690.00f, 22.0f, 1.32f, 3600000L, 1705f, 1665f, 1779f, 912f, SetupType.RISING_TREND, 87),
            RawStockData("SUNPHARMA", Exchange.NSE, "Sun Pharmaceutical Inds", "Pharma", 1925.30f, 31.40f, 1.66f, 2100000L, 1935f, 1890f, 1960f, 1105f, SetupType.BULLISH_BOUNCE, 89),
            RawStockData("TATASTEEL", Exchange.NSE, "Tata Steel Ltd", "Metal", 158.70f, -3.40f, -2.10f, 11200000L, 163f, 157.5f, 184f, 118f, SetupType.BEARISH_BREAKDOWN, 82),

            // BSE Flagships & SENSEX Stocks
            RawStockData("ASIANPAINT", Exchange.BSE, "Asian Paints Ltd", "Consumer", 3120.40f, -54.0f, -1.70f, 850000L, 3180f, 3110f, 3422f, 2670f, SetupType.BEARISH_REJECTION, 87),
            RawStockData("L&T", Exchange.BSE, "Larsen & Toubro Ltd", "Infra", 3740.00f, 52.0f, 1.41f, 1200000L, 3765f, 3670f, 3919f, 2900f, SetupType.BULLISH_BOUNCE, 91),
            RawStockData("MARUTI", Exchange.BSE, "Maruti Suzuki India", "Auto", 12980.0f, 190.0f, 1.48f, 410000L, 13080f, 12750f, 13680f, 9900f, SetupType.BULLISH_BREAKOUT, 85),
            RawStockData("SBIN", Exchange.BSE, "State Bank of India", "Banking", 812.50f, 11.20f, 1.40f, 5800000L, 818f, 798f, 912f, 555f, SetupType.BULLISH_BOUNCE, 90),
            RawStockData("TITAN", Exchange.BSE, "Titan Company Ltd", "Consumer", 3680.00f, 45.0f, 1.24f, 920000L, 3710f, 3620f, 3886f, 3055f, SetupType.RISING_TREND, 84),
            RawStockData("WIPRO", Exchange.BSE, "Wipro Limited", "IT", 542.10f, -8.30f, -1.51f, 3100000L, 554f, 539f, 575f, 375f, SetupType.BEARISH_REJECTION, 86),
            RawStockData("NTPC", Exchange.BSE, "NTPC Limited", "Energy", 428.50f, 8.90f, 2.12f, 4500000L, 432f, 418f, 448f, 230f, SetupType.BULLISH_BOUNCE, 93),
            RawStockData("CIPLA", Exchange.BSE, "Cipla Limited", "Pharma", 1640.00f, 18.50f, 1.14f, 1100000L, 1655f, 1618f, 1702f, 1145f, SetupType.BULLISH_BREAKOUT, 87),
            RawStockData("JSWSTEEL", Exchange.BSE, "JSW Steel Ltd", "Metal", 995.00f, -16.0f, -1.58f, 2300000L, 1015f, 990f, 1060f, 740f, SetupType.BEARISH_BREAKDOWN, 81),
            RawStockData("HINDUNILVR", Exchange.BSE, "Hindustan Unilever Ltd", "FMCG", 2865.00f, -22.0f, -0.76f, 1300000L, 2895f, 2850f, 3035f, 2170f, SetupType.CONSOLIDATING, 72)
        )

        val processedStocks = rawStocks.map { raw ->
            val candles = generateCandlesForStock(raw)
            val sma44 = candles.last().sma44 ?: (raw.price * 0.985f)
            val smaPast5 = candles.getOrNull(candles.size - 6)?.sma44 ?: (sma44 * 0.995f)

            val slopeRate = ((sma44 - smaPast5) / smaPast5) * 100f
            val slope = when {
                slopeRate > 0.08f -> SmaSlope.RISING
                slopeRate < -0.08f -> SmaSlope.FALLING
                else -> SmaSlope.FLAT
            }

            val distance = (((raw.price - sma44) / sma44) * 10000).roundToInt() / 100f

            // Risk management calculations based on SMA 44 setup
            val (sl, t1, t2) = calculateTradingPlan(raw.price, sma44, raw.setupType)

            Stock(
                symbol = raw.symbol,
                exchange = raw.exchange,
                companyName = raw.name,
                sector = raw.sector,
                currentPrice = raw.price,
                change = raw.change,
                changePercent = raw.changePercent,
                volume = raw.volume,
                dayHigh = raw.dayHigh,
                dayLow = raw.dayLow,
                high52W = raw.high52W,
                low52W = raw.low52W,
                sma44 = (sma44 * 100).roundToInt() / 100f,
                smaSlope = slope,
                slopeRatePercent = (slopeRate * 100).roundToInt() / 100f,
                distanceToSmaPercent = distance,
                setupType = raw.setupType,
                setupConfidence = raw.confidence,
                entryPrice = raw.price,
                stopLoss = sl,
                target1 = t1,
                target2 = t2,
                candles = candles
            )
        }

        _stocksState.value = processedStocks
    }

    private fun calculateTradingPlan(price: Float, sma44: Float, setup: SetupType): Triple<Float, Float, Float> {
        val riskAmount: Float
        val sl: Float
        val t1: Float
        val t2: Float

        if (setup.isBullish) {
            // Stop loss placed 1.2% below SMA 44 or current low
            val buffer = sma44 * 0.012f
            sl = ((minOf(sma44 - buffer, price * 0.975f)) * 100).roundToInt() / 100f
            riskAmount = maxOf(price - sl, price * 0.015f)
            t1 = ((price + (riskAmount * 2.0f)) * 100).roundToInt() / 100f
            t2 = ((price + (riskAmount * 3.2f)) * 100).roundToInt() / 100f
        } else {
            // Bearish setup: SL above falling SMA 44
            val buffer = sma44 * 0.012f
            sl = ((maxOf(sma44 + buffer, price * 1.025f)) * 100).roundToInt() / 100f
            riskAmount = maxOf(sl - price, price * 0.015f)
            t1 = ((price - (riskAmount * 2.0f)) * 100).roundToInt() / 100f
            t2 = ((price - (riskAmount * 3.2f)) * 100).roundToInt() / 100f
        }

        return Triple(sl, t1, t2)
    }

    private fun generateCandlesForStock(raw: RawStockData): List<Candle> {
        val totalBars = 65
        val candles = ArrayList<Candle>(totalBars)
        val now = System.currentTimeMillis()
        val dayMillis = 86_400_000L
        val sdf = SimpleDateFormat("dd MMM", Locale.ENGLISH)

        // Seeded price progression towards raw.price
        val random = Random(raw.symbol.hashCode())
        val isBullish = raw.setupType.isBullish

        // Starting price 65 bars ago
        var current = if (isBullish) raw.price * 0.78f else raw.price * 1.22f
        val closingPrices = FloatArray(totalBars)

        for (i in 0 until totalBars) {
            val progress = i.toFloat() / totalBars
            val trendDrift = if (isBullish) 0.0035f else -0.0035f
            val volatility = (random.nextFloat() - 0.48f) * 0.022f

            // Specific candle shape for recent bars to simulate realistic 44 SMA bounce/breakout
            val factor = if (i >= totalBars - 3) {
                when (raw.setupType) {
                    SetupType.BULLISH_BOUNCE -> 0.018f
                    SetupType.BULLISH_BREAKOUT -> 0.025f
                    SetupType.BEARISH_REJECTION -> -0.018f
                    SetupType.BEARISH_BREAKDOWN -> -0.025f
                    else -> 0.005f
                }
            } else {
                trendDrift + volatility
            }

            val open = current
            current = (current * (1f + factor))
            val high = maxOf(open, current) * (1f + random.nextFloat() * 0.01f)
            val low = minOf(open, current) * (1f - random.nextFloat() * 0.01f)
            val close = if (i == totalBars - 1) raw.price else current
            closingPrices[i] = close

            val candleTime = now - ((totalBars - 1 - i) * dayMillis)
            val dateLabel = sdf.format(Date(candleTime))

            // Calculate 44 SMA
            val sma = if (i >= 43) {
                var sum = 0f
                for (k in (i - 43)..i) {
                    sum += closingPrices[k]
                }
                sum / 44f
            } else null

            val vol = (raw.volume * (0.6f + random.nextFloat() * 0.8f)).toLong()

            candles.add(
                Candle(
                    timestamp = candleTime,
                    dateLabel = dateLabel,
                    open = (open * 100).roundToInt() / 100f,
                    high = (high * 100).roundToInt() / 100f,
                    low = (low * 100).roundToInt() / 100f,
                    close = (close * 100).roundToInt() / 100f,
                    volume = vol,
                    sma44 = sma?.let { (it * 100).roundToInt() / 100f }
                )
            )
        }

        return candles
    }

    private data class RawStockData(
        val symbol: String,
        val exchange: Exchange,
        val name: String,
        val sector: String,
        val price: Float,
        val change: Float,
        val changePercent: Float,
        val volume: Long,
        val dayHigh: Float,
        val dayLow: Float,
        val high52W: Float,
        val low52W: Float,
        val setupType: SetupType,
        val confidence: Int
    )
}
