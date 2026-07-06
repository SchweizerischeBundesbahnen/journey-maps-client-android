// SPDX-License-Identifier: MIT
// Copyright (c) 2024 Schweizerische Bundesbahnen AG (Swiss Federal Railways)

package ch.sbb.maps.android.example

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import ch.sbb.maps.android.example.R.string
import ch.sbb.maps.android.example.ui.ConfigurableMapView
import ch.sbb.maps.android.example.ui.DefaultMapView
import ch.sbb.maps.android.example.ui.OnBoarding
import ch.sbb.maps.android.example.ui.geojson.GeoJsonMapView
import ch.sbb.maps.android.example.ui.poi.PoiMapView
import ch.sbb.maps.android.example.ui.VersionDialog
import ch.sbb.maps.design.SBBHeaderSmall

@Composable
fun SBBMapDemoApp(navController: NavHostController = rememberNavController()) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentScreen =
        SBBMapDemoScreen.valueOf(
            backStackEntry?.destination?.route ?: SBBMapDemoScreen.OnBoarding.name,
        )

    val showVersionInfo = remember { mutableStateOf(false) }
    Scaffold(
        topBar = {
            SBBHeaderSmall(
                title = stringResource(currentScreen.title),
                navController = navController,
                canNavigateBack = navController.previousBackStackEntry != null,
                onSBBLogoClick = { showVersionInfo.value = true },
            )
        },
    ) { innerPadding ->
        VersionDialog(
            version = BuildConfig.VERSION_NAME,
            showDialog = showVersionInfo,
        )
        val tilesApiKey = BuildConfig.JOURNEY_MAPS_DEMO_API_KEY
        NavHost(
            navController = navController,
            startDestination = SBBMapDemoScreen.OnBoarding.name,
            modifier =
                Modifier
                    .padding(innerPadding),
        ) {
            composable(route = SBBMapDemoScreen.OnBoarding.name) {
                OnBoarding(navController, tilesApiKey = tilesApiKey)
            }
            composable(route = SBBMapDemoScreen.Default.name) {
                DefaultMapView(tilesApiKey = tilesApiKey)
            }
            composable(route = SBBMapDemoScreen.Configurable.name) {
                ConfigurableMapView(tilesApiKey = tilesApiKey)
            }
            composable(route = SBBMapDemoScreen.Poi.name) {
                PoiMapView(tilesApiKey = tilesApiKey)
            }
            composable(route = SBBMapDemoScreen.PoiWithSubcategories.name) {
                PoiMapView(tilesApiKey = tilesApiKey, enableSubcategorySelection = true)
            }
            composable(route = SBBMapDemoScreen.GeoJson.name) {
                GeoJsonMapView(tilesApiKey = tilesApiKey)
            }
        }
    }
}

enum class SBBMapDemoScreen(
    @StringRes val title: Int,
) {
    OnBoarding(title = string.app_bar_title_onboarding),
    Default(title = string.app_bar_title_default),
    Configurable(title = string.app_bar_title_configurable),
    Poi(title = string.app_bar_title_poi),
    PoiWithSubcategories(title = string.app_bar_title_poi_with_subcategories),
    GeoJson(title = string.app_bar_title_geojson),
}
