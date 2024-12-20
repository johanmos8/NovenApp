package com.mirkwood.novenapp

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.mirkwood.novenapp.presentation.MainViewModel
import com.mirkwood.novenapp.presentation.NavigationScreen
import com.mirkwood.novenapp.presentation.components.AppDrawer
import com.mirkwood.novenapp.presentation.components.NovenaTopAppBar
import com.mirkwood.novenapp.presentation.components.ReadingScreen
import com.mirkwood.novenapp.presentation.components.TextTabs
import com.mirkwood.novenapp.presentation.screens.home.HomeScreen
import com.mirkwood.novenapp.ui.theme.NovenAppTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val mainViewModel = MainViewModel()
        enableEdgeToEdge()
        setContent {
            val navController = rememberNavController()
            val currentNavBackStackEntry by navController.currentBackStackEntryAsState()

            val currentRoute =
                currentNavBackStackEntry?.destination?.route ?: NavigationScreen.HomeScreen.route
            val drawerState: DrawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
            val coroutineScope = rememberCoroutineScope()

            NovenAppTheme {
                ModalNavigationDrawer(drawerContent = {
                    AppDrawer(
                        route = currentRoute,
                        navHostController = navController,
                        closeDrawer = {
                            coroutineScope.launch {
                                drawerState.close() // Cierra el drawer
                            }
                        },
                        modifier = Modifier
                    )
                }, drawerState = drawerState) {
                    Scaffold(modifier = Modifier.fillMaxSize(),
                        topBar = {
                            NovenaTopAppBar(
                                navController = navController,
                                showBackButton = currentRoute != NavigationScreen.HomeScreen.route,
                                drawerAction= {
                                    coroutineScope.launch {
                                        drawerState.open() // abre el drawer
                                    }
                                })
                        }
                    ) { innerPadding ->
                        NavHost(
                            navController = navController,
                            startDestination = NavigationScreen.HomeScreen.route,
                        ) {
                            composable(NavigationScreen.HomeScreen.route) {
                                HomeScreen(
                                    navController
                                )
                            }
                            composable(
                                NavigationScreen.DayScreen.route,
                                arguments = listOf(navArgument("position") {
                                    type = NavType.IntType
                                })
                            ) { it ->

                                val day = it.arguments?.getInt("position") ?: -1

                                val novenaDay = if (day != -1) {
                                    day
                                } else {
                                    mainViewModel.getNovenaDay()
                                }
                                Log.d("NovenaDay", novenaDay.toString())
                                novenaDay?.let { currentDay ->
                                    TextTabs(
                                        innerPadding = innerPadding,
                                        mainViewModel = mainViewModel,
                                        currentDay
                                    )
                                }
                            }

                        }

                    }
                }
            }
        }
    }
}
