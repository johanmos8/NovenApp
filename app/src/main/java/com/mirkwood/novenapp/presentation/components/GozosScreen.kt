package com.mirkwood.novenapp.presentation.components

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import com.mirkwood.compose_preview.PreviewAllPhones
import com.mirkwood.novenapp.R
import com.mirkwood.novenapp.presentation.model.Gozo
import com.mirkwood.novenapp.presentation.model.MainModule
import com.mirkwood.novenapp.ui.theme.NovenAppTheme

/**
 * All of a devotional's Gozos verses on one scrollable page. A Gozos card is a short
 * call-and-response litany meant to be prayed straight through - the previous design
 * paged one verse per full-screen swipe, which also clipped the verse text on phones
 * for anything longer than the shortest verse (no vertical scroll in that layout).
 *
 * The soundtrack toggle is a [SmallFloatingActionButton] pinned to the bottom of the
 * verse card rather than an icon in the scrolling header: the Gozos are sung, so the
 * control has to stay reachable while the reader scrolls down the litany. Playback
 * state is hoisted here so it survives the internal recompositions and the
 * phone/tablet layout switch; the player itself is released when this page leaves
 * composition (e.g. swiping to the next prayer stage), so the music never bleeds over.
 */
@Composable
internal fun GozosScreen(
    gozos: List<Gozo>,
    image: MainModule.Hero?
) {
    val configuration = LocalConfiguration.current
    val isCompactWidth = configuration.screenWidthDp < 600

    var musicPlaying by rememberSaveable { mutableStateOf(false) }
    GozosSoundtrack(playing = musicPlaying, onPaused = { musicPlaying = false })

    val toggleMusic = { musicPlaying = !musicPlaying }

    if (isCompactWidth) {
        PhoneLayout(gozos, image, musicPlaying, toggleMusic)
    } else {
        TabletLayout(gozos, image, musicPlaying, toggleMusic)
    }
}

@Composable
internal fun TabletLayout(
    gozos: List<Gozo>,
    image: MainModule.Hero?,
    musicPlaying: Boolean,
    onToggleMusic: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        if (image != null) {
            HeroImage(
                header = image,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.4f, fill = false)
            )
        }
        GozosPanel(
            gozos = gozos,
            headerStyle = MaterialTheme.typography.headlineLarge,
            musicPlaying = musicPlaying,
            onToggleMusic = onToggleMusic,
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.6f)
                .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                .background(MaterialTheme.colorScheme.background)
        )
    }
}

@Composable
internal fun PhoneLayout(
    gozos: List<Gozo>,
    image: MainModule.Hero?,
    musicPlaying: Boolean,
    onToggleMusic: () -> Unit,
) {
    val screenHeight = LocalConfiguration.current.screenHeightDp.dp
    val fontScale = LocalDensity.current.fontScale
    val isLargeFont = fontScale > 1.2f
    val baseHeroHeight = screenHeight * 0.35f
    val heroHeight = if (isLargeFont) {
        (baseHeroHeight / fontScale.coerceAtMost(1.6f)).coerceIn(120.dp, baseHeroHeight)
    } else {
        baseHeroHeight
    }
    Column(modifier = Modifier.fillMaxSize()) {
        if (image != null) {
            HeroImage(
                header = image,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(heroHeight)
            )
        }
        GozosPanel(
            gozos = gozos,
            headerStyle = MaterialTheme.typography.headlineMedium,
            musicPlaying = musicPlaying,
            onToggleMusic = onToggleMusic,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                // The pulled-up corner only makes sense overlapping a hero above it.
                .offset(y = if (image != null) (-32).dp else 0.dp)
                .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                .background(MaterialTheme.colorScheme.background)
        )
    }
}

/**
 * The scrollable verse card plus the pinned soundtrack control. The scroll content
 * carries extra bottom padding so the last verse can be read clear of the floating
 * button.
 */
