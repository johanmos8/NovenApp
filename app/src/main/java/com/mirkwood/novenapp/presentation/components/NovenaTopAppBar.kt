package com.mirkwood.novenapp.presentation.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.mirkwood.novenapp.R
import com.mirkwood.novenapp.presentation.NovenaAction

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun NovenaTopAppBar(
    drawerAction: () -> Unit,
    onHomeClick: (NovenaAction) -> Unit
) {
    val context = LocalContext.current
    TopAppBar(
        title = {},//{ Text(text = context.getString(R.string.novena_aguinaldos_title)) },
        navigationIcon = {

            IconButton(onClick = { drawerAction() }) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = context.getString(R.string.text_ir_a_inicio)
                )
            }
        }
    )
}
