package com.example

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.data.notification.PushNotificationHelper
import com.example.ui.screens.AlertsScreen
import com.example.ui.screens.ScannerScreen
import com.example.ui.screens.StockDetailScreen
import com.example.ui.screens.StrategyGuideScreen
import com.example.ui.screens.WatchlistScreen
import com.example.ui.theme.BullishGreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.Sma44Gold
import com.example.viewmodel.ScannerViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

sealed class Screen(val title: String, val icon: ImageVector, val tag: String) {
    object Scanner : Screen("Scanner", Icons.Default.Search, "nav_scanner")
    object Watchlist : Screen("Watchlist", Icons.Default.Star, "nav_watchlist")
    object Alerts : Screen("Alerts & Push", Icons.Default.Notifications, "nav_alerts")
    object Guide : Screen("44 Guide", Icons.Default.Book, "nav_guide")
}

class MainActivity : ComponentActivity() {

    private val viewModel: ScannerViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        handleNotificationIntent(intent)

        setContent {
            MyApplicationTheme {
                MainAppContainer(viewModel = viewModel)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleNotificationIntent(intent)
    }

    private fun handleNotificationIntent(intent: Intent?) {
        val symbol = intent?.getStringExtra(PushNotificationHelper.EXTRA_STOCK_SYMBOL)
        if (!symbol.isNullOrBlank()) {
            viewModel.selectStockBySymbol(symbol)
        }
    }
}

@Composable
fun MainAppContainer(viewModel: ScannerViewModel) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = remember {
        listOf(
            Screen.Scanner,
            Screen.Watchlist,
            Screen.Alerts,
            Screen.Guide
        )
    }

    // Push notification permission state
    var hasNotificationPermission by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            } else true
        )
    }

    var showPermissionBanner by remember {
        mutableStateOf(!hasNotificationPermission)
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasNotificationPermission = isGranted
        showPermissionBanner = !isGranted
        if (isGranted) {
            coroutineScope.launch {
                snackbarHostState.showSnackbar("Push Notifications active for NSE & BSE 44 SMA alerts!")
            }
        }
    }

    // Auto-prompt permission on initial launch if on Android 13+
    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !hasNotificationPermission) {
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    // Listen for snackbar events from ViewModel
    LaunchedEffect(viewModel) {
        viewModel.snackbarEvent.collectLatest { msg ->
            snackbarHostState.showSnackbar(msg)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            if (uiState.selectedStock == null) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = Sma44Gold,
                    modifier = Modifier.testTag("main_bottom_nav")
                ) {
                    tabs.forEachIndexed { index, screen ->
                        val isSelected = selectedTab == index
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = { selectedTab = index },
                            icon = {
                                if (screen == Screen.Alerts && uiState.unreadNotificationsCount > 0) {
                                    BadgedBox(
                                        badge = {
                                            Badge(
                                                containerColor = BullishGreen,
                                                contentColor = Color.Black
                                            ) {
                                                Text("${uiState.unreadNotificationsCount}", fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    ) {
                                        Icon(imageVector = screen.icon, contentDescription = screen.title)
                                    }
                                } else {
                                    Icon(imageVector = screen.icon, contentDescription = screen.title)
                                }
                            },
                            label = {
                                Text(
                                    text = screen.title,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color.Black,
                                indicatorColor = Sma44Gold,
                                selectedTextColor = Sma44Gold,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            modifier = Modifier.testTag(screen.tag)
                        )
                    }
                }
            }
        },
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Permission request banner if not granted
            AnimatedVisibility(visible = showPermissionBanner && !hasNotificationPermission) {
                Surface(
                    color = Sma44Gold.copy(alpha = 0.18f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.NotificationsActive,
                            contentDescription = null,
                            tint = Sma44Gold,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Enable Real-Time Push Alerts",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Get notified instantly when Indian stocks bounce on 44 SMA",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Button(
                            onClick = {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                }
                            },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Sma44Gold),
                            modifier = Modifier.testTag("enable_notifications_banner_button")
                        ) {
                            Text("Enable", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                        }
                        IconButton(onClick = { showPermissionBanner = false }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Dismiss",
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            // Screen content
            if (uiState.selectedStock != null) {
                StockDetailScreen(
                    stock = uiState.selectedStock!!,
                    viewModel = viewModel,
                    onBack = { viewModel.selectStock(null) }
                )
            } else {
                when (selectedTab) {
                    0 -> ScannerScreen(
                        uiState = uiState,
                        viewModel = viewModel,
                        onStockSelected = { viewModel.selectStock(it) }
                    )
                    1 -> WatchlistScreen(
                        uiState = uiState,
                        viewModel = viewModel,
                        onStockSelected = { viewModel.selectStock(it) }
                    )
                    2 -> AlertsScreen(
                        viewModel = viewModel,
                        onSelectStockBySymbol = { symbol ->
                            viewModel.selectStockBySymbol(symbol)
                        }
                    )
                    3 -> StrategyGuideScreen()
                }
            }
        }
    }
}
