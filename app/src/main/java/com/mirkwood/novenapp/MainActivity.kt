package com.mirkwood.novenapp

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.StringRes
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffoldDefaults
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteType
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mirkwood.novenapp.presentation.components.MAX_TEXT_SCALE
import com.mirkwood.novenapp.presentation.components.MIN_TEXT_SCALE
import com.mirkwood.novenapp.presentation.components.NotificationsSheet
import com.mirkwood.novenapp.presentation.components.TEXT_SCALE_STEP
import com.mirkwood.novenapp.presentation.components.TextSizeMenu
import com.mirkwood.novenapp.presentation.notifications.areNotificationsEnabled
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.mirkwood.compose_preview.PreviewAllPhones
import com.mirkwood.novenapp.presentation.AppNavHost
import com.mirkwood.novenapp.presentation.MainViewModel
import com.mirkwood.novenapp.presentation.NovenaAction
import com.mirkwood.novenapp.presentation.components.NovenAppBar
import com.mirkwood.novenapp.presentation.navigation.NavigationScreen
import com.mirkwood.novenapp.ui.theme.NovenAppTheme
import org.koin.androidx.compose.koinViewModel

/** A specific day of a specific devotional, requested from outside the app (a notification tap or the widget). */
data class PendingTarget(val devotionalId: String, val day: Int)

class MainActivity : ComponentActivity() {

    private var pendingTarget by mutableStateOf<PendingTarget?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        // Only a *fresh* start (no saved state) represents a genuine notification/widget
        // tap to act on. Without this guard, rotating the screen (or any other
        // recreation with saved state) re-read the same intent's extras - which are
        // never cleared just by consuming them in Compose state - and navigated to the
        // same day a second time, doubling it on the back stack. See the P0-4 finding
        // in specs/code-audit-v3. A genuinely new tap while already running still comes
        // through onNewIntent below, unconditionally, as it should.
        if (savedInstanceState == null) {
            pendingTarget = intent.extractPendingTarget()
        }
        setContent {
            MyApp(
                pendingTarget = pendingTarget,
                onPendingTargetConsumed = {
                    intent.removeExtra(EXTRA_TARGET_DAY)
                    intent.removeExtra(EXTRA_TARGET_DEVOTIONAL_ID)
                    pendingTarget = null
                }
            )
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        pendingTarget = intent.extractPendingTarget()
    }

    private fun Intent.extractPendingTarget(): PendingTarget? {
        val day = getIntExtra(EXTRA_TARGET_DAY, -1)
        val devotionalId = getStringExtra(EXTRA_TARGET_DEVOTIONAL_ID)
        return if (day > 0 && devotionalId != null) PendingTarget(devotionalId, day) else null
    }

    companion object {
        const val EXTRA_TARGET_DAY = "extra_target_day"
        const val EXTRA_TARGET_DEVOTIONAL_ID = "extra_target_devotional_id"
    }
}

/**
 * The app's 3 top-level destinations, shown as a persistent bottom bar instead of a
 * hamburger drawer - there were never more than 3 real destinations (today's prayer,
 * villancicos, about/rate). Jumping to a specific day used to live in a drawer, then a
 * day-chip row on the Novena tab; both were removed once the Camino card made "today"
 * the only entry point, with day-to-day movement handled inside DayScreen's own
 * prev/next controls instead (see specs/day-locked-state).
 */
private data class BottomNavDestination(
    val route: String,
    @StringRes val labelRes: Int,
    val icon: @Composable () -> Unit
)

/** A screen title reported by the back-stack entry identified by [entryId]. */
private data class ReportedTitle(val entryId: String, val title: String)

