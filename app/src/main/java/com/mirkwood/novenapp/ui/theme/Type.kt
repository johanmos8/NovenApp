@file:OptIn(ExperimentalTextApi::class)

package com.mirkwood.novenapp.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.mirkwood.novenapp.R

// Both are single-file variable fonts (Google Fonts ships no static weight instances for
// either), so each weight below points at the same font resource with a different 'wght'
// axis setting rather than a separate file.
private val NotoSerif = FontFamily(
    Font(R.font.noto_serif_variable, weight = FontWeight.Normal, variationSettings = FontVariation.Settings(FontVariation.weight(400))),
    Font(R.font.noto_serif_variable, weight = FontWeight.Medium, variationSettings = FontVariation.Settings(FontVariation.weight(500))),
    Font(R.font.noto_serif_variable, weight = FontWeight.Bold, variationSettings = FontVariation.Settings(FontVariation.weight(700)))
)

private val Manrope = FontFamily(
    Font(R.font.manrope_variable, weight = FontWeight.Normal, variationSettings = FontVariation.Settings(FontVariation.weight(400))),
    Font(R.font.manrope_variable, weight = FontWeight.Medium, variationSettings = FontVariation.Settings(FontVariation.weight(500))),
    Font(R.font.manrope_variable, weight = FontWeight.SemiBold, variationSettings = FontVariation.Settings(FontVariation.weight(600))),
    Font(R.font.manrope_variable, weight = FontWeight.Bold, variationSettings = FontVariation.Settings(FontVariation.weight(700)))
)

private val DefaultTypography = Typography()

/*
 * "Novena Contemplative MD3" type scale: Noto Serif for display/headline (literary
 * gravitas for titles and invocations), Manrope for title/body/label (ergonomic reading
 * comfort). Sizes/weights/line-heights/tracking are the spec's literal values; the two
 * roles it doesn't define (displaySmall, titleSmall) are interpolated between their
 * neighbors, keeping the spec's own default tracking for any field it left unset.
 */
val Typography = DefaultTypography.copy(
    displayLarge = DefaultTypography.displayLarge.copy(
        fontFamily = NotoSerif, fontWeight = FontWeight.Normal,
        fontSize = 48.sp, lineHeight = 56.sp, letterSpacing = (-0.25).sp
    ),
    displayMedium = DefaultTypography.displayMedium.copy(
        fontFamily = NotoSerif, fontWeight = FontWeight.Normal,
        fontSize = 40.sp, lineHeight = 48.sp
    ),
    displaySmall = DefaultTypography.displaySmall.copy(
        fontFamily = NotoSerif, fontWeight = FontWeight.Normal,
        fontSize = 34.sp, lineHeight = 42.sp
    ),
    headlineLarge = DefaultTypography.headlineLarge.copy(
        fontFamily = NotoSerif, fontWeight = FontWeight.Medium,
        fontSize = 32.sp, lineHeight = 40.sp
    ),
    headlineMedium = DefaultTypography.headlineMedium.copy(
        fontFamily = NotoSerif, fontWeight = FontWeight.Medium,
        fontSize = 24.sp, lineHeight = 32.sp
    ),
    headlineSmall = DefaultTypography.headlineSmall.copy(
        fontFamily = NotoSerif, fontWeight = FontWeight.Medium,
        fontSize = 20.sp, lineHeight = 28.sp
    ),
    titleLarge = DefaultTypography.titleLarge.copy(
        fontFamily = Manrope, fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp, lineHeight = 26.sp, letterSpacing = 0.1.sp
    ),
    titleMedium = DefaultTypography.titleMedium.copy(
        fontFamily = Manrope, fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp, lineHeight = 24.sp, letterSpacing = 0.15.sp
    ),
    titleSmall = DefaultTypography.titleSmall.copy(
        fontFamily = Manrope, fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp, lineHeight = 20.sp
    ),
    bodyLarge = DefaultTypography.bodyLarge.copy(
        fontFamily = Manrope, fontWeight = FontWeight.Normal,
        fontSize = 17.sp, lineHeight = 28.sp, letterSpacing = 0.25.sp
    ),
    bodyMedium = DefaultTypography.bodyMedium.copy(
        fontFamily = Manrope, fontWeight = FontWeight.Normal,
        fontSize = 15.sp, lineHeight = 24.sp, letterSpacing = 0.25.sp
    ),
    bodySmall = DefaultTypography.bodySmall.copy(
        fontFamily = Manrope, fontWeight = FontWeight.Normal,
        fontSize = 13.sp, lineHeight = 18.sp
    ),
    labelLarge = DefaultTypography.labelLarge.copy(
        fontFamily = Manrope, fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = 0.1.sp
    ),
    labelMedium = DefaultTypography.labelMedium.copy(
        fontFamily = Manrope, fontWeight = FontWeight.SemiBold,
        fontSize = 12.sp, lineHeight = 16.sp, letterSpacing = 0.5.sp
    ),
    labelSmall = DefaultTypography.labelSmall.copy(
        fontFamily = Manrope, fontWeight = FontWeight.Medium,
        fontSize = 11.sp, lineHeight = 16.sp, letterSpacing = 0.5.sp
    )
)
