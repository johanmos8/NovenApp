package com.mirkwood.novenapp.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mirkwood.novenapp.R
import com.mirkwood.novenapp.presentation.model.Gozo
import com.mirkwood.novenapp.presentation.model.MainModule


@Composable
internal fun GozosScreen(
    gozos: List<Gozo>,
    image: MainModule.Hero
) {
    val ctx = LocalContext.current
    Column(
        modifier = Modifier
            .fillMaxSize(),
    ) {
        HeroImage(
            header = image,
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .offset(y = (-48).dp)
                .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {

                    Text(
                        text = stringResource(R.string.gozos_title),
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.headlineLarge
                    )
                    IconView()
                }
                GozosNavigator(gozos)
            }
        }
    }
}
