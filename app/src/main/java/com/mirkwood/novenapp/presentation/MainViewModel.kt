package com.mirkwood.novenapp.presentation

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.mirkwood.novenapp.data.devotional.DevotionalRepository
import com.mirkwood.novenapp.data.notifications.DevotionalNotificationStore
import com.mirkwood.novenapp.data.notifications.NotificationPermissionPromptStore
import com.mirkwood.novenapp.data.progress.DevotionalProgressStore
import com.mirkwood.novenapp.presentation.model.DevotionalContent
import com.mirkwood.novenapp.presentation.model.devotional.DevotionalMeta
import com.mirkwood.novenapp.presentation.navigation.NavigationScreen
import com.mirkwood.novenapp.presentation.util.Util
import com.mirkwood.novenapp.reminder.NovenaReminderScheduler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

/** One devotional's state as shown on the Home feed - see specs/devotional-catalog-selection. */
internal data class DevotionalHomeItem(
    val devotional: DevotionalMeta,
    val currentDay: Int?,
    val completedDays: Set<Int>,
    val isNotifying: Boolean
)

/**
 * Drives the Home feed (every relevant devotional, not just one hardcoded entry - see
 * specs/devotional-catalog-selection) and the shared lookups the day screen needs.
 */
internal class MainViewModel(
    private val repository: DevotionalRepository,
    private val progressStore: DevotionalProgressStore,
    private val notificationStore: DevotionalNotificationStore,
    private val permissionPromptStore: NotificationPermissionPromptStore
) : ViewModel() {

    /** Bumped to force [homeDevotionals] to re-resolve "today" (a date rollover, or the debug forced-date switch). */
    private val refreshTrigger = MutableStateFlow(0)

    /**
     * The day screen's "early look" banner, once closed, stays closed for this session
     * (across days and rotation) and comes back on the next launch. In memory only, by
     * the owner's choice - no persisted store.
     */
    private val _previewBannerDismissed = MutableStateFlow(false)
    val previewBannerDismissed: StateFlow<Boolean> = _previewBannerDismissed

    fun dismissPreviewBanner() {
        _previewBannerDismissed.value = true
    }

    // Every catalog devotional shows a card - including a FixedRange one that hasn't
    // started yet (JourneyStatus.NotStarted's countdown), matching today's existing
    // year-round behavior for the one novena. "Relevant" only gates notifications
    // (Util.isNotifyingByDefault), not which cards Home shows.
    val homeDevotionals: StateFlow<List<DevotionalHomeItem>> =
        combine(repository.getCatalog().map(::devotionalHomeItemFlow)) { items ->
            items.toList().sortedWith(HOME_SORT_ORDER)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private fun devotionalHomeItemFlow(devotional: DevotionalMeta) = combine(
        progressStore.observeProgress(devotional.id),
        notificationStore.observeOverride(devotional.id),
        refreshTrigger
    ) { progress, override, _ ->
        DevotionalHomeItem(
            devotional = devotional,
            currentDay = Util.resolveCurrentDay(devotional.schedule),
            completedDays = Util.resolveEffectiveCompletedDays(devotional.schedule, progress.completedDays, progress.cycleKey),
            isNotifying = Util.isNotifyingEffective(devotional.schedule, override?.enabled, override?.cycleKey)
        )
    }

    fun getDevotional(id: String): DevotionalMeta = repository.getDevotional(id)

    /** Fallback for a missing route arg - the route always requires one in practice. */
    fun defaultDevotionalId(): String =
        Util.pickFeaturedDevotional(repository.getCatalog())?.id ?: repository.getCatalog().first().id

    /** The devotional's completed days for the *currently relevant cycle* only - see Util.resolveEffectiveCompletedDays. */
    fun observeCompletedDays(devotionalId: String) = run {
        val schedule = repository.getDevotional(devotionalId).schedule
        progressStore.observeProgress(devotionalId)
            .map { progress -> Util.resolveEffectiveCompletedDays(schedule, progress.completedDays, progress.cycleKey) }
    }

    fun getContentFor(devotionalId: String, language: String): DevotionalContent? =
        repository.loadContent(devotionalId, language)

    fun refreshCurrentDay() {
        refreshTrigger.update { it + 1 }
    }

    /**
     * [isCurrentlyCompleted] is the *effective* (cycle-filtered) value the caller is
     * already displaying, so toggling a day always starts from what the user can
     * actually see, never from a stale prior cycle's raw stored value.
     */
    fun toggleDayCompleted(devotionalId: String, day: Int, isCurrentlyCompleted: Boolean) {
        viewModelScope.launch {
            val schedule = repository.getDevotional(devotionalId).schedule
            val cycleKey = Util.resolveProgressCycleKey(schedule)
            progressStore.setDayCompleted(devotionalId, day, !isCurrentlyCompleted, cycleKey)
        }
    }

    /**
     * Long-press on a Home card - see specs/devotional-catalog-selection §4.2/4.3. The
     * override is stamped with the devotional's *current* cycle key
     * ([Util.resolveCycleKey]), so it naturally stops applying once that
     * cycle ends, with no separate expiry step (see the P0-2 finding in
     * specs/code-audit-v3). Schedules immediately when turning on, rather than waiting
     * for the next app start's [NovenaReminderScheduler.scheduleIfNeeded] to notice the
     * override; [NovenaReminderScheduler]'s own date resolution makes sure that doesn't
     * fire anything whose slot already passed today (P0-3).
     */
    fun toggleNotification(context: Context, devotionalId: String, isCurrentlyNotifying: Boolean) {
        viewModelScope.launch {
            val devotional = repository.getDevotional(devotionalId)
            val enabling = !isCurrentlyNotifying
            val cycleKey = Util.resolveCycleKey(devotional.schedule)
            notificationStore.setOverride(devotionalId, enabling, cycleKey)
            if (enabling) {
                val today = LocalDate.now()
                NovenaReminderScheduler.scheduleReminder(context, devotionalId, today)
                NovenaReminderScheduler.scheduleEveningCheckIn(context, devotionalId, today)
            }
        }
    }

    /** Whether Home already asked for the notification permission on its own - see [NotificationPermissionPromptStore]. */
    fun hasAutoPromptedNotificationPermission(): Boolean = permissionPromptStore.hasAutoPrompted()

    fun markNotificationPermissionAutoPrompted() = permissionPromptStore.markAutoPrompted()

    fun onAction(action: NovenaAction, navController: NavController) {
        when (action) {
            NovenaAction.GoHome -> {
                navController.navigate(NavigationScreen.HomeScreen.route)
            }

            is NovenaAction.GoToDay -> {
                navController.navigate(
                    NavigationScreen.DayScreen.createRoute(action.devotionalId, action.selectedDay)
                ) {
                    // Defense in depth against a duplicate GoToDay firing twice in a
                    // row for the same destination (e.g. a stray recomposition of the
                    // pending-target effect) - see the P0-4 finding in
                    // specs/code-audit-v3. The primary fix for the notification/widget
                    // deep-link case itself is in MainActivity (only process the
                    // pending intent when there's no saved instance state).
                    launchSingleTop = true
                }
            }
        }
    }

    private companion object {
        /** In-window FixedRange devotionals first, then Evergreen - one comparator, shared with Util.pickFeaturedDevotional. */
        val HOME_SORT_ORDER: Comparator<DevotionalHomeItem> = compareBy(Util.devotionalFeatureOrder) { it.devotional }
    }
}
