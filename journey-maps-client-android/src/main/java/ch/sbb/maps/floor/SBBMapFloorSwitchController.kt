// SPDX-License-Identifier: MIT
// Copyright (c) 2024 Schweizerische Bundesbahnen AG (Swiss Federal Railways)

package ch.sbb.maps.floor

import androidx.lifecycle.MutableLiveData
import ch.sbb.maps.floor.SBBMapFloorStyleIds.FLOOR_LIST_PROPERTY
import ch.sbb.maps.floor.SBBMapFloorStyleIds.SERVICE_POINTS_SOURCE
import com.google.gson.JsonObject
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.Style
import org.maplibre.android.style.expressions.Expression
import org.maplibre.android.style.sources.GeoJsonSource

/**
 * Coordinates floor switching. For the selected floor it identifies the floor layers
 * ([SBBMapFloorStyleLayers]), rewrites their filters ([SBBMapFloorFilter]), and updates
 * their visibility ([SBBMapFloorVisibility]).
 *
 * [floors] exposes the floors available at the current location.
 */
internal class SBBMapFloorSwitchController(
    private val map: MapLibreMap,
) {
    internal val floors: MutableLiveData<List<Int>> = MutableLiveData(emptyList())

    private val visibility = SBBMapFloorVisibility()
    private var cachedStyle: Style? = null

    internal fun getAvailableFloorFilters(
        selectedFloor: Int,
        poiEnabled: Boolean,
        poiLayerIds: Set<String>,
    ) {
        refreshFloors()
        map.getStyle { style ->
            if (cachedStyle !== style) {
                cachedStyle = style
                visibility.reset()
            }

            val layers = SBBMapFloorStyleLayers.parse(style)
            applyFloorFilters(style, layers, selectedFloor)

            val indoorEnabled = floors.value?.isNotEmpty() == true || selectedFloor != 0
            visibility.applyLevelAnd2d(style, layers, indoorEnabled, poiEnabled, poiLayerIds)
            visibility.applyBackgroundMasks(style, indoorEnabled, selectedFloor)
        }
    }

    private fun applyFloorFilters(
        style: Style,
        layers: List<JsonObject>,
        selectedFloor: Int,
    ) {
        SBBMapFloorStyleLayers.filterableFloorIds(layers).forEach { layerId ->
            val layer = style.getLayer(layerId) ?: return@forEach
            val filter = layer.floorFilter ?: return@forEach
            val parsed = SBBMapFloorFilter.parse(filter) ?: return@forEach
            layer.floorFilter = Expression.raw(SBBMapFloorFilter.withFloor(parsed, selectedFloor).toString())
        }
    }

    private fun refreshFloors() {
        val source = map.style?.getSourceAs<GeoJsonSource>(SERVICE_POINTS_SOURCE)
        val newFloors =
            source
                ?.querySourceFeatures(Expression.has(FLOOR_LIST_PROPERTY))
                ?.flatMap { feature ->
                    feature
                        .properties()
                        ?.get(FLOOR_LIST_PROPERTY)
                        ?.asString
                        ?.split(",")
                        ?.mapNotNull { it.toIntOrNull() }
                        ?: emptyList()
                }?.distinct() ?: emptyList()
        floors.value = newFloors
    }
}
