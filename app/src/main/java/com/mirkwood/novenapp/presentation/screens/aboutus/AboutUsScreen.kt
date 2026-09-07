package com.mirkwood.novenapp.presentation.screens.aboutus

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import com.mirkwood.compose_preview.PreviewAllPhones
import com.mirkwood.novenapp.ui.theme.NovenAppTheme
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mirkwood.novenapp.R
import com.mirkwood.novenapp.presentation.components.AppInfoFooter
import com.mirkwood.novenapp.presentation.components.CopyRightFooter

/**
 * NovenApp's own branding leads the screen; the studio that made it ([MadeBySection])
 * follows as a small credit instead of a second, equally-sized "about" section - the
 * two used to read as two competing apps' about pages stapled together.
 */
@Composable
fun AboutUsScreen(
    modifier: Modifier,
) {

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        AppDescription()

        Spacer(modifier = Modifier.height(24.dp))

        MadeBySection()

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            modifier = Modifier
                .padding(16.dp)
                .align(Alignment.CenterHorizontally),
            text = stringResource(R.string.about_us_app_info_label),
            fontSize = 15.sp,
            fontWeight = FontWeight.W500,
            fontStyle = FontStyle.Italic
        )
        AppInfoFooter()
        CopyRightFooter()
    }
}

@Composable
fun AppDescription() {

    Column {

        Image(
            painter = painterResource(
                id = R.drawable.ic_logo
            ),
            contentDescription = stringResource(R.string.content_description_app_logo),
            modifier = Modifier
                .height(90.dp)
                .align(Alignment.CenterHorizontally),
        )
        Text(
            modifier =
            Modifier
                .padding(top = 8.dp)
                .align(Alignment.CenterHorizontally),
            text = stringResource(R.string.app_name),
            fontSize = 32.sp,
            fontWeight = FontWeight.W300,
        )

        Text(
            modifier = Modifier
                .padding(16.dp)
                .align(Alignment.CenterHorizontally),
            text = stringResource(R.string.about_us_app_description_text),
            fontSize = 15.sp,
            fontWeight = FontWeight.W300,
            textAlign = TextAlign.Justify,
            fontStyle = FontStyle.Italic
        )
    }
}

@Composable
fun MadeBySection() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        HorizontalDivider(modifier = Modifier.padding(bottom = 16.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = painterResource(id = R.drawable.outrageous_cat_logo_no_bg),
                contentDescription = stringResource(R.string.content_description_studio_logo),
                modifier = Modifier.height(32.dp),
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = stringResource(R.string.about_us_made_by_label),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = stringResource(R.string.about_us_outrageous_cat_section_title),
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold
            )
        }
        Text(
            modifier = Modifier.padding(top = 8.dp),
            text = stringResource(R.string.about_us_outrageous_cat_slogan),
            style = MaterialTheme.typography.labelSmall,
            textAlign = TextAlign.Center,
            fontStyle = FontStyle.Italic,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            modifier = Modifier.padding(top = 8.dp),
            text = stringResource(R.string.about_us_outrageous_cat_philosophy_text),
            style = MaterialTheme.typography.labelSmall,
            textAlign = TextAlign.Justify,
            fontStyle = FontStyle.Italic,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@PreviewAllPhones
@Composable
fun PreviewAboutUsScreen() {
    NovenAppTheme {
        AboutUsScreen(
            modifier = Modifier.fillMaxSize(),
        )
    }
}
