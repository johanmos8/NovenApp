package com.mirkwood.novenapp.presentation.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mirkwood.compose_preview.PreviewAllPhones
import com.mirkwood.novenapp.R
import com.mirkwood.novenapp.presentation.model.SocialMedia
import com.mirkwood.novenapp.ui.theme.NovenAppTheme

@Composable
fun SocialMediaList() {
    val uriHandler = LocalUriHandler.current  // Maneja los enlaces en Compose

    Column {
        Text(
            modifier = Modifier.align(Alignment.CenterHorizontally),
            text = stringResource(R.string.text_follow_us),
            fontSize = 13.sp,
            fontWeight = FontWeight.W300,
            textAlign = TextAlign.Center,
            fontStyle = FontStyle.Italic
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally)
        ) {
            SocialMedia.all.forEach { social ->
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(
                            shape = RoundedCornerShape(12.dp)
                        )
                        .shadow(elevation = 4.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .clickable { uriHandler.openUri(social.url) },  // Abre el enlace
                    contentAlignment = Alignment.Center // 🔹 Centra el contenido dentro del Box

                ) {

                    Icon(
                        painter = painterResource(id = social.icon),
                        contentDescription = social.name,
                        tint = MaterialTheme.colorScheme.onSurface, // 🔹 Se adapta al tema
                        modifier = Modifier
                            .size(24.dp)

                    )
                }

            }
        }
    }

}

@PreviewAllPhones
@Composable
fun PreviewSocialMediaList() {
    NovenAppTheme {
        Surface(
            modifier = Modifier.background(Color.White)
        ) {

            SocialMediaList()
        }
    }
}
