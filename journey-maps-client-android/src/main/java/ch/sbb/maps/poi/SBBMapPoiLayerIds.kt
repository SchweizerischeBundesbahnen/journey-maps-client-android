// SPDX-License-Identifier: MIT
// Copyright (c) 2024 Schweizerische Bundesbahnen AG (Swiss Federal Railways)

package ch.sbb.maps.poi

/**
 * Centralizes the layer, source, and property ids used by POI handling: the POI
 * layers, their data source, and the ids involved in POI selection.
 */
internal object SBBMapPoiLayerIds {
    val ALL =
        listOf(
            "journey-pois",
            "journey-pois-first",
            "journey-pois-second-lvl",
            "journey-pois-third-lvl",
            "journey-pois-second-2d",
        )

    /** Layers that carry the sub-category filter. */
    val CATEGORY_FILTERABLE =
        listOf(
            "journey-pois-second-lvl",
            "journey-pois-first",
        )

    const val SELECTED_LAYER_PREFIX = "journey-pois-selected"
    const val SELECTED_MARKER = "marker_poi_bright_selected"
    const val SBB_ID = "sbbId"
    const val ICON_PROPERTY = "icon"
    const val SERVICE_POINTS_SOURCE = "service_points"
}
