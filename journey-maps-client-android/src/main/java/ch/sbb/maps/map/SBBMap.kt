// SPDX-License-Identifier: MIT
// Copyright (c) 2024 Schweizerische Bundesbahnen AG (Swiss Federal Railways)

package ch.sbb.maps.map

import android.Manifest
import android.content.Context
import androidx.annotation.RequiresPermission
import androidx.lifecycle.MutableLiveData
import ch.sbb.maps.annotation.SBBCircle
import ch.sbb.maps.annotation.SBBFloorConnector
import ch.sbb.maps.annotation.SBBGeoJson
import ch.sbb.maps.annotation.SBBLine
import ch.sbb.maps.annotation.SBBMapAnnotationsController
import ch.sbb.maps.floor.SBBMapFloorSwitchController
import ch.sbb.maps.poi.SBBMapPoiController
import ch.sbb.maps.theme.SBBMapThemeManager
import ch.sbb.maps.userlocation.SBBMapUserLocation
import ch.sbb.maps.util.SBBMapLogger
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.maplibre.android.MapLibre
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.gestures.MoveGestureDetector
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapLibreMap.OnMoveListener
import org.maplibre.android.maps.MapView
import org.maplibre.geojson.Feature

internal class SBBMap(
    context: Context,
    private val tilesApiKey: String,
    private val cameraPosition: CameraPosition,
    private val poiEnabled: Boolean,
    private val compassEnabled: Boolean,
    private val attributionMarginLeft: Int,
    private val mapStyle: SBBMapStyle = SBBMapStyle.BRIGHT,
    private val compassMarginsPx: IntArray, // intArrayOf(left, top, right, bottom) in pixels
    private val compassGravity: Int, // e.g. Gravity.TOP or Gravity.END
    private val uiSettings: SBBMapUiSettings = SBBMapUiSettings(),
) {
    private val logger = SBBMapLogger(this::class.simpleName)

    private val mapLibreMap = CompletableDeferred<MapLibreMap>()
    private val mapStyleLoaded = CompletableDeferred<Unit>()

    internal val mapView: MapView

    init {
        logger.debug("init", "Initializing MapLibre instance")
        MapLibre.getInstance(context)
        mapView = MapView(context)
    }

    internal fun initialize() {
        mapView.onDidFinishLoadingStyle(mapStyleLoaded)
        mapView.getMapAsync { map -> initializeMap(map) }
    }

    /** Used by [ch.sbb.maps.map.SBBMapView] within the SDK. */
    internal var poiController: SBBMapPoiController? = null
    internal var pathController: SBBMapAnnotationsController? = null

    internal val floors: MutableLiveData<List<Int>> = MutableLiveData(emptyList())
    internal var isCompassActivated: MutableLiveData<Boolean> = MutableLiveData(true)

    private val userLocation = SBBMapUserLocation(context)

    private val isDarkMode = MutableLiveData(SBBMapThemeManager.isDarkMode.value)
    private val isSatelliteView = MutableLiveData(false)

    private var floorSwitch: SBBMapFloorSwitchController? = null
    private val selectedFloor = MutableLiveData(0)
    private val selectedCategories = MutableLiveData<List<String>>(emptyList())
    private var displayedGeoJson: SBBGeoJson = SBBGeoJson.EMPTY

    internal var onMapClickCallback: ((LatLng) -> Unit)? = null
    internal var onMapLongClickCallback: ((LatLng) -> Unit)? = null
    internal var onCameraIdleCallback: (() -> Unit)? = null
    internal var onMoveBeginCallback: (() -> Unit)? = null
    internal var onPoiClickCallback: ((Feature) -> Unit)? = null

    private fun initializeMap(map: MapLibreMap) {
        logger.debug("initializeMap", "Initializing Map")

        mapLibreMap.complete(map)
        poiController = SBBMapPoiController(map)
        pathController = SBBMapAnnotationsController(map)
        floorSwitch = SBBMapFloorSwitchController(map)

        setupUiSettings(map)

        cameraPosition.let { map.cameraPosition = it }

        arrangeListeners(map)

        CoroutineScope(Dispatchers.Main).launch {
            updateMapStyle(mapStyle)
        }
    }

    private fun setupUiSettings(map: MapLibreMap) {
        map.uiSettings.isLogoEnabled = false
        map.uiSettings.isAttributionEnabled = uiSettings.showAttribution
        map.uiSettings.isCompassEnabled = compassEnabled
        map.uiSettings.setAttributionMargins(attributionMarginLeft, 0, 0, 0)
        map.uiSettings.compassGravity = compassGravity
        map.uiSettings.setCompassMargins(
            compassMarginsPx[0],
            compassMarginsPx[1],
            compassMarginsPx[2],
            compassMarginsPx[3],
        )
        // Gesture controls
        map.uiSettings.isZoomGesturesEnabled = uiSettings.enableZoomGestures
        map.uiSettings.isScrollGesturesEnabled = uiSettings.enableScrollGestures
        map.uiSettings.isRotateGesturesEnabled = uiSettings.enableRotateGestures
        map.uiSettings.isTiltGesturesEnabled = uiSettings.enableTiltGestures
        map.uiSettings.isDoubleTapGesturesEnabled = uiSettings.enableDoubleTapGestures
    }

    private fun arrangeListeners(map: MapLibreMap) {
        map.addOnCameraMoveListener {
            isCompassActivated.value = map.cameraPosition.bearing != 0.0
        }
        map.addOnCameraIdleListener {
            if (poiEnabled) {
                poiController?.deselectPoi()
            }
            floors.postValue(floorSwitch?.floors?.value)
            if (floors.value.isNullOrEmpty()) {
                updateSelectedFloor(0)
            } else {
                selectedFloor.value?.let { updateSelectedFloor(it) }
            }
            onCameraIdleCallback?.invoke()
        }
        map.addOnMapClickListener { point ->
            val clickedPoi = poiController?.setupPoiClickEvent(point)
            if (clickedPoi != null && poiEnabled) {
                onPoiClickCallback?.invoke(clickedPoi)
            } else {
                onMapClickCallback?.invoke(point)
            }
            true
        }
        map.addOnMapLongClickListener { point ->
            onMapLongClickCallback?.invoke(point)
            true
        }
        map.addOnMoveListener(
            object : OnMoveListener {
                override fun onMoveBegin(p0: MoveGestureDetector) {
                    onMoveBeginCallback?.invoke()
                }

                override fun onMove(p0: MoveGestureDetector) {
                    // not implemented
                }

                override fun onMoveEnd(p0: MoveGestureDetector) {
                    // not implemented
                }
            },
        )
    }

    private suspend fun getMap(): MapLibreMap = mapLibreMap.await()

    private suspend fun updateMapStyle(mapStyle: SBBMapStyle) {
        logger.debug("updateMapStyle", "Updating map style")
        getMap().setStyle(
            mapStyle.getStyleUrl(tilesApiKey),
        ) {
            poiController?.setPoiResourcesVisible(
                enablePoi = poiEnabled,
                poiFilterSubCategories = selectedCategories.value,
            )
            SBBGeoJson.addToStyle(it, displayedGeoJson)
        }
    }

    internal suspend fun toggleSatelliteView(newIsSatelliteView: Boolean) {
        isSatelliteView.value = newIsSatelliteView
        updateMapStyle(
            when {
                newIsSatelliteView -> SBBMapStyle.SATELLITE
                isDarkMode.value == true -> SBBMapStyle.DARK
                else -> SBBMapStyle.BRIGHT
            },
        )
    }

    /**
     * @return if the map location has successfully been enabled
     */
    @RequiresPermission(anyOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    internal suspend fun enableMapLocation(): Boolean {
        val map = getMap()
        mapStyleLoaded.await()
        return userLocation.enable(map)
    }

    @RequiresPermission(anyOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    internal suspend fun disableMapLocation() {
        userLocation.disable(getMap())
    }

    internal fun updateSelectedFloor(floor: Int) {
        selectedFloor.postValue(floor)
        floorSwitch?.getAvailableFloorFilters(floor)
        pathController?.updateFloorVisibility(floor)
    }

    internal suspend fun updateZoomLevel(newZoomLevel: Double) {
        val map = getMap()
        val currentPosition = this.cameraPosition
        val newCameraPosition =
            CameraPosition
                .Builder()
                .target(currentPosition.target)
                .zoom(newZoomLevel)
                .build()
        map.animateCamera(CameraUpdateFactory.newCameraPosition(newCameraPosition))
    }

    internal fun updatePoiSubcategories(newSelectedCategories: List<String>) {
        poiController?.updateSubcategories(newSelectedCategories)
        selectedCategories.postValue(newSelectedCategories)
    }

    internal suspend fun updateGeoJson(newGeoJson: SBBGeoJson) {
        mapStyleLoaded.await()
        val map = getMap()
        val old = displayedGeoJson
        displayedGeoJson = newGeoJson
        map.getStyle { style ->
            SBBGeoJson.removeFromStyle(style, old)
            SBBGeoJson.addToStyle(style, newGeoJson)
        }
    }

    internal suspend fun updatePathLines(lines: List<SBBLine>) {
        mapStyleLoaded.await()
        getMap()
        pathController?.updateLines(lines, selectedFloor.value ?: 0)
    }

    internal suspend fun updatePathCircles(circles: List<SBBCircle>) {
        mapStyleLoaded.await()
        getMap()
        pathController?.updateCircles(circles, selectedFloor.value ?: 0)
    }

    internal suspend fun updatePathFloorConnectors(connectors: List<SBBFloorConnector>) {
        mapStyleLoaded.await()
        getMap()
        pathController?.updateFloorConnectors(connectors, selectedFloor.value ?: 0)
    }

    internal suspend fun centerToCoordinates(
        newCoordinates: LatLng?,
        zoomLevel: Double,
    ) {
        val map = getMap()
        mapStyleLoaded.await()
        if (newCoordinates != null) {
            mapView.onDidBecomeIdleOnce {
                val nearestPoi = poiController?.setupPoiClickEvent(newCoordinates)
                if (nearestPoi != null) {
                    onPoiClickCallback?.invoke(nearestPoi)
                }
            }
            map.animateCamera(
                CameraUpdateFactory.newLatLngZoom(
                    newCoordinates,
                    zoomLevel,
                ),
            )
        }
    }
}

private fun MapView.onDidBecomeIdleOnce(action: () -> Unit) {
    addOnDidBecomeIdleListener(
        object : MapView.OnDidBecomeIdleListener {
            override fun onDidBecomeIdle() {
                removeOnDidBecomeIdleListener(this)
                action()
            }
        },
    )
}

private fun MapView.onDidFinishLoadingStyle(mapStyleLoaded: CompletableDeferred<Unit>) {
    addOnDidFinishLoadingStyleListener {
        mapStyleLoaded.complete(Unit)
    }
}