private val bottomNavDestinations = listOf(
    BottomNavDestination(
        route = NavigationScreen.HomeScreen.route,
        labelRes = R.string.bottom_nav_novena,
        icon = { Icon(Icons.Default.Home, contentDescription = null) }
    ),
    BottomNavDestination(
        route = NavigationScreen.LyricsScreen.route,
        labelRes = R.string.bottom_nav_villancicos,
        icon = { Icon(painterResource(R.drawable.music_note_24px), contentDescription = null) }
    ),
    BottomNavDestination(
        route = NavigationScreen.MoreScreen.route,
        labelRes = R.string.bottom_nav_more,
        icon = { Icon(Icons.Default.MoreVert, contentDescription = null) }
    )
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun MyApp(
    pendingTarget: PendingTarget? = null,
    onPendingTargetConsumed: () -> Unit = {}
) {
    val navController = rememberNavController()
    val currentNavBackStackEntry by navController.currentBackStackEntryAsState()

    val currentRoute =
        currentNavBackStackEntry?.destination?.route ?: NavigationScreen.HomeScreen.route
    val viewModel: MainViewModel = koinViewModel()
    // Each title is stored with the id of the back-stack entry that reported it, and only
    // shown while that entry is current. Until the new entry reports its own, the static
    // fallback label shows - never the previous day's page or the previous song
    // (P1-6 in specs/code-audit-v3).
    var songTitle by remember { mutableStateOf<ReportedTitle?>(null) }
    var stageTitle by remember { mutableStateOf<ReportedTitle?>(null) }
    val currentEntryId = currentNavBackStackEntry?.id
    val currentSongTitle = songTitle?.takeIf { it.entryId == currentEntryId }?.title
    val currentStageTitle = stageTitle?.takeIf { it.entryId == currentEntryId }?.title

    // Lyrics text size, owned by the song's back-stack entry it was set on: a newly opened
    // song (new entry id) starts at 1x, the owner's choice, while a song being popped keeps
    // its size through the exit animation. Saveable so rotation keeps it.
    var lyricsScaleEntryId by rememberSaveable { mutableStateOf<String?>(null) }
    var lyricsScaleValue by rememberSaveable { mutableFloatStateOf(1f) }
    fun lyricsTextScaleFor(entryId: String?): Float =
        if (entryId != null && entryId == lyricsScaleEntryId) lyricsScaleValue else 1f
    fun setLyricsTextScale(entryId: String?, scale: Float) {
        lyricsScaleEntryId = entryId
        lyricsScaleValue = scale
    }

    val isTopLevelRoute = bottomNavDestinations.any { it.route == currentRoute }

    // Phones keep the existing floating-pill NavigationBar exactly as designed; only
    // medium/expanded windows (tablets, unfolded foldables) switch to a rail/drawer via
    // NavigationSuiteScaffold - see specs/adaptive-nav-layout for why this isn't unified
    // into one container for every window size.
    val navSuiteType = NavigationSuiteScaffoldDefaults.calculateFromAdaptiveInfo(currentWindowAdaptiveInfo())
    val isCompactWidth = navSuiteType == NavigationSuiteType.NavigationBar

    // A notification or the home-screen widget was tapped with a specific day to
    // open - reuses the same route-building logic as the in-app day picker.
    LaunchedEffect(pendingTarget) {
        if (pendingTarget != null) {
            viewModel.onAction(NovenaAction.GoToDay(pendingTarget.devotionalId, pendingTarget.day), navController)
            onPendingTargetConsumed()
        }
    }

    // Re-resolve "today's day" every time the Novena tab comes to the front, so a date
    // rollover (past midnight, or the More ▸ Debug forced-date switch) is reflected
    // without a full app restart.
    LaunchedEffect(currentRoute) {
        if (currentRoute == NavigationScreen.HomeScreen.route) {
            viewModel.refreshCurrentDay()
        }
    }

    // The app bar bell's notifications sheet (specs/notifications-panel). Blocked state is
    // re-checked on every resume - returning from system Settings is the usual way it
    // changes - and after the permission dialog.
    val context = LocalContext.current
    val homeDevotionals by viewModel.homeDevotionals.collectAsStateWithLifecycle()
    var showNotificationsSheet by rememberSaveable { mutableStateOf(false) }
    var notificationsBlocked by remember { mutableStateOf(!areNotificationsEnabled(context)) }
    LifecycleResumeEffect(Unit) {
        notificationsBlocked = !areNotificationsEnabled(context)
        onPauseOrDispose { }
    }
    if (showNotificationsSheet) {
        NotificationsSheet(
            devotionals = homeDevotionals,
            notificationsBlocked = notificationsBlocked,
            onToggle = { devotionalId, isCurrentlyNotifying ->
                viewModel.toggleNotification(context, devotionalId, isCurrentlyNotifying)
            },
            onPermissionResult = { notificationsBlocked = !areNotificationsEnabled(context) },
            onDismiss = { showNotificationsSheet = false }
        )
    }
    val openNotificationsSheet = { showNotificationsSheet = true }

    val topBar: @Composable () -> Unit = {
        when (currentRoute) {
            NavigationScreen.HomeScreen.route -> NovenAppBar(
                sectionTitle = stringResource(R.string.app_bar_section_home),
                onNotificationsClick = openNotificationsSheet,
                notificationsBlocked = notificationsBlocked
            )
            NavigationScreen.LyricsScreen.route -> NovenAppBar(
                sectionTitle = stringResource(R.string.app_bar_section_lyrics),
                onNotificationsClick = openNotificationsSheet,
                notificationsBlocked = notificationsBlocked
            )
            NavigationScreen.MoreScreen.route -> NovenAppBar(
                sectionTitle = stringResource(R.string.app_bar_section_more),
                onNotificationsClick = openNotificationsSheet,
                notificationsBlocked = notificationsBlocked
            )
            NavigationScreen.DayScreen.route -> NovenAppBar(
                sectionTitle = currentStageTitle ?: stringResource(R.string.app_bar_section_reading),
                onBackClick = { navController.popBackStack() }
            )
            NavigationScreen.LyricsViewScreen.route, NavigationScreen.AboutUsScreen.route -> {
                TopAppBar(
                    title = {
                        Text(
                            if (currentRoute == NavigationScreen.LyricsViewScreen.route) {
                                currentSongTitle ?: stringResource(R.string.app_bar_section_lyrics)
                            } else {
                                stringResource(R.string.text_about_us)
                            }
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(R.string.btn_back)
                            )
                        }
                    },
                    actions = {
                        if (currentRoute == NavigationScreen.LyricsViewScreen.route) {
                            val scale = lyricsTextScaleFor(currentEntryId)
                            TextSizeMenu(
                                canDecreaseText = scale > MIN_TEXT_SCALE,
                                canIncreaseText = scale < MAX_TEXT_SCALE,
                                onDecreaseText = {
                                    setLyricsTextScale(currentEntryId, (scale - TEXT_SCALE_STEP).coerceAtLeast(MIN_TEXT_SCALE))
                                },
                                onIncreaseText = {
                                    setLyricsTextScale(currentEntryId, (scale + TEXT_SCALE_STEP).coerceAtMost(MAX_TEXT_SCALE))
                                }
                            )
                        }
                    }
                )
            }
        }
    }

    val content: @Composable (PaddingValues) -> Unit = { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            AppNavHost(
                navController = navController,
                mainViewModel = viewModel,
                onSongTitleUpdated = { entryId, newTitle ->
                    songTitle = ReportedTitle(entryId, newTitle)
                },
                onStageTitleUpdated = { entryId, newTitle ->
                    stageTitle = ReportedTitle(entryId, newTitle)
                },
                lyricsTextScaleFor = ::lyricsTextScaleFor
            )
        }
    }

    fun navigateToTopLevel(route: String) {
        navController.navigate(route) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    NovenAppTheme {
        when {
            isCompactWidth -> Scaffold(
                modifier = Modifier.fillMaxSize(),
                topBar = topBar,
                bottomBar = {
                    if (isTopLevelRoute) {
                        // A regular Material 3 NavigationBar, but wrapped in a rounded,
                        // elevated Surface with side margins so it reads as a bar floating
                        // over the app background rather than a slab pinned to the edge.
                        // It stays in the Scaffold bottomBar slot, so screen content is
                        // still inset above it automatically.
                        Surface(
                            shape = MaterialTheme.shapes.large,
                            color = MaterialTheme.colorScheme.surfaceContainer,
                            tonalElevation = 3.dp,
                            shadowElevation = 6.dp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .navigationBarsPadding()
                                .padding(horizontal = 16.dp, vertical = 12.dp)
                        ) {
                            NavigationBar(
                                containerColor = Color.Transparent,
                                windowInsets = WindowInsets(0, 0, 0, 0)
                            ) {
                                bottomNavDestinations.forEach { destination ->
                                    NavigationBarItem(
                                        selected = currentRoute == destination.route,
                                        onClick = { navigateToTopLevel(destination.route) },
                                        icon = destination.icon,
                                        label = { Text(stringResource(destination.labelRes)) }
                                    )
                                }
                            }
                        }
                    }
                },
                content = content
            )

            // Medium/expanded window (tablet, unfolded foldable): only show the
            // rail/drawer around top-level destinations, matching the phone layout's
            // rule that stack screens (Day, Lyrics view, About) render with no nav
            // container of their own.
            isTopLevelRoute -> NavigationSuiteScaffold(
                navigationSuiteItems = {
                    bottomNavDestinations.forEach { destination ->
                        item(
                            selected = currentRoute == destination.route,
                            onClick = { navigateToTopLevel(destination.route) },
                            icon = destination.icon,
                            label = { Text(stringResource(destination.labelRes)) }
                        )
                    }
                },
                layoutType = navSuiteType
            ) {
                Scaffold(modifier = Modifier.fillMaxSize(), topBar = topBar, content = content)
            }

            else -> Scaffold(modifier = Modifier.fillMaxSize(), topBar = topBar, content = content)
        }
    }
}

@PreviewAllPhones
@Composable
fun PreviewApp() {
    NovenAppTheme {

        MyApp()
    }
}
