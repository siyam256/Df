package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.AppsCacheManagerCard
import com.example.ui.components.BatteryOptimizationNotice
import com.example.ui.components.DailyScheduleCard
import com.example.ui.components.DashboardHero
import com.example.ui.components.HistoryLogSection
import com.example.ui.components.JunkCategoriesSection
import com.example.ui.components.TimePickerDialogComponent
import com.example.ui.theme.EmeraldContainer
import com.example.ui.theme.EmeraldOnContainer
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.WhiteBackground
import com.example.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                MainScreen(viewModel = viewModel)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.checkBatteryOptimization()
        viewModel.checkPermissionsAndServices()
        viewModel.loadStorageAndApps()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: MainViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    // Request Notification permission on Android 13+
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { _ -> }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    LaunchedEffect(uiState.showSuccessSnackbar) {
        if (uiState.showSuccessSnackbar) {
            snackbarHostState.showSnackbar(uiState.successSnackbarText)
            viewModel.dismissSnackbar()
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = WhiteBackground,
        contentWindowInsets = WindowInsets.safeDrawing,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(EmeraldContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CleaningServices,
                                contentDescription = null,
                                tint = EmeraldPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = if (uiState.language == "bn") "অটো ক্যাচ ক্লিনার" else "Auto Cache Cleaner",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = if (uiState.language == "bn") "ডেইলি ব্যাকগ্রাউন্ড অপ্টিমাইজেশন" else "Daily Background Optimizer",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }
                    }
                },
                actions = {
                    // Language Switcher Badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(EmeraldContainer)
                            .border(1.dp, Color(0xFFA7F3D0), RoundedCornerShape(12.dp))
                            .clickable { viewModel.toggleLanguage() }
                            .testTag("language_toggle_button")
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Translate,
                                contentDescription = "Toggle Language",
                                tint = EmeraldOnContainer,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (uiState.language == "bn") "বাংলা" else "EN",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = EmeraldOnContainer
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    IconButton(
                        onClick = { viewModel.loadStorageAndApps() },
                        modifier = Modifier.testTag("refresh_stats_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh Stats",
                            tint = TextSecondary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.TopCenter
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 600.dp)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item { Spacer(modifier = Modifier.height(4.dp)) }

                // 1. Dashboard Hero
                item {
                    DashboardHero(
                        isCleaning = uiState.isCleaning,
                        progress = uiState.cleanProgress,
                        cleaningStep = uiState.cleaningStep,
                        estimatedJunkBytes = uiState.stats.totalJunkBytes,
                        lastCleanedBytes = uiState.lastCleanedBytes,
                        totalCleanedBytes = uiState.totalCleanedBytes,
                        language = uiState.language,
                        onCleanClick = { viewModel.performClean() }
                    )
                }

                // 2. Apps Cache Manager Card (Core answer to user query)
                item {
                    AppsCacheManagerCard(
                        apps = uiState.installedApps,
                        isAccessibilityEnabled = uiState.isAccessibilityEnabled,
                        hasUsageStatsPermission = uiState.hasUsageStatsPermission,
                        cleanState = uiState.appCleanState,
                        language = uiState.language,
                        onStartAutoClean = { viewModel.startAutomatedAppClean() },
                        onStopAutoClean = { viewModel.stopAutomatedAppClean() },
                        onOpenAppStorage = { pkg -> viewModel.openAppStorage(pkg) },
                        onRequestAccessibility = { viewModel.requestAccessibility() },
                        onRequestUsageAccess = { viewModel.requestUsageAccess() }
                    )
                }

                // 3. Battery Optimization Notice (if needed for background wakeup)
                item {
                    BatteryOptimizationNotice(
                        isIgnored = uiState.isBatteryOptimizationIgnored,
                        language = uiState.language,
                        onOptimizedResolved = { viewModel.checkBatteryOptimization() }
                    )
                }

                // 4. Daily Scheduled Clean Card
                item {
                    DailyScheduleCard(
                        isEnabled = uiState.isScheduleEnabled,
                        hour = uiState.scheduleHour,
                        minute = uiState.scheduleMinute,
                        language = uiState.language,
                        onToggle = { viewModel.toggleSchedule(it) },
                        onChangeTimeClick = { viewModel.setShowTimePicker(true) }
                    )
                }

                // 5. Scanned Cache Categories Breakdown
                item {
                    JunkCategoriesSection(
                        categories = uiState.categories,
                        language = uiState.language
                    )
                }

                // 6. Clean History Records
                item {
                    HistoryLogSection(
                        historyList = uiState.historyList,
                        language = uiState.language,
                        onClearHistory = { viewModel.clearHistory() }
                    )
                }

                // Minimal Clean Footer Note
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = if (uiState.language == "bn") {
                                "অটো ক্যাচ ক্লিনার • শতভাগ নিরাপদ ও ব্যাটারি ফ্রেন্ডলি"
                            } else {
                                "Auto Cache Cleaner • 100% Safe & Battery Friendly"
                            },
                            fontSize = 11.sp,
                            color = TextMuted
                        )
                    }
                }
            }
        }
    }

    // Material 3 Time Picker Dialog
    if (uiState.showTimePicker) {
        TimePickerDialogComponent(
            initialHour = uiState.scheduleHour,
            initialMinute = uiState.scheduleMinute,
            language = uiState.language,
            onConfirm = { hour, minute ->
                viewModel.updateScheduleTime(hour, minute)
            },
            onDismiss = {
                viewModel.setShowTimePicker(false)
            }
        )
    }
}
