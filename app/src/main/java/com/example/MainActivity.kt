package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.ui.JarvisViewModel
import com.example.ui.components.ApiKeyInfoDialog
import com.example.ui.components.HudCoreScreen
import com.example.ui.components.MicrophonePermissionRationaleDialog
import com.example.ui.components.TelemetryScreen
import com.example.ui.components.TerminalScreen
import com.example.ui.theme.JarvisCardBorder
import com.example.ui.theme.JarvisCyan
import com.example.ui.theme.JarvisDeepDark
import com.example.ui.theme.JarvisSurfaceDark
import com.example.ui.theme.JarvisSurfaceElevated
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val viewModel: JarvisViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                JarvisApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun JarvisApp(viewModel: JarvisViewModel) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val messages by viewModel.messages.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Microphone Permission Launcher
    val micPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.initVoiceEngine()
            viewModel.startListening()
            scope.launch {
                snackbarHostState.showSnackbar("Microphone access granted. J.A.R.V.I.S. is listening.")
            }
        } else {
            scope.launch {
                snackbarHostState.showSnackbar("Microphone permission denied. Voice input requires access.")
            }
        }
    }

    val requestVoiceAction = {
        val permission = Manifest.permission.RECORD_AUDIO
        val granted = ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
        if (granted) {
            viewModel.initVoiceEngine()
            viewModel.startListening()
        } else {
            viewModel.setShowPermissionRationale(true)
        }
    }

    val toggleHeyJarvisAction = {
        val permission = Manifest.permission.RECORD_AUDIO
        val granted = ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
        if (granted) {
            viewModel.initVoiceEngine()
            viewModel.toggleContinuousListening()
        } else {
            viewModel.setShowPermissionRationale(true)
        }
    }

    // Permission Rationale Dialog
    if (uiState.showPermissionRationale) {
        MicrophonePermissionRationaleDialog(
            onGrantClicked = {
                viewModel.setShowPermissionRationale(false)
                micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            },
            onDismiss = { viewModel.setShowPermissionRationale(false) }
        )
    }

    // API Key Info Dialog
    if (uiState.showApiKeyDialog) {
        ApiKeyInfoDialog(
            isApiKeyConfigured = uiState.isApiKeyPresent,
            onDismiss = { viewModel.setShowApiKeyDialog(false) }
        )
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            NavigationBar(
                modifier = Modifier
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .testTag("main_navigation_bar"),
                containerColor = JarvisSurfaceDark,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = uiState.currentTab == 0,
                    onClick = { viewModel.selectTab(0) },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.GraphicEq,
                            contentDescription = "HUD Core"
                        )
                    },
                    label = {
                        Text(
                            text = "HUD CORE",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = JarvisCyan,
                        selectedTextColor = JarvisCyan,
                        unselectedIconColor = TextMuted,
                        unselectedTextColor = TextMuted,
                        indicatorColor = JarvisSurfaceElevated
                    ),
                    modifier = Modifier.testTag("nav_tab_hud")
                )

                NavigationBarItem(
                    selected = uiState.currentTab == 1,
                    onClick = { viewModel.selectTab(1) },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Terminal,
                            contentDescription = "Terminal"
                        )
                    },
                    label = {
                        Text(
                            text = "TERMINAL",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = JarvisCyan,
                        selectedTextColor = JarvisCyan,
                        unselectedIconColor = TextMuted,
                        unselectedTextColor = TextMuted,
                        indicatorColor = JarvisSurfaceElevated
                    ),
                    modifier = Modifier.testTag("nav_tab_terminal")
                )

                NavigationBarItem(
                    selected = uiState.currentTab == 2,
                    onClick = { viewModel.selectTab(2) },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Dns,
                            contentDescription = "Telemetry"
                        )
                    },
                    label = {
                        Text(
                            text = "TELEMETRY",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = JarvisCyan,
                        selectedTextColor = JarvisCyan,
                        unselectedIconColor = TextMuted,
                        unselectedTextColor = TextMuted,
                        indicatorColor = JarvisSurfaceElevated
                    ),
                    modifier = Modifier.testTag("nav_tab_telemetry")
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(JarvisDeepDark)
                .padding(innerPadding)
        ) {
            when (uiState.currentTab) {
                0 -> HudCoreScreen(
                    uiState = uiState,
                    onStartListening = requestVoiceAction,
                    onStopListening = { viewModel.stopListening() },
                    onToggleHeyJarvis = toggleHeyJarvisAction,
                    onToggleMute = { viewModel.toggleMute() },
                    onReplayAudio = { viewModel.speakText(uiState.lastResponse) },
                    onStopSpeaking = { viewModel.stopSpeaking() },
                    onCommandSelected = { cmd -> viewModel.executeQuickCommand(cmd) },
                    onApiKeyBadgeClicked = { viewModel.setShowApiKeyDialog(true) }
                )

                1 -> TerminalScreen(
                    messages = messages,
                    onSendMessage = { text -> viewModel.sendTextDirective(text) },
                    onSpeakMessage = { text -> viewModel.speakText(text) },
                    onDeleteMessage = { id -> viewModel.deleteMessage(id) },
                    onClearAll = { viewModel.clearChat() },
                    onMicTrigger = requestVoiceAction,
                    isListening = uiState.isListening
                )

                2 -> TelemetryScreen(
                    telemetry = uiState.telemetry,
                    isApiKeyPresent = uiState.isApiKeyPresent,
                    speechPitch = uiState.speechPitch,
                    speechRate = uiState.speechRate,
                    onPitchChange = { viewModel.setSpeechPitch(it) },
                    onRateChange = { viewModel.setSpeechRate(it) },
                    onTestVoice = {
                        viewModel.speakText("All diagnostics operational, Sir. Vocal synthesizer frequency calibrated.")
                    },
                    onRefreshTelemetry = { viewModel.refreshTelemetry() },
                    onTriggerProtocol = { proto -> viewModel.executeQuickCommand(proto) },
                    onApiKeyBadgeClicked = { viewModel.setShowApiKeyDialog(true) }
                )
            }
        }
    }
}
