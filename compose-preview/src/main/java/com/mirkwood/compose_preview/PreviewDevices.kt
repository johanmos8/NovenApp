package com.mirkwood.compose_preview

import android.content.res.Configuration
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview

@Preview(
    name = "Tablet",
    uiMode = Configuration.UI_MODE_NIGHT_NO,
    device = "spec:width=1280dp,height=800dp,dpi=240"
)
@Preview(
    name = "Narrow Phone",
    uiMode = Configuration.UI_MODE_NIGHT_NO,
    device = Devices.PIXEL_4,
    widthDp = 325
)
@Target(
    AnnotationTarget.ANNOTATION_CLASS,
    AnnotationTarget.FUNCTION,
)
annotation class PreviewDevices
