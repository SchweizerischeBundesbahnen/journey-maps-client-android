// SPDX-License-Identifier: MIT
// Copyright (c) 2024 Schweizerische Bundesbahnen AG (Swiss Federal Railways)

package ch.sbb.maps.android.example.ui

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import ch.sbb.maps.android.example.BuildConfig
import ch.sbb.maps.android.example.SBBMapDemoScreen
import ch.sbb.maps.design.SBBRadioButtonGroup
import ch.sbb.maps.design.SBBSecondaryButton
import ch.sbb.maps.theme.SBBMapTheme
import ch.sbb.maps.theme.SBBMapThemeManager

@Composable
fun OnBoarding(
    navController: NavController,
    tilesApiKey: String,
) {
    val radioOptions = listOf("Bright Mode", "Dark Mode")
    var selectedOption by remember { mutableStateOf(radioOptions[0]) }
    val isDarkTheme = remember { mutableStateOf(SBBMapThemeManager.isDarkMode.value) }
    val scrollState = rememberScrollState()
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .background(
                    color = MaterialTheme.colorScheme.background,
                ),
    ) {
        SBBMapPreview(
            tilesApiKey = tilesApiKey,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(200.dp),
        )

        if (isDarkTheme.value) {
            selectedOption = radioOptions[1]
        }
        SBBRadioButtonGroup(
            radioOptions = radioOptions,
            selectedOption = selectedOption,
            onOptionSelected = { newSelectedOption ->
                selectedOption = newSelectedOption
                isDarkTheme.value = isDarkTheme.value.not()
                SBBMapThemeManager.toggleDarkMode()
            },
        )
        SBBSecondaryButton(
            onClick = {
                navController.navigate(SBBMapDemoScreen.Default.name)
            },
            label = "Default Mapview",
        )
        SBBSecondaryButton(
            onClick = {
                navController.navigate(SBBMapDemoScreen.Configurable.name)
            },
            label = "Configurable Mapview",
        )
        SBBSecondaryButton(
            onClick = {
                navController.navigate(SBBMapDemoScreen.Poi.name)
            },
            label = "Poi Mapview",
        )
        SBBSecondaryButton(
            onClick = {
                navController.navigate(SBBMapDemoScreen.PoiWithSubcategories.name)
            },
            label = "Poi Mapview with subcategories",
        )
        SBBSecondaryButton(
            onClick = {
                navController.navigate(SBBMapDemoScreen.GeoJson.name)
            },
            label = "GeoJSON",
        )
    }
}

@Preview(showBackground = true, name = "Light Theme")
@Composable
fun OnBoardingPreviewLight() {
    SBBMapTheme(darkTheme = false) {
        OnBoarding(navController = rememberNavController(), tilesApiKey = BuildConfig.JOURNEY_MAPS_DEMO_API_KEY)
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Dark Theme")
@Composable
fun OnBoardingPreviewDark() {
    SBBMapTheme(darkTheme = true) {
        OnBoarding(navController = rememberNavController(), tilesApiKey = BuildConfig.JOURNEY_MAPS_DEMO_API_KEY)
    }
}
