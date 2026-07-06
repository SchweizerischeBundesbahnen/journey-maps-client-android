// SPDX-License-Identifier: MIT
// Copyright (c) 2024 Schweizerische Bundesbahnen AG (Swiss Federal Railways)

package ch.sbb.maps.map

import android.os.Bundle
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.compose.LocalLifecycleOwner
import ch.sbb.maps.annotation.SBBGeoJson
import ch.sbb.maps.annotation.SBBMapAnnotations
import ch.sbb.maps.controls.SBBMapControls
import ch.sbb.maps.controls.SBBMapControlsOverlay
import ch.sbb.maps.controls.mapFabPaddingHorizontal
import ch.sbb.maps.controls.mapFabPaddingVertical
import ch.sbb.maps.controls.rememberAttributionMarginLeft
import ch.sbb.maps.poi.SBBMapPoi
import ch.sbb.maps.theme.SBBMapThemeManager
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapView

internal const val DEFAULT_ZOOM_LEVEL: Double = 12.0
internal val DEFAULT_CAMERA_POSITION_BERN: CameraPosition =
    CameraPosition
        .Builder()
        .target(LatLng(46.94881863, 7.43913775)) // Bern
        .zoom(DEFAULT_ZOOM_LEVEL)
        .build()

/**
 * The main composable entry point of the SBB Maps SDK.
 *
 * Displays an interactive [MapView] with optional user location,
 * map style switching, indoor floor switching, and POI interaction.
 *
 * @param tilesApiKey API key for the SBB Journey Maps Tiles service.
 * @param modifier [Modifier] applied to the map container.
 * @param uiSettings configures gesture and attribution behavior via [SBBMapUiSettings].
 * @param centerTo observable coordinate; the camera animates here when the value changes.
 * @param zoomLevel observable zoom level applied together with [centerTo].
 * @param cameraPosition initial [CameraPosition]; defaults to Bern if null.
 * @param controls overlay button configuration via [SBBMapControls].
 * @param poi POI configuration via [SBBMapPoi].
 * @param annotations path overlay configuration via [SBBMapAnnotations].
 * @param callbacks interaction event callbacks via [SBBMapCallbacks].
 */
