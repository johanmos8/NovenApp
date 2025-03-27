package com.mirkwood.novenapp.presentation.components

import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.mirkwood.compose_preview.PreviewAllPhones
import com.mirkwood.novenapp.R
import com.mirkwood.novenapp.presentation.NovenaAction
import com.mirkwood.novenapp.ui.theme.NovenAppTheme

@Composable
internal fun GoToDayButton(
    currentDay: Int,
    onAction: (NovenaAction) -> Unit,
) {
    Button(onClick = { onAction(NovenaAction.GoToDay(currentDay)) }

    ) {
        Text(
            text = stringResource(R.string.text_ir_a, currentDay.toString()),
            color = MaterialTheme.colorScheme.background
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