@Composable
private fun GozosPanel(
    gozos: List<Gozo>,
    headerStyle: TextStyle,
    musicPlaying: Boolean,
    onToggleMusic: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier) {
        // The verse list, not just the hero image, is what varies in length here -
        // this is the scroll container the old per-verse layout was missing.
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
                .padding(bottom = 72.dp)
        ) {
            GozosHeader(style = headerStyle)
            GozosList(gozos)
        }
        MusicToggleButton(
            playing = musicPlaying,
            onClick = onToggleMusic,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        )
    }
}

@Composable
private fun GozosHeader(style: TextStyle) {
    Text(
        text = stringResource(R.string.gozos_title),
        fontWeight = FontWeight.Bold,
        style = style,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
    )
}

@Composable
private fun MusicToggleButton(
    playing: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val description = stringResource(
        if (playing) {
            R.string.content_description_toggle_music_off
        } else {
            R.string.content_description_toggle_music_on
        }
    )
    SmallFloatingActionButton(
        onClick = onClick,
        modifier = modifier,
        containerColor = if (playing) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.surfaceVariant
        },
        contentColor = if (playing) {
            MaterialTheme.colorScheme.onPrimary
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        },
    ) {
        Icon(
            imageVector = if (playing) Icons.Filled.Pause else Icons.Filled.MusicNote,
            contentDescription = description,
        )
    }
}

/**
 * Drives the Gozos soundtrack from [playing]. Starting, pausing and releasing happen
 * inside effects rather than in the composition body, so a recomposition can't
 * re-trigger playback and the native player is always released when the page is left.
 *
 * The music pauses - and stays paused, calling [onPaused] so the button agrees - when
 * the app goes to the background, or when another app takes audio focus (a call,
 * another music app). Rotation is not treated as leaving the app. See P1-10 in
 * specs/code-audit-v3.
 */
@Composable
private fun GozosSoundtrack(playing: Boolean, onPaused: () -> Unit) {
    val context = LocalContext.current
    val currentOnPaused by rememberUpdatedState(onPaused)
    val soundtrack = remember { SoundtrackPlayer(context.applicationContext) { currentOnPaused() } }

    DisposableEffect(soundtrack) {
        onDispose { soundtrack.release() }
    }

    LaunchedEffect(playing, soundtrack) {
        if (playing) soundtrack.play() else soundtrack.pause()
    }

    // Paused directly here, not only through `playing`: recomposition isn't guaranteed
    // to run while the app is in the background, so the state change alone could
    // leave the music playing.
    LifecycleEventEffect(Lifecycle.Event.ON_STOP) {
        if (context.findActivity()?.isChangingConfigurations != true) {
            soundtrack.pause()
            currentOnPaused()
        }
    }
}

/**
 * The looping soundtrack player plus its audio focus. The [MediaPlayer] is only created
 * on the first [play], not when the Gozos page first shows, since most visits never
 * play the music (P2-3 in specs/code-audit-v3).
 */
private class SoundtrackPlayer(
    private val context: Context,
    private val onFocusLost: () -> Unit
) {
    private var player: MediaPlayer? = null
    private val audioManager = context.getSystemService(AudioManager::class.java)
    private val focusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build()
        )
        .setOnAudioFocusChangeListener { change ->
            // "Can duck" losses are handled by the system lowering the volume.
            if (change == AudioManager.AUDIOFOCUS_LOSS ||
                change == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT
            ) {
                pause()
                onFocusLost()
            }
        }
        .build()

    fun play() {
        if (audioManager.requestAudioFocus(focusRequest) != AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            // e.g. during a phone call: don't play over it, and flip the button back.
            onFocusLost()
            return
        }
        val mediaPlayer = player
            ?: MediaPlayer.create(context, MainModule.Instrument.Music.soundRes)
                ?.apply { isLooping = true }
                ?.also { player = it }
        if (mediaPlayer == null) {
            audioManager.abandonAudioFocusRequest(focusRequest)
            onFocusLost()
            return
        }
        if (!mediaPlayer.isPlaying) mediaPlayer.start()
    }

    fun pause() {
        player?.takeIf { it.isPlaying }?.pause()
        audioManager.abandonAudioFocusRequest(focusRequest)
    }

    fun release() {
        player?.release()
        player = null
        audioManager.abandonAudioFocusRequest(focusRequest)
    }
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

