// SPDX-License-Identifier: MIT
// Copyright (c) 2024 Schweizerische Bundesbahnen AG (Swiss Federal Railways)

package ch.sbb.maps.controls

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.unit.dp

/**
 * Controller overlay configuration for [ch.sbb.maps.map.SBBMapView].
 *
 * @property userLocationEnabled whether the location button is shown.
 * @property mapStyleSwitchEnabled whether the satellite/map style toggle is visible.
 * @property floorSwitchEnabled whether the indoor floor level switcher is visible.
 * @property compassEnabled whether the compass indicator is enabled.
 * @property alignment edge alignment via [SBBMapControlsAlignment].
 * @property padding additional padding around the overlay.
 */
public data class SBBMapControls(
    val userLocationEnabled: Boolean = true,
    val mapStyleSwitchEnabled: Boolean = true,
    val floorSwitchEnabled: Boolean = true,
    val compassEnabled: Boolean = true,
    val alignment: SBBMapControlsAlignment = SBBMapControlsAlignment.TopEnd,
    val padding: PaddingValues = PaddingValues(0.dp),
)
