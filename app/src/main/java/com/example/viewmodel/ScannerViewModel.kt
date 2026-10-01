package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.AlertRuleEntity
import com.example.data.database.NotificationHistoryEntity
import com.example.data.database.ScannerDatabase
import com.example.data.database.WatchlistEntity
import com.example.data.notification.PushNotificationHelper
import com.example.data.repository.StockRepository
import com.example.model.Exchange
import com.example.model.ExchangeFilter
import com.example.model.MarketIndex
import com.example.model.MarketStatus
import com.example.model.ScannerFilter
import com.example.model.SignalFilter
import com.example.model.SortOption
import com.example.model.Stock
import com.example.model.Timeframe
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class ScannerUiState(
    val stocks: List<Stock> = emptyList(),
    val filteredStocks: List<Stock> = emptyList(),
    val watchedStocks: List<Stock> = emptyList(),
    val selectedStock: Stock? = null,
    val marketIndices: List<MarketIndex> = emptyList(),
    val marketStatus: MarketStatus = MarketStatus(false, "Market Closed", "--:-- IST", "--"),
    val filter: ScannerFilter = ScannerFilter(),
    val isScanning: Boolean = false,
    val isLiveStreaming: Boolean = true,
    val unreadNotificationsCount: Int = 0,
    val risingSmaCount: Int = 0,
    val fallingSmaCount: Int = 0,
    val bounceSignalsCount: Int = 0,
    val messageSnackbar: String? = null
)

class ScannerViewModel(application: Application) : AndroidViewModel(application) {

    private val database = ScannerDatabase.getInstance(application)
    private val notificationHelper = PushNotificationHelper(application)
    private val repository = StockRepository(database.scannerDao(), notificationHelper)

    private val _filterState = MutableStateFlow(ScannerFilter())
    val filterState: StateFlow<ScannerFilter> = _filterState.asStateFlow()

    private val _selectedStock = MutableStateFlow<Stock?>(null)
    val selectedStock: StateFlow<Stock?> = _selectedStock.asStateFlow()

    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    private val _isLiveStreaming = MutableStateFlow(true)
    val isLiveStreaming: StateFlow<Boolean> = _isLiveStreaming.asStateFlow()

    private val _snackbarEvent = MutableSharedFlow<String>()
    val snackbarEvent = _snackbarEvent.asSharedFlow()

    val watchlist: StateFlow<List<WatchlistEntity>> = repository.watchlist
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val alertRules: StateFlow<List<AlertRuleEntity>> = repository.alertRules
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val notificationHistory: StateFlow<List<NotificationHistoryEntity>> = repository.notificationHistory
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val unreadCount: StateFlow<Int> = repository.unreadNotificationsCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val marketIndices: StateFlow<List<MarketIndex>> = repository.marketIndices
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _marketStatus = MutableStateFlow(repository.getIndianMarketStatus())
    val marketStatus: StateFlow<MarketStatus> = _marketStatus.asStateFlow()

