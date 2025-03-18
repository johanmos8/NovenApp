package com.mirkwood.novenapp.presentation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.mirkwood.novenapp.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun NovenaTopAppBar(
    drawerAction: () -> Unit,
) {
    TopAppBar(
        title = {},
        navigationIcon = {

            IconButton(onClick = { drawerAction() }) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = stringResource(R.string.text_ir_a_inicio)
                )
            }
        }
    )
}
