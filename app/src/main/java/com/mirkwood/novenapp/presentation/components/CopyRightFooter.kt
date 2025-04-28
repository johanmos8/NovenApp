package com.mirkwood.novenapp.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.mirkwood.compose_preview.PreviewAllPhones
import com.mirkwood.novenapp.R
import com.mirkwood.novenapp.ui.theme.NovenAppTheme
import java.util.Calendar

@Composable
fun CopyRightFooter() {
    val currentYear = Calendar.getInstance().get(Calendar.YEAR)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            horizontalArrangement = Arrangement.Center, // Centrar en el Row
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "© $currentYear ${stringResource(R.string.about_us_outrageous_cat_section_title)}",
                style = MaterialTheme.typography.bodySmall
            )
            Spacer(modifier = Modifier.padding(horizontal = 8.dp))
            Text(
                text = "@johanmos8",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@PreviewAllPhones
@Composable
fun CopyRightFooterPreview() {
    NovenAppTheme {
        CopyRightFooter()
    }
}