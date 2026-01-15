package com.mirkwood.novenapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.mirkwood.compose_preview.PreviewAllPhones
import com.mirkwood.novenapp.presentation.AppNavHost
import com.mirkwood.novenapp.presentation.MainViewModel
import com.mirkwood.novenapp.presentation.components.AppDrawer
import com.mirkwood.novenapp.presentation.navigation.NavigationScreen
import com.mirkwood.novenapp.ui.theme.NovenAppTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyApp()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyApp() {
    val navController = rememberNavController()
    val currentNavBackStackEntry by navController.currentBackStackEntryAsState()

    val currentRoute =
        currentNavBackStackEntry?.destination?.route ?: NavigationScreen.HomeScreen.route
    val drawerState: DrawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()
    val viewModel: MainViewModel = MainViewModel()
    var currentSongTitle by remember { mutableStateOf("Villancicos") }

    NovenAppTheme {
        ModalNavigationDrawer(drawerContent = {
            ModalDrawerSheet(
                drawerState = drawerState,
                drawerContainerColor = MaterialTheme.colorScheme.background,
                drawerContentColor = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier
                    .fillMaxHeight()
                    .wrapContentWidth()
            ) {
                AppDrawer(
                    onOptionClick = { option ->
                        viewModel.onAction(option, navController)
                    },
                    closeDrawer = {
                        coroutineScope.launch {
                            drawerState.close() // Cierra el drawer
                        }
                    },
                    modifier = Modifier,
                    onAboutUsClick = {
                        navController.navigate(NavigationScreen.AboutUsScreen.route)
                    },
                    onLyricsClick = {
                        navController.navigate(NavigationScreen.LyricsScreen.route)
                    }
                )
            }
        }, drawerState = drawerState) {
            Scaffold(modifier = Modifier.fillMaxSize(),
                topBar = {
                    val showBackButton = currentRoute != NavigationScreen.HomeScreen.route
                    if (currentRoute != NavigationScreen.DayScreen.route) {
                        TopAppBar(
                            title = {
                                Text(
                                    when (currentRoute) {
                                        NavigationScreen.LyricsScreen.route -> stringResource(R.string.villancicos)
                                        NavigationScreen.LyricsViewScreen.route -> currentSongTitle
                                        NavigationScreen.AboutUsScreen.route -> stringResource(R.string.text_about_us)
                                        else -> ""
                                    }
                                )
                            },
                            navigationIcon = {
                                if (showBackButton) {
                                    IconButton(onClick = { navController.popBackStack() }) {
                                        Icon(
                                            Icons.AutoMirrored.Filled.ArrowBack,
                                            contentDescription = "Regresar"
                                        )
                                    }
                                } else {
                                    IconButton(onClick = { coroutineScope.launch { drawerState.open() } }) {
                                        Icon(
                                            Icons.Filled.Menu,
                                            contentDescription = stringResource(R.string.menu)
                                        )
                                    }
                                }
                            }
                        )
                    }
                }
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    if (NavigationScreen.DayScreen.route == currentRoute) {
                        BackButton(
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .zIndex(1f),
                            onBackClick = { navController.popBackStack() }
                        )
                    }
                    AppNavHost(navController, onSongTitleUpdated = { newTitle ->
                        currentSongTitle = newTitle
                    })
                }
            }
        }
    }
}

@Composable
fun BackButton(modifier: Modifier = Modifier, onBackClick: () -> Unit) {
    IconButton(
        onClick = onBackClick,
        modifier = modifier
            .padding(16.dp)
            .background(Color.Black.copy(alpha = 0.3f), CircleShape)
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            tint = Color.White,
            contentDescription = stringResource(R.string.btn_back)
        )
    }
}

@PreviewAllPhones
@Composable
fun PreviewApp() {
    NovenAppTheme {

        MyApp()
    }
}
