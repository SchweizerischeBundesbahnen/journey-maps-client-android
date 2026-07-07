// SPDX-License-Identifier: MIT
// Copyright (c) 2024 Schweizerische Bundesbahnen AG (Swiss Federal Railways)

package ch.sbb.maps.android.example.ui.poi

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetValue
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.lifecycle.MutableLiveData
import ch.sbb.maps.android.example.SBBMapExampleCoordinates
import ch.sbb.maps.poi.SBBPoiCategoryType
import ch.sbb.maps.design.SBBBottomSheetContent
import ch.sbb.maps.design.SBBBottomSheetScaffold
import ch.sbb.maps.design.SBBColors
import ch.sbb.maps.design.SBBSecondaryButton
import ch.sbb.maps.design.SBBTypography
import ch.sbb.maps.map.SBBMapCallbacks
import ch.sbb.maps.controls.SBBMapControls
import ch.sbb.maps.poi.SBBMapPoi
import ch.sbb.maps.map.SBBMapView
import ch.sbb.maps.poi.SBBPoiSelection
import kotlinx.coroutines.launch
import org.maplibre.android.geometry.LatLng
import org.maplibre.geojson.Feature

val DEFAULT_POI_FILTER_SUB_CATEGORIES: List<String> =
    listOf(
        SBBPoiCategoryType.PARK_RAIL.value,
        SBBPoiCategoryType.PARKING_PLACE.value,
        SBBPoiCategoryType.CAR_SHARING.value,
        SBBPoiCategoryType.P2P_CAR_SHARING.value,
        SBBPoiCategoryType.BIKE_PARKING.value,
        SBBPoiCategoryType.BIKE_SHARING.value,
        SBBPoiCategoryType.ON_DEMAND.value,
    )
const val DEFAULT_POI_ZOOM_LEVEL: Double = 16.0

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PoiMapView(
    tilesApiKey: String,
    enableSubcategorySelection: Boolean = false,
) {
    val coroutineScope = rememberCoroutineScope()
    val sheetState =
        rememberStandardBottomSheetState(
            SheetValue.Hidden,
            skipHiddenState = false,
        )
    val bottomSheetScaffoldState =
        rememberBottomSheetScaffoldState(
            bottomSheetState = sheetState,
        )
    val selectedPoi = remember { mutableStateOf<Feature?>(null) }
    val clickedPoint = remember { mutableStateOf<LatLng?>(null) }
    val newCoordinates = remember { MutableLiveData(SBBMapExampleCoordinates.mainStationBern) }
    val zoomLevel = remember { MutableLiveData(DEFAULT_POI_ZOOM_LEVEL) }
    val selectedPoiSubcategories =
        remember { MutableLiveData(DEFAULT_POI_FILTER_SUB_CATEGORIES) }

    LaunchedEffect(key1 = selectedPoi.value, key2 = clickedPoint.value) {
        if (selectedPoi.value != null || clickedPoint.value != null) {
            bottomSheetScaffoldState.bottomSheetState.expand()
        } else {
            bottomSheetScaffoldState.bottomSheetState.hide()
        }
    }

    SBBBottomSheetScaffold(
        bottomSheetScaffoldState = bottomSheetScaffoldState,
        sheetContent = {
            SBBBottomSheetContent(
                title = if (selectedPoi.value != null) "POI Details: " else "Geometry Details :",
                body = {
                    if (selectedPoi.value != null || clickedPoint.value != null) {
                        PoiDetails(
                            feature = selectedPoi.value,
                            geometry = clickedPoint.value,
                        )
                    }
                },
                onCloseSheet = {
                    coroutineScope.launch {
                        bottomSheetScaffoldState.bottomSheetState.hide()
                        selectedPoi.value = null
                        clickedPoint.value = null
                    }
                },
                buttonLabel = "Close",
            )
        },
    ) {
        Column {
            if (enableSubcategorySelection) {
                SBBPoiSelection(
                    onSelectionChange = { newPoiSubcategories ->
                        selectedPoiSubcategories.postValue(newPoiSubcategories)
                    },
                )
            }
            if (!enableSubcategorySelection) {
                Row(modifier = Modifier.background(SBBColors.red)) {
                    SBBSecondaryButton(
                        modifier = Modifier.weight(1.0f).background(SBBColors.transparent),
                        onClick = {
                            newCoordinates.postValue(LatLng(SBBMapExampleCoordinates.bikeParkingBern))
                        },
                        label = "Velostation Bern",
                    )
                    SBBSecondaryButton(
                        modifier = Modifier.weight(1.0f),
                        onClick = {
                            newCoordinates.postValue(LatLng(SBBMapExampleCoordinates.bikeParkingBridgeBern))
                        },
                        label = "Velostation Schanzenbruecke",
                        textStyle = SBBTypography.labelSmall.copy(SBBColors.red),
                    )
                }
            }

            SBBMapView(
                tilesApiKey = tilesApiKey,
                centerTo = newCoordinates,
                zoomLevel = zoomLevel,
                controls = SBBMapControls(
                    floorSwitchEnabled = false,
                    userLocationEnabled = false,
                ),
                poi = SBBMapPoi(
                    enabled = true,
                    selectedPoi = selectedPoi,
                    subcategories = selectedPoiSubcategories,
                ),
                callbacks = SBBMapCallbacks(
                    onCameraIdle = {
                        selectedPoi.value = null
                        clickedPoint.value = null
                        newCoordinates.value = null
                    },
                    onMapClick = { latLng ->
                        clickedPoint.value = latLng
                        selectedPoi.value = null
                    },
                    onPoiClick = { feature ->
                        selectedPoi.value = feature
                        clickedPoint.value = null
                    },
                ),
            )
        }
    }
}