/**
 * Reading progress through the litany, not a permanent record - it lives only for
 * this composition (surviving rotation via [rememberSaveable], but not meant to
 * persist like the day-level "mark as prayed" flag does). Marking a verse blurs and
 * dims it, so scanning down the list shows at a glance which one to pick back up on.
 */
@Composable
private fun GozosList(gozos: List<Gozo>) {
    var readIds by rememberSaveable(
        stateSaver = listSaver(
            save = { it.toList() },
            restore = { it.toSet() }
        )
    ) { mutableStateOf(emptySet<Int>()) }

    Column(modifier = Modifier.fillMaxWidth()) {
        gozos.forEachIndexed { index, gozo ->
            val isRead = gozo.id in readIds
            GozoRow(
                number = index + 1,
                gozo = gozo,
                isRead = isRead,
                onToggleRead = {
                    readIds = if (isRead) readIds - gozo.id else readIds + gozo.id
                }
            )
            if (index != gozos.lastIndex) {
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            }
        }
    }
}

@Composable
private fun GozoRow(number: Int, gozo: Gozo, isRead: Boolean, onToggleRead: () -> Unit) {
    val blurRadius by animateDpAsState(targetValue = if (isRead) 3.dp else 0.dp, label = "gozoBlur")
    val textColor = if (isRead) {
        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = "$number.",
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            // Tertiary: a meditative accent, not an action (specs/palette-tonal-containers §3.3).
            color = if (isRead) textColor else MaterialTheme.colorScheme.tertiary,
            modifier = Modifier.width(28.dp)
        )
        Text(
            text = gozo.texto,
            style = MaterialTheme.typography.bodyLarge,
            color = textColor,
            modifier = Modifier
                .weight(1f)
                .padding(end = 12.dp)
                .blur(blurRadius)
        )
        val toggleDescription = stringResource(
            if (isRead) R.string.gozo_marked_read else R.string.gozo_mark_read,
            number
        )
        if (isRead) {
            // Progress amber, matching the day screen's "marked as prayed" chip, instead
            // of FilledIconButton's default primary.
            FilledIconButton(
                onClick = onToggleRead,
                modifier = Modifier.size(36.dp),
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.secondary,
                    contentColor = MaterialTheme.colorScheme.onSecondary
                )
            ) {
                Icon(Icons.Default.Check, contentDescription = toggleDescription, modifier = Modifier.size(18.dp))
            }
        } else {
            OutlinedIconButton(onClick = onToggleRead, modifier = Modifier.size(36.dp)) {
                Icon(Icons.Default.Check, contentDescription = toggleDescription, modifier = Modifier.size(18.dp))
            }
        }
    }
}

private val previewGozos = listOf(
    Gozo(
        id = 1,
        texto = "¡Oh, Sapiencia suma del Dios soberano, que a infantil alcance te rebajas sacro! ¡Oh, Divino Niño, ven para enseñarnos la prudencia que hace verdaderos sabios! (Villancico 'Ven a nuestras almas')"
    ),
    Gozo(
        id = 2,
        texto = "¡Oh, Adonai potente que Moisés hablando, de Israel al pueblo diste los mandatos! ¡Ah, ven prontamente para rescatarnos, y que un niño débil muestre fuerte el brazo! (Villancico 'Ven a nuestras almas')"
    ),
    Gozo(
        id = 12,
        texto = "¡Ven Salvador nuestro por quien suspiramos Ven a nuestras almas, Ven, no tardes tanto! (Villancico 'Ven a nuestras almas')"
    )
)

@PreviewAllPhones
@Composable
fun PreviewGozosScreen() {
    NovenAppTheme {
        GozosScreen(
            gozos = previewGozos,
            image = MainModule.Hero(
                imageResId = R.drawable.virgen_maria,
                height = MainModule.Hero.Height.Medium
            )
        )
    }
}
