package com.mirkwood.novenapp.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp

/*
 * "Novena Contemplative MD3" - a warm porcelain/candlelight palette: deep indigo for
 * structure, liturgical amber for active/completed states, twilight violet for
 * meditative accents. The light non-container values are literals from that spec.
 *
 * The container/on-container pairs are not: the spec's containers were saturated
 * mid-tones (#3F51B5, #FE932C, #73498B), which made every tonal component (chips, pills,
 * badges, tonal buttons, the nav indicator) compete with the primary action. They're
 * regenerated as standard M3 tonal-palette tones from the same seeds with Material Color
 * Utilities - light tone 90/10, dark tone 30/90 (specs/palette-tonal-containers).
 */

// region Light - spec literals, plus generated containers
val LightPrimary = Color(0xFF24389C)
val LightOnPrimary = Color(0xFFFFFFFF)
val LightPrimaryContainer = Color(0xFFDEE0FF)
val LightOnPrimaryContainer = Color(0xFF00105B)
val LightInversePrimary = Color(0xFFBAC3FF)

val LightSecondary = Color(0xFF904D00)
val LightOnSecondary = Color(0xFFFFFFFF)
val LightSecondaryContainer = Color(0xFFFFDCC3)
val LightOnSecondaryContainer = Color(0xFF2F1500)

val LightTertiary = Color(0xFF593172)
val LightOnTertiary = Color(0xFFFFFFFF)
val LightTertiaryContainer = Color(0xFFF4D9FF)
val LightOnTertiaryContainer = Color(0xFF2E0348)

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

// The original spec's saturated containers. No longer containers, but still the source
// the dark theme's primary/secondary/tertiary roles were derived from, so dark mode's
// non-container roles stay exactly as they were.
private val SpecPrimaryAccent = Color(0xFF3F51B5)
private val SpecSecondaryAccent = Color(0xFFFE932C)
private val SpecTertiaryAccent = Color(0xFF73498B)

/**
 * The palette's vivid candlelight orange, kept only as a deliberate accent (the candle
 * on the day screen's progress pill) - never as a surface or text color. Same value in
 * both themes.
 */
val CandleAccent = SpecSecondaryAccent
// endregion

// region Dark - derived from the light spec (not part of the original design), except
// the container pairs, which are generated tones like the light ones
val DarkPrimary = lerp(SpecPrimaryAccent, Color.White, 0.35f)
val DarkOnPrimary = lerp(SpecPrimaryAccent, Color.Black, 0.55f)
val DarkPrimaryContainer = Color(0xFF283CA0)
val DarkOnPrimaryContainer = Color(0xFFDEE0FF)
val DarkInversePrimary = SpecPrimaryAccent

val DarkSecondary = lerp(SpecSecondaryAccent, Color.White, 0.25f)
val DarkOnSecondary = lerp(SpecSecondaryAccent, Color.Black, 0.65f)
val DarkSecondaryContainer = Color(0xFF6E3900)
val DarkOnSecondaryContainer = Color(0xFFFFDCC3)

// Generated tone 80, not lerp-derived: tertiary is used as text (Gozos numbers), and the
// old derived value only reached 3.8:1 on the dark surface; this is 7.8:1.
val DarkTertiary = Color(0xFFE5B5FF)
val DarkOnTertiary = lerp(SpecTertiaryAccent, Color.Black, 0.55f)
val DarkTertiaryContainer = Color(0xFF5D3576)
val DarkOnTertiaryContainer = Color(0xFFF4D9FF)

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
