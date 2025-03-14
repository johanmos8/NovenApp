package com.mirkwood.novenapp.presentation.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.mirkwood.compose_preview.PreviewAllPhones
import com.mirkwood.novenapp.R
import com.mirkwood.novenapp.ui.theme.NovenAppTheme

@Composable
fun MainTitle() {
    val context = LocalContext.current
    Text(
        text = context.getString(R.string.novena_aguinaldos_title),
        style = MaterialTheme.typography.titleLarge,
        color = MaterialTheme.colorScheme.onBackground
    )
}

@PreviewAllPhones
@Composable
fun PreviewMainTitle() {
    NovenAppTheme {
        MainTitle()
    }
}