@Composable
public fun SBBMapView(
    tilesApiKey: String,
    modifier: Modifier = Modifier,
    uiSettings: SBBMapUiSettings = SBBMapUiSettings(),
    centerTo: MutableLiveData<LatLng> = MutableLiveData(),
    zoomLevel: MutableLiveData<Double> = MutableLiveData(),
    cameraPosition: CameraPosition? = null,
    controls: SBBMapControls = SBBMapControls(),
    poi: SBBMapPoi = SBBMapPoi(),
    annotations: SBBMapAnnotations = SBBMapAnnotations(),
    callbacks: SBBMapCallbacks = SBBMapCallbacks(),
) {
    val isDarkTheme =
        remember {
            mutableStateOf(SBBMapThemeManager.isDarkMode.value)
        }
    val context = LocalContext.current
    val density = LocalDensity.current
    val layoutDirection = LocalLayoutDirection.current
    val controllerOverlayMarginsPx =
        with(density) {
            val extraHorizontal = mapFabPaddingHorizontal.roundToPx()
            val extraVertical = mapFabPaddingVertical.roundToPx()
            intArrayOf(
                controls.padding
                    .calculateLeftPadding(layoutDirection)
                    .roundToPx() + extraHorizontal,
                controls.padding.calculateTopPadding().roundToPx() + extraVertical,
                controls.padding
                    .calculateRightPadding(layoutDirection)
                    .roundToPx() + extraHorizontal,
                controls.padding.calculateBottomPadding().roundToPx() + extraVertical,
            )
        }
    val compassGravity = controls.alignment.toGravity()
    val attributionMarginLeft = rememberAttributionMarginLeft()
    val sbbMap =
        remember {
            SBBMap(
                context,
                tilesApiKey,
                cameraPosition = cameraPosition ?: DEFAULT_CAMERA_POSITION_BERN,
                poiEnabled = poi.enabled,
                compassEnabled = controls.compassEnabled,
                attributionMarginLeft = attributionMarginLeft,
                mapStyle = if (isDarkTheme.value) SBBMapStyle.DARK else SBBMapStyle.BRIGHT,
                compassMarginsPx = controllerOverlayMarginsPx,
                compassGravity = compassGravity,
                uiSettings = uiSettings,
            ).apply {
                onMapClickCallback = callbacks.onMapClick
                onMapLongClickCallback = callbacks.onMapLongClick
                onCameraIdleCallback = callbacks.onCameraIdle
                onPoiClickCallback = callbacks.onPoiClick
                onMoveBeginCallback = callbacks.onMoveBegin
            }
        }
    val sbbMapView = rememberSBBMapViewWithLifecycle(sbbMap)
    val currentSelectedPoi = poi.selectedPoi?.value
    val newCoordinates by centerTo.observeAsState()
    val newZoomLevel by zoomLevel.observeAsState(DEFAULT_ZOOM_LEVEL)
    val newPoiSubcategories by poi.subcategories.observeAsState(emptyList())
    val newGeoJson by poi.geoJson.observeAsState(SBBGeoJson.EMPTY)
    val newPathLines by annotations.lines.observeAsState(emptyList())
    val newPathCircles by annotations.circles.observeAsState(emptyList())
    val newPathFloorConnectors by annotations.floorConnectors.observeAsState(emptyList())

    LaunchedEffect(newPoiSubcategories) {
        if (poi.enabled) sbbMap.updatePoiSubcategories(newPoiSubcategories ?: emptyList())
    }
    LaunchedEffect(newCoordinates) {
        sbbMap.centerToCoordinates(
            newCoordinates = newCoordinates,
            zoomLevel = newZoomLevel,
        )
    }
    LaunchedEffect(newZoomLevel) {
        sbbMap.updateZoomLevel(newZoomLevel)
    }
    LaunchedEffect(currentSelectedPoi) {
        if (currentSelectedPoi == null) sbbMap.poiController?.deselectPoi()
    }
    LaunchedEffect(newGeoJson) {
        sbbMap.updateGeoJson(newGeoJson ?: SBBGeoJson.EMPTY)
    }
    LaunchedEffect(newPathLines) {
        sbbMap.updatePathLines(newPathLines)
    }
    LaunchedEffect(newPathCircles) {
        sbbMap.updatePathCircles(newPathCircles)
    }
    LaunchedEffect(newPathFloorConnectors) {
        sbbMap.updatePathFloorConnectors(newPathFloorConnectors)
    }
    Box {
        AndroidView(
            factory = { sbbMapView },
            modifier = modifier,
        )

        SBBMapControlsOverlay(
            sbbMap = sbbMap,
            userLocationEnabled = controls.userLocationEnabled,
            mapStyleSwitchEnabled = controls.mapStyleSwitchEnabled,
            floorSwitchEnabled = controls.floorSwitchEnabled,
            compassEnabled = controls.compassEnabled,
            alignment = controls.alignment,
            padding = controls.padding,
        )
    }
}

@Composable
internal fun rememberSBBMapViewWithLifecycle(sbbMap: SBBMap): MapView {
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val mapView = sbbMap.mapView

    DisposableEffect(lifecycle) {
        val lifecycleObserver = getMapLifecycleObserver(mapView) { sbbMap.initialize() }
        lifecycle.addObserver(lifecycleObserver)
        onDispose {
            lifecycle.removeObserver(lifecycleObserver)
            mapView.onDestroy()
        }
    }

    return mapView
}

internal fun getMapLifecycleObserver(
    mapView: MapView,
    onCreated: () -> Unit,
): LifecycleEventObserver =
    LifecycleEventObserver { _, event ->
        when (event) {
            Lifecycle.Event.ON_CREATE -> {
                mapView.onCreate(Bundle())
                onCreated()
            }

            Lifecycle.Event.ON_START -> {
                mapView.onStart()
            }

            Lifecycle.Event.ON_RESUME -> {
                mapView.onResume()
            }

            Lifecycle.Event.ON_PAUSE -> {
                mapView.onPause()
            }

            Lifecycle.Event.ON_STOP -> {
                mapView.onStop()
            }

            Lifecycle.Event.ON_DESTROY -> {
                mapView.onDestroy()
            }

            else -> {
                error("unhandled lifecycle event $event")
            }
        }
    }
