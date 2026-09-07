package com.mirkwood.novenapp.presentation.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.mirkwood.compose_preview.PreviewAllPhones
import com.mirkwood.novenapp.R
import com.mirkwood.novenapp.presentation.NovenaAction
import com.mirkwood.novenapp.ui.theme.NovenAppTheme

@Composable
internal fun GoToDayButton(
    currentDay: Int,
    onAction: (NovenaAction) -> Unit,
) {
    Button(
        onClick = { onAction(NovenaAction.GoToDay(currentDay)) },
        contentPadding = PaddingValues(horizontal = 32.dp, vertical = 16.dp)
    ) {
        Text(
            text = stringResource(R.string.text_ir_a, currentDay.toString()),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onPrimary
        )
    }

}

@PreviewAllPhones
@Composable
fun PreviewGoToDayButton() {
    NovenAppTheme {

        GoToDayButton(1,
            {}
        )
    }
}
