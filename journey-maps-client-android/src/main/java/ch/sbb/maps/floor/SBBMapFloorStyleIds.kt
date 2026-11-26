// SPDX-License-Identifier: MIT
// Copyright (c) 2024 Schweizerische Bundesbahnen AG (Swiss Federal Railways)

package ch.sbb.maps.floor

/** Identifiers of the map style that the floor switching relies on. */
internal object SBBMapFloorStyleIds {
    const val ROKAS_FLOOR_ID_SUFFIX = "-lvl"
    const val ROKAS_2D_ID_SUFFIX = "-2d"
    const val ROKAS_BACKGROUND_MASK_LAYER = "rokas_background_mask"
    const val LEVEL_GREYOUT_LAYER = "level_greyout"
    const val SERVICE_POINTS_SOURCE = "service_points"
    const val FLOOR_LIST_PROPERTY = "floor_liststring"

    /** Layer metadata key and the values marking a layer's floor role. */
    const val FLOOR_METADATA_KEY = "general.floor"
    const val FLOOR_METADATA_VALUE_LEVEL = "level"
    const val FLOOR_METADATA_VALUE_2D = "2D"

    /** Indoor and walk layers id prefixes for floors. */
    val FLOOR_LAYER_ID_PREFIXES = listOf("rokas_indoor", "geojson_walk", "rokas-walk")

    /** Layer types that can carry a floor filter. */
    val FILTERABLE_LAYER_TYPES = setOf("fill", "fill-extrusion", "line", "symbol")
}
