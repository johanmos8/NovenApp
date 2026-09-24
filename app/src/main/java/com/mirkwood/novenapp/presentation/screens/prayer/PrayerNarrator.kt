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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import com.mirkwood.novenapp.data.devotional.contentLanguageFor
import java.util.Locale

/**
 * Device text-to-speech narration for the prayer pager, hoisted to
 * [PrayerScreen] so the single "Listen" control in the toolbar works on every
 * page type (reflection, prayer-with-image, Gozos) rather than only the plain
 * reading page, which is where the two inline TTS buttons used to live.
 *
 * [isSpeaking] is Compose state so the toolbar button can flip between "Listen"
 * and "Stop". The engine is shut down when [PrayerScreen] leaves composition.
 *
 * [deviceLanguage] is resolved through [contentLanguageFor], the same rule the
 * repository uses to pick the content file, so the voice always matches the text.
 */
@Composable
internal fun rememberPrayerNarrator(deviceLanguage: String): PrayerNarrator {
    val context = LocalContext.current
    val narrator = remember(deviceLanguage) { PrayerNarrator(context, deviceLanguage) }
    DisposableEffect(narrator) {
        onDispose { narrator.dispose() }
    }
    // Narration doesn't keep reading with the app in the background (P1-10 in
    // specs/code-audit-v3). TTS can't pause, so this stops; "Listen" starts the page over.
    LifecycleEventEffect(Lifecycle.Event.ON_STOP) {
        narrator.stop()
    }
    return narrator
}

internal class PrayerNarrator(context: Context, deviceLanguage: String) {

    enum class SpeakResult {
        /** Narration started. */
        Started,
        /** Nothing to do yet: the engine is still starting up, or the text is blank. */
        Ignored,
        /** No TTS engine, or no installed voice for the content's language. */
        VoiceUnavailable
    }

    private val mainHandler = Handler(Looper.getMainLooper())
    private var _isSpeaking by mutableStateOf(false)
    val isSpeaking: Boolean get() = _isSpeaking

    /** Tried in order until the engine accepts one. */
    private val narrationLocales: List<Locale> =
        if (contentLanguageFor(deviceLanguage) == "en") {
            listOf(Locale.ENGLISH)
        } else {
            // Colombian Spanish first (the novena's own tradition), then any Spanish voice.
            listOf(Locale.forLanguageTag("es-CO"), Locale.forLanguageTag("es"))
        }

    @Volatile private var ready = false
    @Volatile private var voiceAvailable = false
    @Volatile private var engineFailed = false

    private val engine = TextToSpeech(context.applicationContext) { status ->
        if (status == TextToSpeech.SUCCESS) {
            onEngineReady()
        } else {
            engineFailed = true
        }
    }

    private fun onEngineReady() {
        // setLanguage returns a negative code (LANG_MISSING_DATA / LANG_NOT_SUPPORTED)
        // when the voice can't be used; any() stops at the first locale that works,
        // leaving the engine set to it.
        voiceAvailable = narrationLocales.any { engine.setLanguage(it) >= TextToSpeech.LANG_AVAILABLE }
        engine.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) = onMain { _isSpeaking = true }
            override fun onDone(utteranceId: String?) = onMain { _isSpeaking = false }
            @Deprecated("Kept for the abstract one-arg overload on older API levels")
            override fun onError(utteranceId: String?) = onMain { _isSpeaking = false }
        })
        ready = true
    }

    private fun onMain(block: () -> Unit) {
        mainHandler.post(block)
    }

    /** Speaks [text], replacing anything already playing. */
    fun speak(text: String): SpeakResult {
        if (engineFailed || (ready && !voiceAvailable)) return SpeakResult.VoiceUnavailable
        if (!ready || text.isBlank()) return SpeakResult.Ignored
        _isSpeaking = true
        engine.speak(text, TextToSpeech.QUEUE_FLUSH, null, UTTERANCE_ID)
        return SpeakResult.Started
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
