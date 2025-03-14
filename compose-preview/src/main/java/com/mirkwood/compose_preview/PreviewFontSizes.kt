package com.mirkwood.compose_preview

import android.content.res.Configuration
import androidx.compose.ui.tooling.preview.Preview

@Preview(
    name = "Small Font",
    uiMode = Configuration.UI_MODE_NIGHT_NO,
    fontScale = .5f,
)
@Preview(
    name = "Large Font",
    uiMode = Configuration.UI_MODE_NIGHT_NO,
    fontScale = 2f,
)
@Target(
    AnnotationTarget.ANNOTATION_CLASS,
    AnnotationTarget.FUNCTION,
)
annotation class PreviewFontSizes
