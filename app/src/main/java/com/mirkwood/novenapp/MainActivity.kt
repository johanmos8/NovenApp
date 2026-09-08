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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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

class MainActivity : ComponentActivity() {

    private var pendingTargetDay by mutableStateOf<Int?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        pendingTargetDay = intent.extractTargetDay()
        setContent {
            MyApp(
                pendingTargetDay = pendingTargetDay,
                onPendingTargetDayConsumed = { pendingTargetDay = null }
            )
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        pendingTargetDay = intent.extractTargetDay()
    }

    private fun Intent.extractTargetDay(): Int? {
        val day = getIntExtra(EXTRA_TARGET_DAY, -1)
        return if (day > 0) day else null
    }

    companion object {
        const val EXTRA_TARGET_DAY = "extra_target_day"
    }
}

/**
 * The app's 3 top-level destinations, shown as a persistent bottom bar instead of a
 * hamburger drawer - there were never more than 3 real destinations (today's prayer,
 * villancicos, about/rate), and the day picker that used to live in the drawer is now
 * a [com.mirkwood.novenapp.presentation.components.DayStrip] directly on the Novena tab.
 */
private data class BottomNavDestination(
    val route: String,
    @StringRes val labelRes: Int,
    val icon: @Composable () -> Unit
)

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyApp(
    pendingTargetDay: Int? = null,
    onPendingTargetDayConsumed: () -> Unit = {}
) {
    val navController = rememberNavController()
    val currentNavBackStackEntry by navController.currentBackStackEntryAsState()

    val currentRoute =
        currentNavBackStackEntry?.destination?.route ?: NavigationScreen.HomeScreen.route
    val viewModel: MainViewModel = koinViewModel()
    var currentSongTitle by remember { mutableStateOf("Villancicos") }

    val isTopLevelRoute = bottomNavDestinations.any { it.route == currentRoute }

    // A notification or the home-screen widget was tapped with a specific day to
    // open - reuses the same route-building logic as the in-app day picker.
    LaunchedEffect(pendingTargetDay) {
        if (pendingTargetDay != null) {
            viewModel.onAction(NovenaAction.GoToDay(pendingTargetDay), navController)
            onPendingTargetDayConsumed()
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

    NovenAppTheme {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                when (currentRoute) {
                    NavigationScreen.HomeScreen.route -> NovenAppBar(
                        sectionTitle = stringResource(R.string.app_bar_section_home)
                    )
                    NavigationScreen.LyricsScreen.route -> NovenAppBar(
                        sectionTitle = stringResource(R.string.app_bar_section_lyrics)
                    )
                    NavigationScreen.MoreScreen.route -> NovenAppBar(
                        sectionTitle = stringResource(R.string.app_bar_section_more)
                    )
                    NavigationScreen.DayScreen.route -> NovenAppBar(
                        sectionTitle = stringResource(R.string.app_bar_section_reading),
                        onBackClick = { navController.popBackStack() }
                    )
                    NavigationScreen.LyricsViewScreen.route, NavigationScreen.AboutUsScreen.route -> {
                        TopAppBar(
                            title = {
                                Text(
                                    if (currentRoute == NavigationScreen.LyricsViewScreen.route) {
                                        currentSongTitle
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
                            }
                        )
                    }
                }
            },
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
                                    onClick = {
                                        navController.navigate(destination.route) {
                                            popUpTo(navController.graph.findStartDestination().id) {
                                                saveState = true
                                            }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    },
                                    icon = destination.icon,
                                    label = { Text(stringResource(destination.labelRes)) }
                                )
                            }
                        }
                    }
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                AppNavHost(
                    navController = navController,
                    mainViewModel = viewModel,
                    onSongTitleUpdated = { newTitle ->
                        currentSongTitle = newTitle
                    }
                )
            }
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
