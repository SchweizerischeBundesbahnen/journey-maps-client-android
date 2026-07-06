// SPDX-License-Identifier: MIT
// Copyright (c) 2024 Schweizerische Bundesbahnen AG (Swiss Federal Railways)

package ch.sbb.maps.userlocation

import android.Manifest
import android.content.Context
import android.location.LocationManager
import androidx.annotation.RequiresPermission
import androidx.core.content.ContextCompat
import ch.sbb.maps.util.SBBMapLogger
import org.maplibre.android.location.LocationComponentActivationOptions
import org.maplibre.android.location.LocationComponentOptions
import org.maplibre.android.location.modes.CameraMode
import org.maplibre.android.location.modes.RenderMode
import org.maplibre.android.maps.MapLibreMap

internal class SBBMapUserLocation(
    private val context: Context,
) {
    private val logger = SBBMapLogger(this::class.simpleName)
    private val permissionHandler = SBBMapPermissionHandler(context)

    internal companion object {
        const val LOCATION_PERMISSION_REQUEST_CODE: Int = 1
    }

    /**
     * @return if the map location has successfully been enabled
     */
    @RequiresPermission(anyOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    internal fun enable(map: MapLibreMap): Boolean {
        if (permissionHandler.checkLocationPermission()) {
            permissionHandler.requestPermission()
            return false
        }

        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            logger.error("enable", "GPS is disabled. Cannot enable map location.")
            return false
        }

        val style =
            map.style ?: run {
                logger.debug("enable", "Map style is null. Cannot activate location component.")
                return false
            }

        map.locationComponent.apply {
            if (isLocationComponentActivated && isLocationComponentEnabled) return true

            val options =
                LocationComponentOptions
                    .builder(context)
                    .pulseEnabled(true)
                    .pulseColor(ContextCompat.getColor(context, org.maplibre.android.R.color.maplibre_blue))
                    .build()

            activateLocationComponent(
                LocationComponentActivationOptions
                    .builder(context, style)
                    .locationComponentOptions(options)
                    .useDefaultLocationEngine(true)
                    .build(),
            )
            isLocationComponentEnabled = true
            cameraMode = CameraMode.TRACKING
            renderMode = RenderMode.NORMAL
            return true
        }
    }

    @RequiresPermission(anyOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    internal fun disable(map: MapLibreMap) {
        map.locationComponent.apply {
            if (!isLocationComponentActivated || permissionHandler.checkLocationPermission()) return
            isLocationComponentEnabled = false
        }
    }
}
