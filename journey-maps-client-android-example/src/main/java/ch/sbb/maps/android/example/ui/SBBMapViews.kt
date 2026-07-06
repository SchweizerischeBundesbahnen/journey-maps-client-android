// SPDX-License-Identifier: MIT
// Copyright (c) 2024 Schweizerische Bundesbahnen AG (Swiss Federal Railways)

package ch.sbb.maps.android.example.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.MutableLiveData
import ch.sbb.maps.map.SBBMapUiSettings
import ch.sbb.maps.controls.SBBMapControls
import ch.sbb.maps.map.SBBMapView
import org.maplibre.android.camera.CameraPosition

/**
 * Small, non-interactive map preview used as a placeholder on OnBoarding page.
 * By default, all gestures and controls are disabled and attribution is hidden.
 */
@Composable
fun SBBMapPreview(
    tilesApiKey: String,
    modifier: Modifier = Modifier,
    cameraPosition: CameraPosition? = null,
    initialZoom: Double = 6.0,
) {
    SBBMapView(
        tilesApiKey = tilesApiKey,
        modifier = modifier,
        cameraPosition = cameraPosition,
        zoomLevel = MutableLiveData(initialZoom),
        controls = SBBMapControls(
            userLocationEnabled = false,
            mapStyleSwitchEnabled = false,
            floorSwitchEnabled = false,
            compassEnabled = false,
        ),
        uiSettings =
            SBBMapUiSettings(
                showAttribution = false,
                enableZoomGestures = false,
                enableScrollGestures = false,
                enableRotateGestures = false,
                enableTiltGestures = false,
                enableDoubleTapGestures = false,
            ),
    )
}

@Composable
fun DefaultMapView(tilesApiKey: String) {
    SBBMapView(tilesApiKey = tilesApiKey)
}

@Composable
fun ConfigurableMapView(tilesApiKey: String) {
    val userLocationEnabled = remember { mutableStateOf(true) }
    val mapStyleSwitchEnabled = remember { mutableStateOf(true) }
    val floorSwitchEnabled = remember { mutableStateOf(true) }
    Column {
        ConfigurationBoard(
            userLocationEnabled = userLocationEnabled,
            mapStyleSwitchEnabled = mapStyleSwitchEnabled,
            floorSwitchEnabled = floorSwitchEnabled,
        )
        SBBMapView(
            tilesApiKey = tilesApiKey,
            controls = SBBMapControls(
                userLocationEnabled = userLocationEnabled.value,
                mapStyleSwitchEnabled = mapStyleSwitchEnabled.value,
                floorSwitchEnabled = floorSwitchEnabled.value,
            ),
        )
    }
}