    // Combined UI State
    val uiState: StateFlow<ScannerUiState> = combine(
        repository.stocksState,
        watchlist,
        alertRules,
        _filterState,
        _selectedStock,
        _isScanning,
        _isLiveStreaming,
        unreadCount,
        marketIndices,
        _marketStatus
    ) { params: Array<Any?> ->
        val stocks = (params[0] as? List<Stock>) ?: emptyList()
        val watchedList = (params[1] as? List<WatchlistEntity>) ?: emptyList()
        val rulesList = (params[2] as? List<AlertRuleEntity>) ?: emptyList()
        val filter = (params[3] as? ScannerFilter) ?: ScannerFilter()
        val selected = params[4] as? Stock
        val scanning = (params[5] as? Boolean) ?: false
        val live = (params[6] as? Boolean) ?: true
        val unread = (params[7] as? Int) ?: 0
        val indices = (params[8] as? List<MarketIndex>) ?: emptyList()
        val mStatus = (params[9] as? MarketStatus) ?: repository.getIndianMarketStatus()

        val filtered = repository.getFilteredStocks(stocks, watchedList, rulesList, filter)
        val watchedSymbols = watchedList.map { it.symbol }.toSet()
        val watched = stocks.filter { watchedSymbols.contains(it.symbol) }

        val rising = stocks.count { it.smaSlope == com.example.model.SmaSlope.RISING }
        val falling = stocks.count { it.smaSlope == com.example.model.SmaSlope.FALLING }
        val bounces = stocks.count { it.setupType == com.example.model.SetupType.BULLISH_BOUNCE }

        ScannerUiState(
            stocks = stocks,
            filteredStocks = filtered,
            watchedStocks = watched,
            selectedStock = selected,
            marketIndices = indices,
            marketStatus = mStatus,
            filter = filter,
            isScanning = scanning,
            isLiveStreaming = live,
            unreadNotificationsCount = unread,
            risingSmaCount = rising,
            fallingSmaCount = falling,
            bounceSignalsCount = bounces
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ScannerUiState())

    private var liveStreamJob: Job? = null
    private var clockJob: Job? = null

    init {
        startMarketClock()
        startLiveStreamSimulation()
    }

    private fun startMarketClock() {
        clockJob?.cancel()
        clockJob = viewModelScope.launch {
            while (isActive) {
                _marketStatus.value = repository.getIndianMarketStatus()
                delay(1000)
            }
        }
    }

    private fun startLiveStreamSimulation() {
        liveStreamJob?.cancel()
        liveStreamJob = viewModelScope.launch {
            while (isActive) {
                if (_isLiveStreaming.value) {
                    repository.refreshStockData()
                }
                delay(3500)
            }
        }
    }

    fun toggleLiveStreaming() {
        _isLiveStreaming.value = !_isLiveStreaming.value
    }

    fun selectStock(stock: Stock?) {
        _selectedStock.value = stock
    }

    fun selectStockBySymbol(symbol: String) {
        val found = uiState.value.stocks.find { it.symbol.equals(symbol, ignoreCase = true) }
        _selectedStock.value = found
    }

    fun updateSearchQuery(query: String) {
        _filterState.value = _filterState.value.copy(searchQuery = query)
    }

    fun setExchangeFilter(exchange: ExchangeFilter) {
        _filterState.value = _filterState.value.copy(exchange = exchange)
    }

    fun setSignalFilter(signal: SignalFilter) {
        _filterState.value = _filterState.value.copy(signal = signal)
    }

    fun setTimeframe(timeframe: Timeframe) {
        _filterState.value = _filterState.value.copy(timeframe = timeframe)
    }

    fun setSectorFilter(sector: String) {
        _filterState.value = _filterState.value.copy(sector = sector)
    }

    fun setSortOption(sortOption: SortOption) {
        _filterState.value = _filterState.value.copy(sortOption = sortOption)
    }

    fun triggerScan() {
        viewModelScope.launch {
            _isScanning.value = true
            delay(600)
            repository.refreshStockData()
            _isScanning.value = false
            _snackbarEvent.emit("Scanner synced with NSE & BSE live feeds")
        }
    }

    fun toggleWatchlist(stock: Stock) {
        viewModelScope.launch {
            repository.toggleWatchlist(stock.symbol, stock.exchange, stock.companyName)
            val action = if (stock.isWatched) "removed from" else "added to"
            _snackbarEvent.emit("${stock.symbol} $action Watchlist")
        }
    }

    fun addCustomAlert(stock: Stock, alertType: String, tolerancePercent: Float = 0.5f) {
        viewModelScope.launch {
            repository.addAlertRule(stock.symbol, stock.exchange, alertType, tolerancePercent)
            _snackbarEvent.emit("Active 44 SMA Push Alert set for ${stock.symbol}!")
        }
    }

    fun deleteAlertRule(ruleId: Long) {
        viewModelScope.launch {
            repository.deleteAlertRule(ruleId)
            _snackbarEvent.emit("Alert rule deleted")
        }
    }

    fun triggerTestNotification() {
        viewModelScope.launch {
            val res = repository.triggerTestPushNotification()
            if (res != -1) {
                _snackbarEvent.emit("Push Notification dispatched! Check your status bar.")
            } else {
                _snackbarEvent.emit("Please grant Notification permission to receive alerts.")
            }
        }
    }

    fun triggerStockNotification(stock: Stock) {
        val res = repository.triggerStockPushNotification(stock)
        viewModelScope.launch {
            if (res != -1) {
                _snackbarEvent.emit("Push alert sent for ${stock.symbol}!")
            } else {
                _snackbarEvent.emit("Please enable notifications in device settings.")
            }
        }
    }

    fun markNotificationRead(id: Long) {
        viewModelScope.launch {
            repository.markNotificationRead(id)
        }
    }

    fun markAllNotificationsRead() {
        viewModelScope.launch {
            repository.markAllNotificationsRead()
            _snackbarEvent.emit("All notifications marked as read")
        }
    }

    fun clearNotifications() {
        viewModelScope.launch {
            repository.clearAllNotifications()
            _snackbarEvent.emit("Notification history cleared")
        }
    }
}
