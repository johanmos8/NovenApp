package com.mirkwood.novenapp.presentation.components

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.mirkwood.novenapp.R

/** Bounds and step for the in-screen reading-text size control, shared by prayers and carols. */
internal const val MIN_TEXT_SCALE = 0.85f
internal const val MAX_TEXT_SCALE = 1.5f
internal const val TEXT_SCALE_STEP = 0.15f

/**
 * "Aa" button opening A-/A+ as a menu (specs/prayer-toolbar-single-row). Picking an item
 * doesn't close the menu, so the user can step through several sizes while watching the
 * text change behind it; an outside tap or back closes it. Used by the day screen's
 * toolbar and the lyrics app bar.
 */
@Composable
internal fun TextSizeMenu(
    canDecreaseText: Boolean,
    canIncreaseText: Boolean,
    onDecreaseText: () -> Unit,
    onIncreaseText: () -> Unit
) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    val decreaseLabel = stringResource(R.string.content_description_decrease_text_size)
    val increaseLabel = stringResource(R.string.content_description_increase_text_size)
    val menuLabel = stringResource(R.string.content_description_text_size)

    Box {
        IconButton(
            onClick = { expanded = true },
            modifier = Modifier.semantics { contentDescription = menuLabel }
        ) {
            Text(text = "Aa", style = MaterialTheme.typography.titleMedium)
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(
                text = { Text(text = "A−", style = MaterialTheme.typography.titleMedium) },
                onClick = onDecreaseText,
                enabled = canDecreaseText,
                modifier = Modifier.semantics { contentDescription = decreaseLabel }
            )
            DropdownMenuItem(
                text = { Text(text = "A+", style = MaterialTheme.typography.titleLarge) },
                onClick = onIncreaseText,
                enabled = canIncreaseText,
                modifier = Modifier.semantics { contentDescription = increaseLabel }
            )
        }
    }
}
