// SPDX-License-Identifier: MIT
// Copyright (c) 2024 Schweizerische Bundesbahnen AG (Swiss Federal Railways)

package ch.sbb.maps.map

import org.maplibre.android.geometry.LatLng
import org.maplibre.geojson.Feature

/**
 * Event callbacks for [SBBMapView].
 *
 * All callbacks are optional and default to null (no op).
 *
 * @property onMapClick triggered when the user taps an empty map area.
 * @property onPoiClick triggered when a POI is tapped (takes priority over [onMapClick]).
 * @property onMapLongClick triggered on a long press.
 * @property onCameraIdle triggered when the camera finishes moving.
 * @property onMoveBegin triggered when the user starts a pan/zoom gesture.
 */
public data class SBBMapCallbacks(
    val onMapClick: ((LatLng) -> Unit)? = null,
    val onPoiClick: ((Feature) -> Unit)? = null,
    val onMapLongClick: ((LatLng) -> Unit)? = null,
    val onCameraIdle: (() -> Unit)? = null,
    val onMoveBegin: (() -> Unit)? = null,
)
