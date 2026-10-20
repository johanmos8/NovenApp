package com.mirkwood.novenapp.presentation.screens.lyrics

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.mirkwood.compose_preview.PreviewAllPhones
import com.mirkwood.novenapp.R
import com.mirkwood.novenapp.ui.theme.NovenAppTheme

/** A stanza of a carol, and whether it was detected as the chorus. */
internal data class LyricsStanza(val text: String, val isChorus: Boolean)

/** A stanza must repeat at least this many times to count as the chorus. */
private const val MIN_CHORUS_REPEATS = 3

/**
 * Splits [lyrics] into stanzas on blank lines and marks the chorus: the single stanza
 * repeated most often, and only if it appears at least [MIN_CHORUS_REPEATS] times. A tie,
 * or nothing repeated that often, means no chorus - "any repeated stanza" highlighted
 * nearly whole songs that repeat verses (e.g. 9 of 10 stanzas of "Mi burrito sabanero").
 */
internal fun parseLyrics(lyrics: String): List<LyricsStanza> {
    val stanzas = lyrics.replace("\r\n", "\n")
        .split(Regex("\n\\s*\n"))
        .map { it.trim() }
        .filter { it.isNotEmpty() }

    val counts = stanzas.groupingBy { it }.eachCount()
    val maxCount = counts.values.maxOrNull() ?: 0
    val mostRepeated = counts.filterValues { it == maxCount }.keys
    val chorus = mostRepeated.singleOrNull()?.takeIf { maxCount >= MIN_CHORUS_REPEATS }

    return stanzas.map { LyricsStanza(text = it, isChorus = it == chorus) }
}

/**
 * A carol's lyrics. The title lives in the app bar, and back navigation is the app
 * bar's too. This renders the author (when known) and the stanzas with room between
 * them; the chorus is set in the tertiary accent and italics, the same meditative
 * accent as the Gozos numbers (specs/palette-tonal-containers §3.2). Theme typography
 * throughout, so it scales with the system font size.
 *
 * @param textScale multiplier on the stanza text, driven by the lyrics app bar's "Aa"
 *   menu; it starts at 1 every time a song is opened (owner's choice).
 */
@Composable
fun LyricsScreen(lyrics: String, artist: String? = null, textScale: Float = 1f) {
    val stanzas = remember(lyrics) { parseLyrics(lyrics) }
    val baseStyle = MaterialTheme.typography.bodyLarge
    val stanzaStyle = remember(baseStyle, textScale) {
        baseStyle.copy(fontSize = baseStyle.fontSize * textScale, lineHeight = baseStyle.lineHeight * textScale)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (!artist.isNullOrBlank()) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    painter = painterResource(R.drawable.music_note_24px),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = artist,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        stanzas.forEach { stanza ->
            Text(
                text = stanza.text,
                textAlign = TextAlign.Center,
                style = stanzaStyle,
                fontStyle = if (stanza.isChorus) FontStyle.Italic else FontStyle.Normal,
                color = if (stanza.isChorus) {
                    MaterialTheme.colorScheme.tertiary
                } else {
                    MaterialTheme.colorScheme.onBackground
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@PreviewAllPhones
@Composable
fun PreviewLyricsScreen() {
    NovenAppTheme {

        LyricsScreen(
            artist = "Popular",
            lyrics = "La Virgen se está peinando\n" +
                    "Entre cortina y cortina\n" +
                    "\n" +
                    "Pero mira cómo beben los peces en el río\n" +
                    "Pero mira cómo beben por ver a Dios nacido\n" +
                    "\n" +
                    "La Virgen lava pañales\n" +
                    "Y los tiende en el romero\n" +
                    "\n" +
                    "Pero mira cómo beben los peces en el río\n" +
                    "Pero mira cómo beben por ver a Dios nacido\n" +
                    "\n" +
                    "La Virgen está lavando\n" +
                    "Con un poquito de jabón\n" +
                    "\n" +
                    "Pero mira cómo beben los peces en el río\n" +
                    "Pero mira cómo beben por ver a Dios nacido"
        )
    }
}
