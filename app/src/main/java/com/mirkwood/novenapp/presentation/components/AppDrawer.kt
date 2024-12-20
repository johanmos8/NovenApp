package com.mirkwood.novenapp.presentation.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.mirkwood.novenapp.R
import com.mirkwood.novenapp.presentation.NavigationScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppDrawer(
    route: String,
    modifier: Modifier = Modifier,
    navHostController: NavHostController,
    closeDrawer: () -> Unit = {}
) {
    val days = (1..9).map { "Día $it" } // Genera los títulos de los días

    ModalDrawerSheet(modifier = Modifier.fillMaxHeight()) {
        DrawerHeader(modifier)
        Spacer(modifier = Modifier.height(16.dp))
        days.forEachIndexed { index, day ->
            NavigationDrawerItem(
                label = {
                    Text(
                        text = day,
                        style = MaterialTheme.typography.labelSmall
                    )
                },
                selected = false,
                onClick = {
                    navHostController.navigate(NavigationScreen.DayScreen.createRoute(index + 1))
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
