// SPDX-License-Identifier: MIT
// Copyright (c) 2024 Schweizerische Bundesbahnen AG (Swiss Federal Railways)

package ch.sbb.maps.annotation

import androidx.lifecycle.MutableLiveData
/**
 * Path overlay configuration for [ch.sbb.maps.map.SBBMapView].
 *
 * Provides observable lists of lines, circles, and floor connectors that are
 * rendered on the map. Changes to any of the [MutableLiveData] fields trigger
 * an immediate update of the corresponding map layers.
 *
 * @property lines observable list of [SBBLine] segments forming the path.
 * @property circles observable list of [SBBCircle] markers (start/end points).
 * @property floorConnectors observable list of [SBBFloorConnector] transition indicators.
 */
public data class SBBMapAnnotations(
    val lines: MutableLiveData<List<SBBLine>> = MutableLiveData(emptyList()),
    val circles: MutableLiveData<List<SBBCircle>> = MutableLiveData(emptyList()),
    val floorConnectors: MutableLiveData<List<SBBFloorConnector>> = MutableLiveData(emptyList()),
)
