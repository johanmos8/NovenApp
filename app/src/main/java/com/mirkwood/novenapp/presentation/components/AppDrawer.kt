package com.mirkwood.novenapp.presentation.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.mirkwood.compose_preview.PreviewAllPhones
import com.mirkwood.novenapp.R
import com.mirkwood.novenapp.presentation.NovenaAction
import com.mirkwood.novenapp.ui.theme.NovenAppTheme

@Composable
internal fun AppDrawer(
    modifier: Modifier = Modifier,
    onOptionClick: (NovenaAction) -> Unit,
    closeDrawer: () -> Unit = {}
) {
    val context = LocalContext.current
    val days =
        (1..9).map { context.getString(R.string.text_dia, it) } // Genera los títulos de los días

    ModalDrawerSheet(
        modifier = Modifier
            .fillMaxHeight()
            .wrapContentWidth()
    ) {
        DrawerHeader(modifier)
        Spacer(modifier = Modifier.height(16.dp))
        days.forEachIndexed { index, day ->
            NavigationDrawerItem(
                label = {
                    Text(
                        text = day,
                        style = MaterialTheme.typography.labelLarge
                    )
                },
                selected = false,
                onClick = {
                    //navHostController.navigate(NavigationScreen.DayScreen.createRoute(index + 1))
                    onOptionClick(NovenaAction.GoToDay(index + 1))
                    closeDrawer()
                },
                icon = {
                    Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = null
                    )
                },
                shape = MaterialTheme.shapes.small
            )
        }
    }
}


@Composable
fun DrawerHeader(modifier: Modifier) {
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

@PreviewAllPhones
@Composable
fun PreviewAppDrawer() {
    NovenAppTheme {

        AppDrawer(
            onOptionClick = {},
            closeDrawer = {}
        )
    }
}
