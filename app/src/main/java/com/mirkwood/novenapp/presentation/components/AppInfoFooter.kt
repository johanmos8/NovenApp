package com.mirkwood.novenapp.presentation.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.mirkwood.compose_preview.PreviewAllPhones
import com.mirkwood.novenapp.BuildConfig
import com.mirkwood.novenapp.R
import com.mirkwood.novenapp.ui.theme.NovenAppTheme

@Composable
fun AppInfoFooter() {
    val appVersion = BuildConfig.VERSION_NAME


    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.app_name)
            )
            Text(
                text = "Versión: $appVersion"
            )
        }
    }
}

@PreviewAllPhones
@Composable
fun AppInfoFooterPreview() {
    NovenAppTheme {
        AppInfoFooter()
    }
}