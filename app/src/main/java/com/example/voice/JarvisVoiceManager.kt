package com.example.voice

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale
import java.util.UUID

class JarvisVoiceManager(
    private val context: Context,
    private val onSpeechResult: (String) -> Unit,
    private val onPartialResult: (String) -> Unit = {},
    private val onListeningStateChanged: (Boolean) -> Unit = {},
    private val onSpeakingStateChanged: (Boolean) -> Unit = {},
    private val onRmsChangedCallback: (Float) -> Unit = {}
) {
    private val tag = "JarvisVoiceManager"
    private val mainHandler = Handler(Looper.getMainLooper())

    // Speech Recognizer
    private var speechRecognizer: SpeechRecognizer? = null
    private var isListeningInternal = false
    private var continuousMode = false

    // Text to Speech
    private var tts: TextToSpeech? = null
    private var isTtsInitialized = false
    private var isSpeakingInternal = false
    private var currentPitch = 1.0f
    private var currentSpeed = 1.05f

    private val _isRecognitionSupported = MutableStateFlow(true)
    val isRecognitionSupported: StateFlow<Boolean> = _isRecognitionSupported.asStateFlow()

    private val restartListeningRunnable = Runnable {
        if (continuousMode && !isSpeakingInternal && !isListeningInternal) {
            startListening()
        }
    }

    init {
        initTts()
        initSpeechRecognizer()
    }

    private fun initTts() {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                isTtsInitialized = true
                val result = tts?.setLanguage(Locale.UK)
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    tts?.setLanguage(Locale.US)
                }
                tts?.setPitch(currentPitch)
                tts?.setSpeechRate(currentSpeed)

                tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {
                        mainHandler.post {
                            isSpeakingInternal = true
                            onSpeakingStateChanged(true)
                        }
                    }

                    override fun onDone(utteranceId: String?) {
                        mainHandler.post {
                            isSpeakingInternal = false
                            onSpeakingStateChanged(false)
                            if (continuousMode) {
                                scheduleRestartListening(500)
                            }
                        }
                    }

                    @Deprecated("Deprecated in Java")
                    override fun onError(utteranceId: String?) {
                        mainHandler.post {
                            isSpeakingInternal = false
                            onSpeakingStateChanged(false)
                        }
                    }

                    override fun onError(utteranceId: String?, errorCode: Int) {
                        mainHandler.post {
                            isSpeakingInternal = false
                            onSpeakingStateChanged(false)
                        }
                    }
                })
                Log.d(tag, "TTS initialized successfully")
            } else {
                Log.e(tag, "TTS initialization failed")
            }
        }
    }

    private fun initSpeechRecognizer() {
        val available = SpeechRecognizer.isRecognitionAvailable(context)
        _isRecognitionSupported.value = available

        if (available) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                setRecognitionListener(object : RecognitionListener {
                    override fun onReadyForSpeech(params: Bundle?) {
                        mainHandler.post {
                            isListeningInternal = true
                            onListeningStateChanged(true)
                        }
                    }

                    override fun onBeginningOfSpeech() {}

                    override fun onRmsChanged(rmsdB: Float) {
                        val normalized = ((rmsdB + 2f) / 12f).coerceIn(0f, 1f)
                        onRmsChangedCallback(normalized)
                    }

                    override fun onBufferReceived(buffer: ByteArray?) {}

                    override fun onEndOfSpeech() {
                        mainHandler.post {
                            isListeningInternal = false
                            onListeningStateChanged(false)
                        }
                    }

                    override fun onError(error: Int) {
                        mainHandler.post {
                            isListeningInternal = false
                            onListeningStateChanged(false)
                            onRmsChangedCallback(0f)
                            Log.d(tag, "SpeechRecognizer error: $error")

                            // Delay restart in continuous mode to avoid CPU spin
                            if (continuousMode && !isSpeakingInternal) {
                                scheduleRestartListening(1200)
                            }
                        }
                    }

                    override fun onResults(results: Bundle?) {
                        mainHandler.post {
                            isListeningInternal = false
                            onListeningStateChanged(false)
                            onRmsChangedCallback(0f)
                            val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                            val spokenText = matches?.firstOrNull()?.trim()
                            if (!spokenText.isNullOrBlank()) {
                                onSpeechResult(spokenText)
                            } else if (continuousMode && !isSpeakingInternal) {
                                scheduleRestartListening(800)
                            }
                        }
                    }

                    override fun onPartialResults(partialResults: Bundle?) {
                        val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        val partial = matches?.firstOrNull()
                        if (!partial.isNullOrBlank()) {
                            onPartialResult(partial)
                        }
                    }

                    override fun onEvent(eventType: Int, params: Bundle?) {}
                })
            }
        }
    }

    private fun scheduleRestartListening(delayMs: Long) {
        mainHandler.removeCallbacks(restartListeningRunnable)
        mainHandler.postDelayed(restartListeningRunnable, delayMs)
    }

    fun startListening() {
        mainHandler.removeCallbacks(restartListeningRunnable)
        if (isSpeakingInternal) {
            stopSpeaking()
        }
        val recognizer = speechRecognizer ?: return
        try {
            recognizer.cancel()
        } catch (_: Exception) {}

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            putExtra("android.speech.extra.EXTRA_ADDITIONAL_LANGUAGES", arrayOf("en-IN", "hi-IN", "en-US", "en-GB"))
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
        }
        try {
            recognizer.startListening(intent)
            isListeningInternal = true
            onListeningStateChanged(true)
        } catch (e: Exception) {
            Log.e(tag, "Failed to start listening", e)
            isListeningInternal = false
            onListeningStateChanged(false)
        }
    }

    fun stopListening() {
        mainHandler.removeCallbacks(restartListeningRunnable)
        try {
            speechRecognizer?.cancel()
            speechRecognizer?.stopListening()
        } catch (e: Exception) {
            Log.e(tag, "Failed to stop listening", e)
        }
        isListeningInternal = false
        onListeningStateChanged(false)
        onRmsChangedCallback(0f)
    }

    fun setContinuousMode(enabled: Boolean) {
        continuousMode = enabled
        if (enabled && !isListeningInternal && !isSpeakingInternal) {
            startListening()
        } else if (!enabled) {
            mainHandler.removeCallbacks(restartListeningRunnable)
            if (isListeningInternal) {
                stopListening()
            }
        }
    }

    fun isContinuousMode(): Boolean = continuousMode

    fun speak(text: String, utteranceId: String = UUID.randomUUID().toString()) {
        if (!isTtsInitialized || text.isBlank()) return
        stopListening()
        try {
            val hasHindi = text.any { it in '\u0900'..'\u097F' }
            if (hasHindi) {
                tts?.setLanguage(Locale("hi", "IN"))
            } else {
                val result = tts?.setLanguage(Locale.UK)
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    tts?.setLanguage(Locale.US)
                }
            }
            tts?.setPitch(currentPitch)
            tts?.setSpeechRate(currentSpeed)
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
        } catch (e: Exception) {
            Log.e(tag, "TTS speak failed", e)
        }
    }

    fun stopSpeaking() {
        if (isTtsInitialized) {
            try {
                tts?.stop()
            } catch (e: Exception) {
                Log.e(tag, "TTS stop failed", e)
            }
            isSpeakingInternal = false
            onSpeakingStateChanged(false)
        }
    }

    fun setVoicePitch(pitch: Float) {
        currentPitch = pitch
        tts?.setPitch(pitch)
    }

    fun setSpeechRate(rate: Float) {
        currentSpeed = rate
        tts?.setSpeechRate(rate)
    }

    fun destroy() {
        continuousMode = false
        mainHandler.removeCallbacks(restartListeningRunnable)
        try {
            speechRecognizer?.cancel()
            speechRecognizer?.destroy()
            speechRecognizer = null
        } catch (e: Exception) {
            Log.e(tag, "Error destroying speech recognizer", e)
        }
        try {
            tts?.stop()
            tts?.shutdown()
            tts = null
        } catch (e: Exception) {
            Log.e(tag, "Error shutting down TTS", e)
        }
    }
}
