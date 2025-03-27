package com.mirkwood.novenapp.presentation.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.mirkwood.compose_preview.PreviewAllPhones
import com.mirkwood.novenapp.R
import com.mirkwood.novenapp.presentation.model.MainModule
import com.mirkwood.novenapp.ui.theme.NovenAppTheme

@Composable
internal fun HeroImage(
    header: MainModule.Hero,
    modifier: Modifier = Modifier
) {
    Image(
        modifier = modifier
            .fillMaxWidth(),
        painter = painterResource(id = header.imageResId),
        contentDescription = null,
        contentScale = ContentScale.Crop,
    )
}

@PreviewAllPhones
@Composable
fun PreviewMainImage() {
    NovenAppTheme {
        HeroImage(
            MainModule.Hero(
                imageResId = R.drawable.virgen_maria,
                height = MainModule.Hero.Height.Medium
            )
        )
    }
}
