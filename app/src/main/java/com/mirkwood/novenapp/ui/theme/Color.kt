package com.mirkwood.novenapp.ui.theme

import androidx.compose.ui.graphics.Color

// Seed hues for the seasonal palette below.
val RedChristmas = Color(0xFFD32F2F)
val GreenChristmas = Color(0xFF388E3C)
val GoldChristmas = Color(0xFFFFD700)

/*
 * A red/green/gold palette used as the non-dynamic-color fallback (pre-Android 12,
 * or dynamic color disabled) instead of Material's generic purple baseline - this is
 * a Christmas novena app, not a generic app, so its default look should say so.
 * Container/on-container pairs are hand-tuned tonal variants of each seed hue,
 * following the same light-tint-container/dark-tint-on-container convention
 * Material's own scheme generator uses.
 */
val LightPrimary = RedChristmas
val LightOnPrimary = Color.White
val LightPrimaryContainer = Color(0xFFFFDAD6)
val LightOnPrimaryContainer = Color(0xFF410002)

val LightSecondary = GreenChristmas
val LightOnSecondary = Color.White
val LightSecondaryContainer = Color(0xFFC8F5C6)
val LightOnSecondaryContainer = Color(0xFF002204)

val LightTertiary = GoldChristmas
val LightOnTertiary = Color(0xFF3D2E00)
val LightTertiaryContainer = Color(0xFFFFE082)
val LightOnTertiaryContainer = Color(0xFF4A3B00)

val LightBackground = Color(0xFFFAFAFA)
val LightOnBackground = Color(0xFF212121)
val LightSurface = Color(0xFFF5F5F5)
val LightOnSurface = Color(0xFF616161)
val LightSurfaceVariant = Color(0xFFF0E0DE)
val LightOnSurfaceVariant = Color(0xFF52443F)

val DarkPrimary = Color(0xFFFFB4AB)
val DarkOnPrimary = Color(0xFF690005)
val DarkPrimaryContainer = Color(0xFF93000A)
val DarkOnPrimaryContainer = Color(0xFFFFDAD6)

val DarkSecondary = Color(0xFFA8D5A2)
val DarkOnSecondary = Color(0xFF00390A)
val DarkSecondaryContainer = Color(0xFF1E5128)
val DarkOnSecondaryContainer = Color(0xFFC8F5C6)

val DarkTertiary = Color(0xFFF5D67B)
val DarkOnTertiary = Color(0xFF3D2E00)
val DarkTertiaryContainer = Color(0xFF5C4700)
val DarkOnTertiaryContainer = Color(0xFFFFE082)

val DarkBackground = Color(0xFF121212)
val DarkOnBackground = Color(0xFFE0E0E0)
val DarkSurface = Color(0xFF1E1E1E)
val DarkOnSurface = Color(0xFFBDBDBD)
val DarkSurfaceVariant = Color(0xFF4A3934)
val DarkOnSurfaceVariant = Color(0xFFD8C2BC)
