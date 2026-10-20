package com.mirkwood.novenapp.presentation.screens.home

import android.Manifest
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleResumeEffect
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.rememberLottieAnimatable
import com.airbnb.lottie.compose.rememberLottieComposition
import com.mirkwood.compose_preview.PreviewAllPhones
import com.mirkwood.novenapp.R
import com.mirkwood.novenapp.presentation.DevotionalHomeItem
import com.mirkwood.novenapp.presentation.NovenaAction
import com.mirkwood.novenapp.presentation.model.devotional.DevotionalCatalog
import com.mirkwood.novenapp.presentation.notifications.areNotificationsEnabled
import com.mirkwood.novenapp.presentation.notifications.needsNotificationPermission
import com.mirkwood.novenapp.presentation.util.Util
import com.mirkwood.novenapp.ui.theme.NovenAppTheme
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

/**
 * The "Camino" tab: a feed of every devotional currently relevant (an in-window
 * novena, or any evergreen one) - not a single hardcoded card. Long-pressing a card
 * toggles its notifications on/off directly (no menu - owner's explicit choice, see
 * specs/devotional-catalog-selection §4.2), confirmed with haptic feedback, a Toast
 * (owner's preference over a Snackbar), and the animated badge on the card itself.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun HomeScreen(
    devotionals: List<DevotionalHomeItem>,
    onEvent: (NovenaAction) -> Unit,
    onToggleNotification: (devotionalId: String, isCurrentlyNotifying: Boolean) -> Unit,
    // Defaults make previews (and any caller that doesn't wire them) never auto-prompt.
    hasAutoPromptedPermission: () -> Boolean = { true },
    onAutoPromptedPermission: () -> Unit = {},
    celebrationDismissed: Boolean = false,
    onDismissCelebration: () -> Unit = {}
) {
    val isCelebrationDay = devotionals.any { Util.isCelebrationDay(it.devotional.schedule) }
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val enabledMessage = stringResource(R.string.devotional_notifications_enabled)
    val disabledMessage = stringResource(R.string.devotional_notifications_disabled)
    val blockedMessage = stringResource(R.string.devotional_notifications_blocked)

    // Title of the devotional whose long-press activation is waiting on the system
    // permission dialog, so its confirmation Toast can be shown - truthfully - only
    // once the dialog's result is known (P1-5 in specs/code-audit-v3). Saveable so a
    // rotation while the dialog is up doesn't lose it. Null for the automatic prompt
    // below, which has no Toast of its own.
    var pendingActivationTitle by rememberSaveable { mutableStateOf<String?>(null) }

    // Drives the blocked-bell next to each card's "Active" badge. Re-checked on every
    // resume, since the usual way to fix it is leaving for system Settings and coming
    // back (the permission dialog itself also pauses/resumes this Activity).
    var notificationsBlocked by remember { mutableStateOf(!areNotificationsEnabled(context)) }
    LifecycleResumeEffect(Unit) {
        notificationsBlocked = !areNotificationsEnabled(context)
        onPauseOrDispose { }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) {
        notificationsBlocked = !areNotificationsEnabled(context)
        pendingActivationTitle?.let { title ->
            val message = if (areNotificationsEnabled(context)) enabledMessage else blockedMessage
            Toast.makeText(context, message.format(title), Toast.LENGTH_LONG).show()
        }
        pendingActivationTitle = null
    }

    // A FixedRange devotional is on *by default* inside its window, so relying on the
    // long-press alone to ask for POST_NOTIFICATIONS meant a fresh Android 13+ install
    // never got asked at all and every reminder was silently dropped (R1 in
    // specs/code-audit-v3). Ask once, on our own, as soon as Home shows anything that's
    // effectively on - and never again after that first time (the long-press path below
    // still asks whenever the user explicitly turns something on). Uses today's effective
    // state, not the upcoming-cycle value the badge shows, so a default novena doesn't
    // trigger this prompt out of season (specs/notifications-panel).
    val anyNotifying = devotionals.any { it.isNotifyingNow }
    LaunchedEffect(anyNotifying) {
        if (anyNotifying && needsNotificationPermission(context) && !hasAutoPromptedPermission()) {
            onAutoPromptedPermission()
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LottieAnimationRender(R.raw.animation_snow_falling)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            devotionals.forEach { item ->
                val title = stringResource(item.devotional.titleResId)
                val journeyStatus = remember(item.devotional, item.currentDay, item.completedDays) {
                    resolveJourneyStatus(item.devotional, item.currentDay, item.completedDays)
                }

                val toggleLabel = stringResource(R.string.content_description_toggle_notifications, title)

                JourneyCard(
                    devotionalTitle = title,
                    status = journeyStatus,
                    isNotifying = item.isNotifying,
                    notificationsBlocked = notificationsBlocked,
                    onContinue = { day -> onEvent(NovenaAction.GoToDay(item.devotional.id, day)) },
                    onPreview = { onEvent(NovenaAction.GoToDay(item.devotional.id, 1)) },
                    // onLongClickLabel is what lets TalkBack announce this gesture at
                    // all; without it the long press was undiscoverable non-visually.
                    // No outer clip() here: one was tried for P1-7's rectangular ripple,
                    // but it also cut off ElevatedCard's elevation shadow (R2 in
                    // specs/code-audit-v3), so it was rolled back at the owner's request.
                    modifier = Modifier
                        .combinedClickable(
                            // TODO(owner): define what a plain tap on the journey card
                            // does (Q7 in specs/code-audit-v3) - deliberately a no-op
                            // for now; the card's own button is the entry point.
                            onClick = {},
                            onLongClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                val wasNotifying = item.isNotifying
                                onToggleNotification(item.devotional.id, wasNotifying)
                                // The Toast only promises alerts when they can actually be
                                // posted (P1-5 in specs/code-audit-v3): if the runtime
                                // permission is missing, ask first and confirm from the
                                // dialog's result; if notifications are blocked some other
                                // way (denied for good, or turned off in system Settings),
                                // say so instead of claiming they're on.
                                when {
                                    wasNotifying -> Toast.makeText(
                                        context, disabledMessage.format(title), Toast.LENGTH_SHORT
                                    ).show()

                                    areNotificationsEnabled(context) -> Toast.makeText(
                                        context, enabledMessage.format(title), Toast.LENGTH_SHORT
                                    ).show()

                                    needsNotificationPermission(context) -> {
                                        pendingActivationTitle = title
                                        // Counts as the one automatic ask too, so the
                                        // LaunchedEffect above doesn't fire a second dialog
                                        // once this toggle makes `anyNotifying` true.
                                        onAutoPromptedPermission()
                                        permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                    }

                                    else -> Toast.makeText(
                                        context, blockedMessage.format(title), Toast.LENGTH_LONG
                                    ).show()
                                }
                            },
                            onLongClickLabel = toggleLabel
                        )
                )
            }
        }

        // On a celebration day the sign plays on top of the feed, where no card can cover
        // it, until it's closed; the cards underneath - "Completed" included - are then
        // reachable as on any other day (Q13 in specs/code-audit-v3).
        if (isCelebrationDay && !celebrationDismissed) {
            CelebrationOverlay(onDismiss = onDismissCelebration)
        }
    }
}

/** Covers the feed; a tap anywhere, the close button, or back dismisses it. */
@Composable
private fun CelebrationOverlay(onDismiss: () -> Unit) {
    BackHandler(onBack = onDismiss)
    val dismissLabel = stringResource(R.string.content_description_dismiss_celebration)
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClickLabel = dismissLabel,
                onClick = onDismiss
            )
    ) {
        CelebrationAnimation()
        IconButton(
            onClick = onDismiss,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = dismissLabel,
                tint = MaterialTheme.colorScheme.onBackground
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
 * Loops for up to [maxLoopDuration] (a decorative background animation has no reason
 * to run - and drain battery - for as long as the app happens to stay open), then
 * settles on whatever frame it was on. Skips animating at all when the system's
 * "Remove animations" accessibility setting is on (`ANIMATOR_DURATION_SCALE == 0`),
 * holding on the *last* frame - the first one can be empty, e.g. the celebration sign
 * before it draws in (P2-6 and R4 in specs/code-audit-v3).
 */
@Composable
internal fun LottieAnimationRender(
    animationRes: Int,
    modifier: Modifier = Modifier,
    // Owner's call (Q9 in specs/code-audit-v3): 30s felt too short for someone who stays
    // on Home a while, so one minute.
    maxLoopDuration: Duration = 1.minutes
) {
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(animationRes))
    val context = LocalContext.current
    val reduceMotionEnabled = remember {
        Settings.Global.getFloat(context.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) == 0f
    }
    val animatable = rememberLottieAnimatable()

    LaunchedEffect(composition, reduceMotionEnabled) {
        val loadedComposition = composition ?: return@LaunchedEffect
        if (reduceMotionEnabled) {
            animatable.snapTo(loadedComposition, progress = 1f)
            return@LaunchedEffect
        }
        val deadline = System.nanoTime() + maxLoopDuration.inWholeNanoseconds
        while (isActive && System.nanoTime() < deadline) {
            animatable.animate(loadedComposition, iterations = 1)
        }
    }

    LottieAnimation(
        composition = composition,
        progress = { animatable.progress },
        modifier = modifier.fillMaxSize() // Ajusta el tamaño según necesites
    )
}

@PreviewAllPhones
@Composable
private fun PreviewHomeScreen() {
    NovenAppTheme {
        HomeScreen(
            devotionals = listOf(
                DevotionalHomeItem(
                    devotional = DevotionalCatalog.default,
                    currentDay = 4,
                    completedDays = setOf(1, 2, 3),
                    isNotifying = true
                )
            ),
            onEvent = {},
            onToggleNotification = { _, _ -> }
        )
    }
}

@PreviewAllPhones
@Composable
private fun PreviewHomeScreenOffSeason() {
    NovenAppTheme {
        HomeScreen(
            devotionals = listOf(
                DevotionalHomeItem(
                    devotional = DevotionalCatalog.default,
                    currentDay = null,
                    completedDays = emptySet(),
                    isNotifying = false
                )
            ),
            onEvent = {},
            onToggleNotification = { _, _ -> }
        )
    }
}
