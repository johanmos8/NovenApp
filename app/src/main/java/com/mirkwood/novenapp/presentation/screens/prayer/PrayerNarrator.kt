package com.mirkwood.novenapp.presentation.screens.prayer

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import java.util.Locale

/**
 * Device text-to-speech narration for the prayer pager, hoisted to
 * [PrayerScreen] so the single "Listen" control in the toolbar works on every
 * page type (reflection, prayer-with-image, Gozos) rather than only the plain
 * reading page, which is where the two inline TTS buttons used to live.
 *
 * [isSpeaking] is Compose state so the toolbar button can flip between "Listen"
 * and "Stop". The engine is shut down when [PrayerScreen] leaves composition.
 */
@Composable
internal fun rememberPrayerNarrator(): PrayerNarrator {
    val context = LocalContext.current
    val narrator = remember { PrayerNarrator(context) }
    DisposableEffect(narrator) {
        onDispose { narrator.dispose() }
    }
    return narrator
}

internal class PrayerNarrator(context: Context) {

    private val mainHandler = Handler(Looper.getMainLooper())
    private var _isSpeaking by mutableStateOf(false)
    val isSpeaking: Boolean get() = _isSpeaking

    private var ready = false
    private val engine = TextToSpeech(context.applicationContext) { status ->
        ready = status == TextToSpeech.SUCCESS
        if (ready) {
            onEngineReady()
        }
    }

    private fun onEngineReady() {
        engine.language = Locale.forLanguageTag("es-CO")
        engine.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) = onMain { _isSpeaking = true }
            override fun onDone(utteranceId: String?) = onMain { _isSpeaking = false }
            @Deprecated("Kept for the abstract one-arg overload on older API levels")
            override fun onError(utteranceId: String?) = onMain { _isSpeaking = false }
        })
    }

    private fun onMain(block: () -> Unit) {
        mainHandler.post(block)
    }

    /** Speaks [text], replacing anything already playing. No-op until the engine is ready. */
    fun speak(text: String) {
        if (!ready || text.isBlank()) return
        _isSpeaking = true
        engine.speak(text, TextToSpeech.QUEUE_FLUSH, null, UTTERANCE_ID)
    }

    fun stop() {
        _isSpeaking = false
        if (ready) engine.stop()
    }

    fun dispose() {
        engine.stop()
        engine.shutdown()
    }

    private companion object {
        const val UTTERANCE_ID = "prayer-narration"
    }
}
