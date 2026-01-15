package com.mirkwood.novenapp.presentation.screens.aboutus

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mirkwood.novenapp.R
import com.mirkwood.novenapp.presentation.components.AppInfoFooter
import com.mirkwood.novenapp.presentation.components.CopyRightFooter

@Composable
fun AboutUsScreen(
    modifier: Modifier,
) {

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        OutrageousCatSection()

        Spacer(modifier = Modifier.height(24.dp))

        AppDescription()
        Spacer(modifier = Modifier.height(16.dp))

    }
}

@Composable
fun OutrageousCatSection() {
    Column {
        Image(
            painter = painterResource(id = R.drawable.outrageous_cat_logo_no_bg),
            contentDescription = "",
            modifier = Modifier
                .height(90.dp)
                .align(Alignment.CenterHorizontally),
        )
        Text(
            modifier =
            Modifier
                .padding(top = 8.dp)
                .align(Alignment.CenterHorizontally),
            text = stringResource(R.string.about_us_outrageous_cat_section_title),
            fontSize = 32.sp,
            fontWeight = FontWeight.W300,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .align(Alignment.CenterHorizontally),
            text = stringResource(R.string.about_us_outrageous_cat_slogan),
            fontSize = 13.sp,
            fontWeight = FontWeight.W300,
            textAlign = TextAlign.Center,
            fontStyle = FontStyle.Italic
        )
        Text(
            modifier = Modifier
                .padding(16.dp)
                .align(Alignment.CenterHorizontally),
            text = stringResource(R.string.about_us_outrageous_cat_philosophy_text),
            fontSize = 15.sp,
            fontWeight = FontWeight.W300,
            textAlign = TextAlign.Justify,
            fontStyle = FontStyle.Italic
        )
    }
}

@Composable
fun AppDescription() {

    Column {


        Image(
            painter = painterResource(
                id = R.drawable.ic_logo
            ),
            contentDescription = "hola",
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
            text = stringResource(R.string.about_us_shuffle_friends_description_text),
            fontSize = 15.sp,
            fontWeight = FontWeight.W300,
            textAlign = TextAlign.Justify,
            fontStyle = FontStyle.Italic
        )

        Text(
            modifier = Modifier
                .padding(16.dp),
            text = stringResource(R.string.about_us_shuffle_friends_app_info_label),
            fontSize = 15.sp,
            fontWeight = FontWeight.W500,
            fontStyle = FontStyle.Italic
        )
        AppInfoFooter()
        //SocialMediaList() TODO
        CopyRightFooter()
    }
}

@Preview
@Composable
fun PreviewAboutUsScreen() {
    AboutUsScreen(
        modifier = Modifier.fillMaxSize(),
    )
}