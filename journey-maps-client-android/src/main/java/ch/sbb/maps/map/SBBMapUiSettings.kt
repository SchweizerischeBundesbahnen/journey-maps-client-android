// SPDX-License-Identifier: MIT
// Copyright (c) 2024 Schweizerische Bundesbahnen AG (Swiss Federal Railways)

package ch.sbb.maps.map

/**
 * UI related map settings (attribution visibility and gesture controls).
 * Pass an instance to [ch.sbb.maps.map.SBBMapView] via the uiSettings parameter.
 *
 */
public data class SBBMapUiSettings(
    val showAttribution: Boolean = true,
    val enableZoomGestures: Boolean = true,
    val enableScrollGestures: Boolean = true,
    val enableRotateGestures: Boolean = true,
    val enableTiltGestures: Boolean = true,
    val enableDoubleTapGestures: Boolean = true,
)
