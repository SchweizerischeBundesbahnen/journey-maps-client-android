// SPDX-License-Identifier: MIT
// Copyright (c) 2024 Schweizerische Bundesbahnen AG (Swiss Federal Railways)

package ch.sbb.maps.controls

import android.Manifest
import android.annotation.SuppressLint
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.lifecycle.viewmodel.compose.viewModel
import ch.sbb.maps.R
import ch.sbb.maps.floor.SBBMapFloorSwitch
import ch.sbb.maps.map.SBBMap

@SuppressLint("MissingPermission")
@Composable
internal fun SBBMapControlsOverlay(
    sbbMap: SBBMap,
    userLocationEnabled: Boolean = true,
    mapStyleSwitchEnabled: Boolean = true,
    floorSwitchEnabled: Boolean = true,
    compassEnabled: Boolean = true,
    alignment: SBBMapControlsAlignment,
    padding: PaddingValues,
) {
    val viewModel: SBBMapControlsViewModel =
        viewModel(factory = SBBMapControlsViewModel.Factory(sbbMap))

    // ui changes
    val isMyLocationEnabled by viewModel.isMyLocationEnabled.observeAsState(initial = false)
    val isLocationPermissionGranted by viewModel.isLocationPermissionGranted.observeAsState(initial = false)

    // map changes
    val isCompassActivated by sbbMap.isCompassActivated.observeAsState(initial = false)
    val floors by sbbMap.floors.observeAsState()

    val requestPermissionAndSetMyLocationEnabledLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission(),
        ) { isGranted ->
            viewModel.isLocationPermissionGranted.value = isGranted
            viewModel.setMyLocationEnabled(isGranted)
        }

    LaunchedEffect(userLocationEnabled) {
        if (!isLocationPermissionGranted && userLocationEnabled) {
            requestPermissionAndSetMyLocationEnabledLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        } else {
            viewModel.setMyLocationEnabled(userLocationEnabled)
        }
    }

    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(padding),
    ) {
        val isBottomAligned = alignment.isBottomAligned()
        val reserveCompassInset = compassEnabled && isCompassActivated
        Column(
            modifier =
                Modifier
                    .align(alignment.toAlignment())
                    .wrapContentWidth(),
        ) {
            if (!isBottomAligned && reserveCompassInset) {
                MapCompassInsetSpacer()
            }
            if (userLocationEnabled) {
                SBBMapFloatingActionButton(
                    onClick = {
                        if (isLocationPermissionGranted) {
                            viewModel.toggleMyLocation()
                        } else {
                            requestPermissionAndSetMyLocationEnabledLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                        }
                    },
                    icon =
                        if (isMyLocationEnabled) {
                            ImageVector.vectorResource(R.drawable.arrow_compass_filled_small)
                        } else {
                            ImageVector.vectorResource(R.drawable.arrow_compass_small)
                        },
                )
            }

            if (mapStyleSwitchEnabled) {
                SBBMapFloatingActionButton(
                    onClick = viewModel::toggleSatelliteView,
                    icon = ImageVector.vectorResource(R.drawable.layers_small),
                )
            }

            if (floorSwitchEnabled) {
                if (floors?.isNotEmpty() == true) {
                    SBBMapFloorSwitch(
                        floors = floors!!,
                        onFloorSelected = { floor ->
                            viewModel.updateMapFloorLayers(floor)
                        },
                    )
                } else {
                    viewModel.updateMapFloorLayers(0)
                }
            }
            if (isBottomAligned && reserveCompassInset) {
                MapCompassInsetSpacer()
            }
        }
    }
}
