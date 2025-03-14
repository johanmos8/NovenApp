package com.mirkwood.novenapp.presentation.components

import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.mirkwood.compose_preview.PreviewAllPhones
import com.mirkwood.novenapp.R
import com.mirkwood.novenapp.presentation.NovenaAction
import com.mirkwood.novenapp.ui.theme.NovenAppTheme

@Composable
internal fun GoToDayButton(
    currentDay: Int,
    onAction: (NovenaAction) -> Unit,
) {
    val context = LocalContext.current
    Button(onClick = { onAction(NovenaAction.GoToDay(currentDay)) }

    ) {
        Text(
            text = context.getString(R.string.text_ir_a, currentDay.toString()),
            color = MaterialTheme.colorScheme.onBackground
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