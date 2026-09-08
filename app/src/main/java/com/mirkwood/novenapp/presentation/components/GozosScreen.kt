package com.mirkwood.novenapp.presentation.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
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
 */
@Composable
internal fun GozosScreen(
    gozos: List<Gozo>,
    image: MainModule.Hero?
) {
    val configuration = LocalConfiguration.current
    val screenWidthDp = configuration.screenWidthDp

    if (screenWidthDp < 600) {
        PhoneLayout(gozos, image)
    } else {
        TabletLayout(gozos, image)
    }
}

@Composable
internal fun TabletLayout(gozos: List<Gozo>, image: MainModule.Hero?) {
    Column(modifier = Modifier.fillMaxSize()) {
        if (image != null) {
            HeroImage(
                header = image,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.4f, fill = false)
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.6f)
                .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                .background(MaterialTheme.colorScheme.background)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                GozosHeader(style = MaterialTheme.typography.headlineLarge)
                GozosList(gozos)
            }
        }
    }
}

@Composable
internal fun PhoneLayout(gozos: List<Gozo>, image: MainModule.Hero?) {
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
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                // The pulled-up corner only makes sense overlapping a hero above it.
                .offset(y = if (image != null) (-32).dp else 0.dp)
                .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                .background(MaterialTheme.colorScheme.background)
        ) {
            // The verse list, not just the hero image, is what varies in length here -
            // this is the scroll container the old per-verse layout was missing.
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                GozosHeader(style = MaterialTheme.typography.headlineMedium)
                GozosList(gozos)
            }
        }
    }
}

@Composable
private fun GozosHeader(style: TextStyle) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = stringResource(R.string.gozos_title),
            fontWeight = FontWeight.Bold,
            style = style
        )
        IconView()
    }
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
            color = if (isRead) textColor else MaterialTheme.colorScheme.primary,
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
            FilledIconButton(onClick = onToggleRead, modifier = Modifier.size(36.dp)) {
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
