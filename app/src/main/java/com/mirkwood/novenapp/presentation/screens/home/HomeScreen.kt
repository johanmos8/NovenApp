package com.mirkwood.novenapp.presentation.screens.home

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.mirkwood.compose_preview.PreviewAllPhones
import com.mirkwood.novenapp.R
import com.mirkwood.novenapp.presentation.NovenaAction
import com.mirkwood.novenapp.presentation.components.DayStrip
import com.mirkwood.novenapp.presentation.model.devotional.DevotionalCatalog
import com.mirkwood.novenapp.presentation.model.devotional.DevotionalMeta
import com.mirkwood.novenapp.presentation.state.NovenaViewState
import com.mirkwood.novenapp.presentation.util.Util
import com.mirkwood.novenapp.ui.theme.NovenAppTheme

/**
 * The "Camino" tab. A vertically scrolling feed rather than a single centered CTA:
 * the [JourneyCard] is the hero entry point into the daily prayer, and further cards
 * (other sections of interest) can be appended to the same column later.
 * See `docs/home-journey-redesign.md`.
 */
@Composable
internal fun HomeScreen(
    viewState: NovenaViewState,
    devotional: DevotionalMeta,
    completedDays: Set<Int> = emptySet(),
    onEvent: (NovenaAction) -> Unit
) {
    RequestNotificationPermissionIfNeeded()
    val isChristmas = remember(devotional) { Util.isCelebrationDay(devotional.schedule) }

    Box(modifier = Modifier.fillMaxSize()) {
        if (isChristmas) {
            CelebrationAnimation()
            return@Box
        }

        LottieAnimationRender(R.raw.animation_snow_falling)

        val journeyStatus = remember(devotional, viewState.currentDay, completedDays) {
            resolveJourneyStatus(devotional, viewState.currentDay, completedDays)
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            JourneyCard(
                devotionalTitle = stringResource(devotional.titleResId),
                status = journeyStatus,
                onContinue = { day -> onEvent(NovenaAction.GoToDay(day)) },
                onPreview = { onEvent(NovenaAction.GoToDay(1)) }
            )

            Text(
                text = stringResource(R.string.journey_all_days_label),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(top = 8.dp, start = 4.dp)
            )
            DayStrip(
                totalDays = devotional.totalDays,
                currentDay = viewState.currentDay,
                completedDays = completedDays,
                onDaySelected = { day -> onEvent(NovenaAction.GoToDay(day)) },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
internal fun CelebrationAnimation() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center // Centra el letrero
    ) {
        // Animación de globos en diferentes posiciones
        LottieAnimationRender(R.raw.animation_celebration, Modifier.offset(x = 50.dp, y = 100.dp))

        // Animación del letrero en el centro
        LottieAnimationRender(R.raw.animation_merry)
    }
}


/**
 * Requesting this is what makes the daily reminder in [com.mirkwood.novenapp.reminder.NovenaNotifier]
 * actually visible on Android 13+ - without it the notification is silently
 * swallowed. Asked once per cold start; Android itself rate-limits the system
 * dialog after a couple of denials, so this never turns into a nag.
 */
@Composable
private fun RequestNotificationPermissionIfNeeded() {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return

    val context = LocalContext.current
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = {}
    )

    LaunchedEffect(Unit) {
        val granted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
        if (!granted) {
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}

@Composable
internal fun LottieAnimationRender(animationRes: Int, modifier: Modifier = Modifier) {
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(animationRes))
    LottieAnimation(
        composition = composition,
        iterations = LottieConstants.IterateForever, // Para que sea infinita
        modifier = Modifier.fillMaxSize() // Ajusta el tamaño según necesites
    )
}

@PreviewAllPhones
@Composable
private fun PreviewHomeScreen() {
    NovenAppTheme {
        HomeScreen(
            viewState = NovenaViewState(currentDay = 4),
            devotional = DevotionalCatalog.default,
            completedDays = setOf(1, 2, 3),
            onEvent = {}
        )
    }
}

@PreviewAllPhones
@Composable
private fun PreviewHomeScreenOffSeason() {
    NovenAppTheme {
        HomeScreen(
            viewState = NovenaViewState(currentDay = null),
            devotional = DevotionalCatalog.default,
            onEvent = {}
        )
    }
}
