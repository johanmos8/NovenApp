package com.mirkwood.novenapp.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp

/*
 * "Novena Contemplative MD3" - a warm porcelain/candlelight palette: deep indigo for
 * structure, liturgical amber for active/completed states, twilight violet for
 * meditative accents. Every light-theme value below is a literal from that spec, not
 * derived - only the dark theme (which the spec doesn't define) is computed from it.
 */

// region Light - literal values from the design spec
val LightPrimary = Color(0xFF24389C)
val LightOnPrimary = Color(0xFFFFFFFF)
val LightPrimaryContainer = Color(0xFF3F51B5)
val LightOnPrimaryContainer = Color(0xFFCACFFF)
val LightInversePrimary = Color(0xFFBAC3FF)

val LightSecondary = Color(0xFF904D00)
val LightOnSecondary = Color(0xFFFFFFFF)
val LightSecondaryContainer = Color(0xFFFE932C)
val LightOnSecondaryContainer = Color(0xFF663500)

val LightTertiary = Color(0xFF593172)
val LightOnTertiary = Color(0xFFFFFFFF)
val LightTertiaryContainer = Color(0xFF73498B)
val LightOnTertiaryContainer = Color(0xFFECC4FF)

val LightError = Color(0xFFBA1A1A)
val LightOnError = Color(0xFFFFFFFF)
val LightErrorContainer = Color(0xFFFFDAD6)
val LightOnErrorContainer = Color(0xFF93000A)

val LightBackground = Color(0xFFFAF8FF)
val LightOnBackground = Color(0xFF1A1B21)
val LightSurface = Color(0xFFFAF8FF)
val LightOnSurface = Color(0xFF1A1B21)
val LightSurfaceVariant = Color(0xFFE2E2EA)
val LightOnSurfaceVariant = Color(0xFF454652)
val LightSurfaceTint = Color(0xFF4355B9)
val LightSurfaceDim = Color(0xFFDAD9E1)
val LightSurfaceBright = Color(0xFFFAF8FF)
val LightSurfaceContainerLowest = Color(0xFFFFFFFF)
val LightSurfaceContainerLow = Color(0xFFF3F3FB)
val LightSurfaceContainer = Color(0xFFEEEDF5)
val LightSurfaceContainerHigh = Color(0xFFE8E7F0)
val LightSurfaceContainerHighest = Color(0xFFE2E2EA)
val LightInverseSurface = Color(0xFF2F3036)
val LightInverseOnSurface = Color(0xFFF1F0F8)
val LightOutline = Color(0xFF757684)
val LightOutlineVariant = Color(0xFFC5C5D4)

// The spec's "fixed" roles (same tone in both themes) aren't settable through this
// Material3 version's ColorScheme, but their hexes are still useful raw material below
// for deriving readable dark on-container text without inventing new tones.
private val SecondaryFixed = Color(0xFFFFDCC3)
// endregion

// region Dark - derived from the light spec (not part of the original design)
val DarkPrimary = lerp(LightPrimaryContainer, Color.White, 0.35f)
val DarkOnPrimary = lerp(LightPrimaryContainer, Color.Black, 0.55f)
val DarkPrimaryContainer = lerp(LightPrimary, Color.Black, 0.35f)
val DarkOnPrimaryContainer = LightOnPrimaryContainer
val DarkInversePrimary = LightPrimaryContainer

val DarkSecondary = lerp(LightSecondaryContainer, Color.White, 0.25f)
val DarkOnSecondary = lerp(LightSecondaryContainer, Color.Black, 0.65f)
val DarkSecondaryContainer = lerp(LightSecondary, Color.Black, 0.25f)
val DarkOnSecondaryContainer = SecondaryFixed

val DarkTertiary = lerp(LightTertiaryContainer, Color.White, 0.30f)
val DarkOnTertiary = lerp(LightTertiaryContainer, Color.Black, 0.55f)
val DarkTertiaryContainer = lerp(LightTertiary, Color.Black, 0.30f)
val DarkOnTertiaryContainer = LightOnTertiaryContainer

// Material's own baseline dark error tones - the spec's light error values already are
// Material's baseline light error tones, so the dark side follows the same baseline.
val DarkError = Color(0xFFFFB4AB)
val DarkOnError = Color(0xFF690005)
val DarkErrorContainer = Color(0xFF93000A)
val DarkOnErrorContainer = Color(0xFFFFDAD6)

// The spec's own inverse-surface/inverse-on-surface are, by M3 convention, what the
// opposite theme's surface/onSurface look like - so dark mode's actual surface pair
// reuses those instead of re-deriving new ones, and its inverse pair reuses light's
// real surface/onSurface right back.
val DarkBackground = LightInverseSurface
val DarkOnBackground = LightInverseOnSurface
val DarkSurface = LightInverseSurface
val DarkOnSurface = LightInverseOnSurface
val DarkSurfaceVariant = lerp(LightOnSurfaceVariant, Color.Black, 0.35f)
val DarkOnSurfaceVariant = lerp(LightOnSurfaceVariant, Color.White, 0.55f)
val DarkSurfaceTint = LightSurfaceTint
val DarkSurfaceDim = lerp(DarkSurface, Color.Black, 0.15f)
val DarkSurfaceBright = lerp(DarkSurface, Color.White, 0.15f)
val DarkSurfaceContainerLowest = lerp(DarkSurface, Color.Black, 0.10f)
val DarkSurfaceContainerLow = lerp(DarkSurface, Color.White, 0.04f)
val DarkSurfaceContainer = lerp(DarkSurface, Color.White, 0.08f)
val DarkSurfaceContainerHigh = lerp(DarkSurface, Color.White, 0.12f)
val DarkSurfaceContainerHighest = lerp(DarkSurface, Color.White, 0.16f)
val DarkInverseSurface = LightSurface
val DarkInverseOnSurface = LightOnSurface
val DarkOutline = lerp(LightOutline, Color.White, 0.25f)
val DarkOutlineVariant = lerp(LightOutlineVariant, Color.Black, 0.55f)
// endregion
