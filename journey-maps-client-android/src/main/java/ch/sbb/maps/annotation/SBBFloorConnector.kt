// SPDX-License-Identifier: MIT
// Copyright (c) 2024 Schweizerische Bundesbahnen AG (Swiss Federal Railways)

package ch.sbb.maps.annotation

import android.graphics.Bitmap
import androidx.annotation.ColorInt
import org.maplibre.android.geometry.LatLng

/**
 * A floor transition indicator rendered as a symbol on the map.
 *
 * Floor connectors represent stairs, escalators, or elevators that connect
 * two floor levels. They are displayed as an icon at the transition point
 * and are visible only when [originFloor] matches the currently selected floor.
 *
 * @property id unique identifier used as the MapLibre layer/source suffix.
 * @property coordinates position of the connector on the map.
 * @property originFloor the floor from which the transition starts.
 * @property destinationFloor the floor to which the transition leads.
 * @property icon bitmap representing the connector type (stairs, elevator, etc.).
 * @property iconColor tint color applied to the icon.
 * @property minZoom minimum map zoom level at which the connector becomes visible.
 */
public data class SBBFloorConnector(
    val id: String,
    val coordinates: LatLng,
    val originFloor: Int,
    val destinationFloor: Int,
    val icon: Bitmap,
    @param:ColorInt val iconColor: Int,
    val minZoom: Double = 16.0,
)
