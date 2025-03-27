package com.mirkwood.novenapp.presentation.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterStart
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mirkwood.compose_preview.PreviewAllPhones
import com.mirkwood.novenapp.R
import com.mirkwood.novenapp.presentation.NovenaAction
import com.mirkwood.novenapp.ui.theme.NovenAppTheme


@Composable
internal fun AppDrawer(
    modifier: Modifier = Modifier,
    onOptionClick: (NovenaAction) -> Unit,
    onLyricsClick: () -> Unit,
    closeDrawer: () -> Unit = {}
) {
    val days =
        (1..9).map { stringResource(R.string.text_dia, it) } // Genera los títulos de los días

    Column(modifier=modifier) {
        Spacer(Modifier.windowInsetsTopHeight(WindowInsets.statusBars))
        DrawerHeader()
        DividerItem()
        DrawerItemHeader(stringResource(R.string.text_novena_days))
        days.forEachIndexed { index, day ->
            NovenaItem(
                text = day,
                onOptionClicked = {
                    onOptionClick(NovenaAction.GoToDay(index + 1))
                    closeDrawer()
                },
                selected = false
            )
        }
        DividerItem(modifier = Modifier.padding(horizontal = 28.dp))
        DrawerItemHeader(stringResource(R.string.text_villancicos))
        VillancicoItem(
            selected = false,
            text = stringResource(R.string.text_lyrics),
            onOptionClicked = {
                onLyricsClick()
                closeDrawer()
            }
        )
    }
}

@Composable
private fun NovenaItem(text: String, selected: Boolean, onOptionClicked: () -> Unit) {
    val background = if (selected) {
        Modifier.background(MaterialTheme.colorScheme.primaryContainer)
    } else {
        Modifier
    }
    Row(
        modifier = Modifier
            .height(56.dp)
            .fillMaxWidth()
            .padding(horizontal = 12.dp)
            .clip(CircleShape)
            .then(background)
            .clickable(onClick = { onOptionClicked() }),
        verticalAlignment = CenterVertically
    ) {

        Image(
            painter = painterResource(id = R.drawable.icon_praying),
            contentDescription = null,
            modifier = Modifier
                .size(24.dp) // Tamaño del icono
                .clip(CircleShape)
        )
        Text(
            text,
            style = MaterialTheme.typography.bodyMedium,
            color = if (selected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurface
            },
            modifier = Modifier.padding(start = 12.dp)
        )
    }
}

@Composable
private fun VillancicoItem(text: String, selected: Boolean, onOptionClicked: () -> Unit) {
    val background = if (selected) {
        Modifier.background(MaterialTheme.colorScheme.primaryContainer)
    } else {
        Modifier
    }
    Row(
        modifier = Modifier
            .height(56.dp)
            .fillMaxWidth()
            .padding(horizontal = 12.dp)
            .clip(CircleShape)
            .then(background)
            .clickable(onClick = { onOptionClicked() }),
        verticalAlignment = CenterVertically
    ) {
        val iconTint = if (selected) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        }
        Icon(
            painter = painterResource(R.drawable.music_note_24px),
            tint = iconTint,
            modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 16.dp),
            contentDescription = null
        )
        Text(
            text,
            style = MaterialTheme.typography.bodyMedium,
            color = if (selected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurface
            },
            modifier = Modifier.padding(start = 12.dp)
        )
    }
}

@Composable
fun DrawerHeader(modifier: Modifier = Modifier) {
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.Start,
        modifier = modifier
            .background(MaterialTheme.colorScheme.background)
            .padding(dimensionResource(id = R.dimen.header_padding))
            .fillMaxWidth()
    ) {

        Image(
            painterResource(id = R.drawable.novena),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = modifier
                .size(dimensionResource(id = R.dimen.header_image_size))
                .clip(CircleShape)
        )
        Spacer(modifier = Modifier.padding(dimensionResource(id = R.dimen.spacer_padding)))


    }
}

@Composable
fun DividerItem(modifier: Modifier = Modifier) {
    HorizontalDivider(
        modifier = modifier,
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
    )
}

@Composable
private fun DrawerItemHeader(text: String) {
    Box(
        modifier = Modifier
            .heightIn(min = 52.dp)
            .padding(horizontal = 28.dp),
        contentAlignment = CenterStart
    ) {
        Text(
            text,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@PreviewAllPhones
@Composable
fun PreviewAppDrawer() {
    NovenAppTheme {

        /*AppDrawer(
            onLyricsClick = {},
            onOptionClick = {},
            closeDrawer = {}
        )*/
        AppDrawer(
            onLyricsClick = {},
            onOptionClick = {},
            closeDrawer = {}
        )
    }
}
