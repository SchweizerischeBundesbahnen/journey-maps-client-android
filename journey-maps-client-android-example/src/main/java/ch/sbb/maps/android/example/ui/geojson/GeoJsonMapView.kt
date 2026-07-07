// SPDX-License-Identifier: MIT
// Copyright (c) 2024 Schweizerische Bundesbahnen AG (Swiss Federal Railways)

package ch.sbb.maps.android.example.ui.geojson

import android.graphics.Color
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.MutableLiveData
import ch.sbb.maps.annotation.SBBGeoJson
import ch.sbb.maps.design.SBBSecondaryButton
import ch.sbb.maps.controls.SBBMapControls
import ch.sbb.maps.poi.SBBMapPoi
import ch.sbb.maps.map.SBBMapView
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.geometry.LatLng

private val thun = LatLng(46.754169, 7.631126)
private val bern = LatLng(46.948818, 7.439137)

const val DEFAULT_ZOOM_LEVEL = 16.0

@Composable
fun GeoJsonMapView(tilesApiKey: String) {
    val scope = rememberCoroutineScope()
    val geoJsonState = remember { MutableLiveData(SBBGeoJson.EMPTY) }
    val centerTo = remember { MutableLiveData(thun) }
    val zoomLevel = remember { MutableLiveData(DEFAULT_ZOOM_LEVEL) }
    val camera =
        remember {
            CameraPosition
                .Builder()
                .target(thun)
                .zoom(DEFAULT_ZOOM_LEVEL)
                .build()
        }

    Column(modifier = Modifier.fillMaxSize()) {
        SBBSecondaryButton(
            modifier = Modifier.padding(8.dp),
            onClick = { geoJsonState.value = SBBGeoJson.EMPTY },
            label = "Remove GeoJson",
        )
        SBBSecondaryButton(
            modifier = Modifier.padding(8.dp),
            onClick = {
                geoJsonState.value =
                    SBBGeoJson(
                        geoJson = GeoJsonSamples.thunParkRideParking,
                        fillColor = Color.BLUE,
                        lineColor = Color.RED,
                        lineWidth = 3,
                    )
            },
            label = "Display custom GeoJson on Top",
        )
        SBBSecondaryButton(
            modifier = Modifier.padding(8.dp),
            onClick = {
                geoJsonState.value = SBBGeoJson(GeoJsonSamples.thunParkRideParking)
            },
            label = "Display SBB GeoJson on JourneyMaps",
        )
        SBBSecondaryButton(
            modifier = Modifier.padding(8.dp),
            onClick = {
                scope.launch {
                    centerTo.value = bern
                    delay(80)
                    centerTo.value = thun
                }
            },
            label = "Center to Thun Park & Ride",
        )

        Box(
            modifier =
                Modifier
                    .weight(1f)
                    .fillMaxSize()
                    .padding(8.dp),
        ) {
            SBBMapView(
                tilesApiKey = tilesApiKey,
                cameraPosition = camera,
                centerTo = centerTo,
                zoomLevel = zoomLevel,
                controls = SBBMapControls(
                    userLocationEnabled = false,
                    floorSwitchEnabled = false,
                ),
                poi = SBBMapPoi(
                    enabled = true,
                    geoJson = geoJsonState,
                ),
            )
        }
    }
}
