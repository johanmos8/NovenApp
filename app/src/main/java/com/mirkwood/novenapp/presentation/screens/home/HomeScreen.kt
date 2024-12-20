package com.mirkwood.novenapp.presentation.screens.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.mirkwood.novenapp.R
import com.mirkwood.novenapp.presentation.MainViewModel
import com.mirkwood.novenapp.presentation.components.CountdownExample
import com.mirkwood.novenapp.presentation.components.GoToDayButton
import com.mirkwood.novenapp.presentation.components.MainTitle

@Composable
fun HomeScreen(
    navHostController: NavHostController
) {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {

        Image(
            painter = painterResource(id = R.drawable.novena3),
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            contentDescription = "novena"
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center

        ) {
            MainTitle()
            CountdownExample()
            GoToDayButton(navHostController=navHostController, mainViewModel = MainViewModel())
        }
    }
}