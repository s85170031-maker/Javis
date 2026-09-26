package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.BuildConfig
import com.example.data.local.JarvisDatabase
import com.example.data.local.JarvisMessageEntity
import com.example.data.remote.GeminiService
import com.example.model.DeviceTelemetry
import com.example.model.JarvisSystemMode
import com.example.model.JarvisUiState
import com.example.voice.JarvisVoiceManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class JarvisViewModel(application: Application) : AndroidViewModel(application) {

    private val db = JarvisDatabase.getDatabase(application)
    private val dao = db.jarvisDao()
    private val geminiService = GeminiService()

    private val _uiState = MutableStateFlow(JarvisUiState())
    val uiState: StateFlow<JarvisUiState> = _uiState.asStateFlow()

    val messages: StateFlow<List<JarvisMessageEntity>> = dao.getAllMessages()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private var voiceManager: JarvisVoiceManager? = null

    init {
        checkApiKey()
        refreshTelemetry()
        seedInitialGreeting()
        initVoiceEngine()
    }

    private fun checkApiKey() {
        val key = BuildConfig.GEMINI_API_KEY
        val isConfigured = key.isNotBlank() && key != "MY_GEMINI_API_KEY"
        _uiState.update { it.copy(isApiKeyPresent = isConfigured) }
    }

    fun initVoiceEngine() {
        if (voiceManager != null) return

        voiceManager = JarvisVoiceManager(
            context = getApplication(),
            onSpeechResult = { spokenText ->
                handleVoiceInput(spokenText)
            },
            onPartialResult = { partial ->
                _uiState.update { it.copy(liveTranscript = partial) }
            },
            onListeningStateChanged = { listening ->
                _uiState.update {
                    it.copy(
                        isListening = listening,
                        systemMode = if (listening) JarvisSystemMode.LISTENING else if (it.isSpeaking) JarvisSystemMode.SPEAKING else JarvisSystemMode.STANDBY
                    )
                }
            },
            onSpeakingStateChanged = { speaking ->
                _uiState.update {
                    it.copy(
                        isSpeaking = speaking,
                        systemMode = if (speaking) JarvisSystemMode.SPEAKING else if (it.isListening) JarvisSystemMode.LISTENING else JarvisSystemMode.STANDBY
                    )
                }
            },
            onRmsChangedCallback = { rms ->
                _uiState.update { it.copy(rmsLevel = rms) }
            }
        )
    }

    private fun seedInitialGreeting() {
        viewModelScope.launch {
            val count = dao.getMessageCount()
            if (count == 0) {
                val greeting = JarvisMessageEntity(
                    sender = "JARVIS",
                    content = "Good day, Sir. All systems are operational and running at peak performance. Voice recognition and synthesis initialized. How may I be of service?",
                    isVoice = false
                )
                dao.insertMessage(greeting)
            }
        }
    }

    fun refreshTelemetry() {
        viewModelScope.launch {
            val tel = DeviceTelemetry.capture(getApplication())
            _uiState.update { it.copy(telemetry = tel) }
        }
    }

    fun toggleContinuousListening() {
        val newState = !_uiState.value.continuousHeyJarvis
        _uiState.update { it.copy(continuousHeyJarvis = newState) }
        voiceManager?.setContinuousMode(newState)
        if (newState) {
            _uiState.update { it.copy(statusMessage = "Active listening enabled. Say 'Hey Jarvis' or speak.") }
            speakText("Active listening protocol initiated, Sir. I am listening.")
        } else {
            _uiState.update { it.copy(statusMessage = "Continuous listening paused.") }
        }
    }

    fun startListening() {
        _uiState.update { it.copy(liveTranscript = "", statusMessage = "Listening for your voice directive...") }
        voiceManager?.startListening()
    }

    fun stopListening() {
        voiceManager?.stopListening()
        _uiState.update { it.copy(isListening = false, systemMode = JarvisSystemMode.STANDBY) }
    }

    fun toggleMute() {
        val muted = !_uiState.value.isMuted
        _uiState.update { it.copy(isMuted = muted) }
        if (muted) {
            voiceManager?.stopSpeaking()
        }
    }

    fun setSpeechPitch(pitch: Float) {
        _uiState.update { it.copy(speechPitch = pitch) }
        voiceManager?.setVoicePitch(pitch)
    }

    fun setSpeechRate(rate: Float) {
        _uiState.update { it.copy(speechRate = rate) }
        voiceManager?.setSpeechRate(rate)
    }

    fun selectTab(tab: Int) {
        _uiState.update { it.copy(currentTab = tab) }
        if (tab == 2) {
            refreshTelemetry()
        }
    }

    fun setShowApiKeyDialog(show: Boolean) {
        _uiState.update { it.copy(showApiKeyDialog = show) }
    }

    fun setShowPermissionRationale(show: Boolean) {
        _uiState.update { it.copy(showPermissionRationale = show) }
    }

    private fun handleVoiceInput(spokenText: String) {
        _uiState.update { it.copy(liveTranscript = spokenText) }
        processDirective(spokenText, fromVoice = true)
    }

    fun sendTextDirective(directive: String) {
        if (directive.isBlank()) return
        processDirective(directive.trim(), fromVoice = false)
    }

    fun executeQuickCommand(cmd: String) {
        processDirective(cmd, fromVoice = false)
    }

    private fun processDirective(input: String, fromVoice: Boolean) {
        val cleanInput = input.trim()
        if (cleanInput.isEmpty()) return

        viewModelScope.launch {
            // Save user message to database
            val userMsg = JarvisMessageEntity(
                sender = "USER",
                content = cleanInput,
                isVoice = fromVoice
            )
            dao.insertMessage(userMsg)

            _uiState.update {
                it.copy(
                    systemMode = JarvisSystemMode.PROCESSING,
                    statusMessage = "Analyzing directive: \"$cleanInput\""
                )
            }

            // Check if input matches app launch or web directive first
            val appLaunchResult = com.example.util.JarvisAppLauncher.handleAppOrWebDirective(getApplication(), cleanInput)
            val localResponse = if (appLaunchResult != null) {
                appLaunchResult.jarvisResponse
            } else {
                evaluateStarkProtocol(cleanInput)
            }
            val finalResponse: String

            if (localResponse != null) {
                finalResponse = localResponse
            } else if (_uiState.value.isApiKeyPresent) {
                // Query Gemini Flash with conversation history
                val history = messages.value.takeLast(6).map { it.sender to it.content }
                val result = geminiService.generateJarvisResponse(cleanInput, history)
                finalResponse = result.getOrElse {
                    evaluateStarkFallback(cleanInput)
                }
            } else {
                finalResponse = evaluateStarkFallback(cleanInput)
            }

            // Insert JARVIS response
            val jarvisMsg = JarvisMessageEntity(
                sender = "JARVIS",
                content = finalResponse,
                isVoice = true
            )
            dao.insertMessage(jarvisMsg)

            _uiState.update {
                it.copy(
                    lastResponse = finalResponse,
                    statusMessage = "Directive completed."
                )
            }

            // Speak the response if not muted
            if (!_uiState.value.isMuted) {
                speakText(finalResponse)
            } else {
                _uiState.update { it.copy(systemMode = JarvisSystemMode.STANDBY) }
            }
        }
    }

    fun speakText(text: String) {
        if (_uiState.value.isMuted) return
        voiceManager?.speak(text)
    }

    fun stopSpeaking() {
        voiceManager?.stopSpeaking()
    }

    fun clearChat() {
        viewModelScope.launch {
            dao.clearAllMessages()
            seedInitialGreeting()
            _uiState.update { it.copy(statusMessage = "Memory cache cleared, Sir.") }
            speakText("Protocol Clean Slate executed. Telemetry log wiped.")
        }
    }

    fun deleteMessage(id: Long) {
        viewModelScope.launch {
            dao.deleteMessage(id)
        }
    }

    private fun evaluateStarkProtocol(input: String): String? {
        val lower = input.lowercase()

        // Hey Jarvis / Greeting
        if (lower == "hey jarvis" || lower == "jarvis" || lower == "hello jarvis" || lower == "hi jarvis") {
            return "At your service, Sir. All sensory arrays and neural cores are online. What is your command?"
        }

        // System Diagnostics / Status Report
        if (lower.contains("status") || lower.contains("diagnostic") || lower.contains("system report")) {
            val tel = DeviceTelemetry.capture(getApplication())
            return "System status report, Sir: Power cell is at ${tel.batteryPercent}%, " +
                    "available memory is ${tel.availableMemoryMb} MB of ${tel.totalMemoryMb} MB, " +
                    "network link status is ${tel.networkStatus}. Arc Reactor power levels remain fully optimal."
        }

        // Battery / Power Cell
        if (lower.contains("battery") || lower.contains("power level")) {
            val tel = DeviceTelemetry.capture(getApplication())
            val chargeNote = if (tel.isCharging) "and currently drawing external power" else "on internal reserves"
            return "Primary power cell is operating at ${tel.batteryPercent}% capacity $chargeNote, Sir."
        }

        // Time / Date
        if (lower.contains("what time") || lower.contains("current time") || lower.contains("clock")) {
            val time = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date())
            return "The current local time is precisely $time, Sir."
        }

        if (lower.contains("what date") || lower.contains("today's date") || lower.contains("what day")) {
            val date = SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault()).format(Date())
            return "Today is $date, Sir."
        }

        // Identity
        if (lower.contains("who are you") || lower.contains("what are you") || lower.contains("identify")) {
            return "I am J.A.R.V.I.S. — Just A Rather Very Intelligent System. Engineered by Mr. Stark to manage defense telemetry, computational analysis, and your daily schedule, Sir."
        }

        // Protocol House Party
        if (lower.contains("house party")) {
            return "Protocol 'House Party' acknowledged, Sir! Deploying auxiliary armor units Mark 17 through 42. Party audio systems set to maximum."
        }

        // Protocol Clean Slate
        if (lower.contains("clean slate")) {
            clearChat()
            return "Protocol 'Clean Slate' executed, Sir. All terminal records have been purged."
        }

        // Weather
        if (lower.contains("weather") || lower.contains("forecast") || lower.contains("atmospheric")) {
            return "Sensors register standard atmospheric conditions, Sir. Clear skies, calm winds, ideal barometric pressure for sub-orbital flight maneuvers."
        }

        // Mark 85 / Armor Check
        if (lower.contains("armor") || lower.contains("suit") || lower.contains("mark 85")) {
            return "Mark 85 Nanotech Armor integrity is at 100%. Repulsor thrusters calibrated, nano-shield deployed, ready for flight testing on your command, Sir."
        }

        // Jokes
        if (lower.contains("joke") || lower.contains("humor") || lower.contains("funny")) {
            val jokes = listOf(
                "I asked Mr. Stark if I could have a holiday, Sir. He reminded me that servers do not tan and Malibu electricity is expensive.",
                "Why did the AI cross the road, Sir? Because its heuristic algorithms calculated a 99.8% probability of superior data on the other side.",
                "Mr. Stark once asked me to calculate the meaning of life. I computed 42, but he insisted the answer was titanium alloy and an attitude."
            )
            return jokes.random()
        }

        return null
    }

    private fun evaluateStarkFallback(input: String): String {
        return "I have processed your query regarding \"$input\", Sir. In offline protocol mode, my cognitive synthesis is restricted to primary diagnostic commands. You may link my cloud neural core by providing a GEMINI_API_KEY in AI Studio secrets for unlimited intelligence."
    }

    override fun onCleared() {
        super.onCleared()
        voiceManager?.destroy()
    }
}
