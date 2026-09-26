package com.example.model

import com.example.data.local.JarvisMessageEntity

enum class JarvisSystemMode(val label: String, val subtitle: String) {
    STANDBY("STANDBY", "ALL SYSTEMS NOMINAL // STARK TECH"),
    LISTENING("LISTENING", "VOICE INPUT ACTIVE // WAITING FOR DIRECTIVE"),
    PROCESSING("PROCESSING", "NEURAL CORE LINK // ANALYZING DIRECTIVE"),
    SPEAKING("TRANSMITTING", "VOCAL SYNTHESIZER ACTIVE"),
    ERROR("ALERT", "DIAGNOSTIC NOTICE")
}

data class JarvisUiState(
    val systemMode: JarvisSystemMode = JarvisSystemMode.STANDBY,
    val isListening: Boolean = false,
    val isSpeaking: Boolean = false,
    val rmsLevel: Float = 0f,
    val liveTranscript: String = "",
    val lastResponse: String = "Good day, Sir. All systems operational. I am at your disposal.",
    val continuousHeyJarvis: Boolean = false,
    val isMuted: Boolean = false,
    val speechPitch: Float = 1.0f,
    val speechRate: Float = 1.05f,
    val currentTab: Int = 0, // 0: HUD Core, 1: Terminal, 2: Telemetry
    val isApiKeyPresent: Boolean = false,
    val telemetry: DeviceTelemetry = DeviceTelemetry(),
    val showApiKeyDialog: Boolean = false,
    val showPermissionRationale: Boolean = false,
    val statusMessage: String = "Core online. Ready for command."
)
