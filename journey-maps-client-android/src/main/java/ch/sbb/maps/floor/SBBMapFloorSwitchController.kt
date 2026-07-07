// SPDX-License-Identifier: MIT
// Copyright (c) 2024 Schweizerische Bundesbahnen AG (Swiss Federal Railways)

package ch.sbb.maps.floor

import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.style.expressions.Expression
import org.maplibre.android.style.layers.CircleLayer
import org.maplibre.android.style.layers.FillExtrusionLayer
import org.maplibre.android.style.layers.FillLayer
import org.maplibre.android.style.layers.HeatmapLayer
import org.maplibre.android.style.layers.Layer
import org.maplibre.android.style.layers.LineLayer
import org.maplibre.android.style.layers.Property
import org.maplibre.android.style.layers.PropertyFactory
import org.maplibre.android.style.layers.SymbolLayer
import org.maplibre.android.style.sources.GeoJsonSource

internal class SBBMapFloorSwitchController(
    private val map: MapLibreMap,
) {
    internal val floors: MutableLiveData<List<Int>> = MutableLiveData(emptyList())

    private val gson = Gson()

    internal fun getAvailableFloorFilters(selectedFloor: Int) {
        floors.value = getAvailableFloors().value
        map.style
            ?.layers
            ?.filter { layer ->
                layer.id.endsWith(ROKAS_FLOOR_ID_SUFFIX) ||
                    layer.id.startsWith(ROKAS_INDOOR_PREFIX) ||
                    layer.id.startsWith(ROKAS_GEOJSON_WALK_PREFIX) ||
                    layer.id.startsWith(ROKAS_WALK_PREFIX) ||
                    layer.id.startsWith(ROKAS_BACKGROUND_MASK_PREFIX)
            }?.map { layer -> layer.id }
            ?.also { knownLvlLayerIds ->
                updateMapLayers(knownLvlLayerIds, selectedFloor)
            }
    }

    private fun getAvailableFloors(): MutableLiveData<List<Int>> {
        var newFloors: List<Int> = emptyList()

        val geoJsonSource = map.style?.getSourceAs<GeoJsonSource>(SERVICE_POINTS_SOURCE)
        geoJsonSource?.let { source ->
            val features = source.querySourceFeatures(Expression.has(FLOOR_LIST_PROPERTY))
            newFloors =
                features
                    .flatMap { feature ->
                        feature
                            .properties()
                            ?.get(FLOOR_LIST_PROPERTY)
                            ?.asString
                            ?.split(",")
                            ?.mapNotNull { it.toIntOrNull() }
                            ?: emptyList()
                    }.distinct()
        }
        floors.postValue(newFloors)

        return floors
    }

    private fun updateMapLayers(
        knownLvlLayerIds: List<String>,
        selectedFloor: Int,
    ) {
        map.getStyle { style ->
            knownLvlLayerIds.forEach { layerId ->
                val layer = style.getLayer(layerId)
                if (layer?.id?.startsWith(ROKAS_BACKGROUND_MASK_PREFIX) == true) {
                    layer.setProperties(
                        PropertyFactory.visibility(
                            if (selectedFloor < 0) Property.VISIBLE else Property.NONE,
                        ),
                    )
                }

                layer?.let { currentLayer ->
                    val newFilter = getNewFilter(currentLayer, selectedFloor)

                    newFilter.let {
                        when (currentLayer) {
                            is SymbolLayer -> currentLayer.withFilter(it)
                            is CircleLayer -> currentLayer.withFilter(it)
                            is HeatmapLayer -> currentLayer.withFilter(it)
                            is FillLayer -> currentLayer.withFilter(it)
                            is FillExtrusionLayer -> currentLayer.withFilter(it)
                            is LineLayer -> currentLayer.withFilter(it)
                        }
                    }
                }
            }
        }
    }

    private fun getNewFilter(
        currentLayer: Layer,
        selectedFloor: Int,
    ): Expression {
        val oldFilter =
            when (currentLayer) {
                is SymbolLayer -> currentLayer.filter
                is CircleLayer -> currentLayer.filter
                is HeatmapLayer -> currentLayer.filter
                is FillLayer -> currentLayer.filter
                is FillExtrusionLayer -> currentLayer.filter
                is LineLayer -> currentLayer.filter
                else -> null
            } ?: return Expression.literal(true)

        val oldFilterList = oldFilter.toArray().toList()
        val newFilter = mutableListOf<Any>()
        newFilter.add(oldFilterList.firstOrNull() ?: return Expression.literal(true))

        var floorFound = false

        oldFilterList.drop(1).forEach { item ->
            when (item) {
                is String -> {
                    if (isFloorFilter(item)) {
                        floorFound = true
                        newFilter.add(item)
                    } else {
                        newFilter.add(item)
                    }
                }

                is Array<*> -> {
                    val newInnerPart = mutableListOf<Any>()
                    newInnerPart.add(item.firstOrNull() ?: return Expression.literal(true))
                    var levelFound = false

                    item.drop(1).forEach { innerPart ->
                        val innerPartString = gson.toJson(innerPart)
                        when {
                            isCaseLevelFilter(innerPartString) -> {
                                levelFound = true
                                innerPart?.let { newInnerPart.add(it) }
                            }

                            isFloorFilter(innerPartString) -> {
                                levelFound = true
                                floorFound = true
                                innerPart?.let { newInnerPart.add(it) }
                            }

                            levelFound -> {
                                levelFound = false
                                newInnerPart.add(selectedFloor)
                            }

                            else -> {
                                innerPart?.let { newInnerPart.add(it) }
                            }
                        }
                    }
                    newFilter.add(newInnerPart)
                }

                else -> {
                    if (floorFound) {
                        floorFound = false
                        newFilter.add(selectedFloor)
                    } else {
                        newFilter.add(item)
                    }
                }
            }
        }

        val filterString = gson.toJson(newFilter)
        return Expression.raw(filterString)
    }

    private fun isFloorFilter(innerPartString: String): Boolean = innerPartString.contains(FLOOR)

    private fun isCaseLevelFilter(innerPartString: String): Boolean =
        innerPartString.startsWith("[\"case\",[\"has\",\"level\"],[\"get\",\"level\"]")

    companion object SBBMapFloorSwitchControllerConsts {
        const val ROKAS_FLOOR_ID_SUFFIX = "-lvl"
        const val ROKAS_INDOOR_PREFIX = "rokas_indoor"
        const val ROKAS_GEOJSON_WALK_PREFIX = "geojson_walk"
        const val ROKAS_WALK_PREFIX = "rokas-walk"
        const val ROKAS_BACKGROUND_MASK_PREFIX = "rokas_background_mask"
        const val SERVICE_POINTS_SOURCE = "service_points"
        const val FLOOR_LIST_PROPERTY = "floor_liststring"
        const val FLOOR = "floor"
    }
}
