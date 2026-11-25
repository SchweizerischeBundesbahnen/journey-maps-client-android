// SPDX-License-Identifier: MIT
// Copyright (c) 2024 Schweizerische Bundesbahnen AG (Swiss Federal Railways)

package ch.sbb.maps.floor

import androidx.lifecycle.MutableLiveData
import ch.sbb.maps.floor.SBBMapFloorStyleIds.FILTERABLE_LAYER_TYPES
import ch.sbb.maps.floor.SBBMapFloorStyleIds.FLOOR_LAYER_ID_PREFIXES
import ch.sbb.maps.floor.SBBMapFloorStyleIds.FLOOR_LIST_PROPERTY
import ch.sbb.maps.floor.SBBMapFloorStyleIds.FLOOR_METADATA_KEY
import ch.sbb.maps.floor.SBBMapFloorStyleIds.FLOOR_METADATA_VALUE_2D
import ch.sbb.maps.floor.SBBMapFloorStyleIds.FLOOR_METADATA_VALUE_LEVEL
import ch.sbb.maps.floor.SBBMapFloorStyleIds.LEVEL_GREYOUT_LAYER
import ch.sbb.maps.floor.SBBMapFloorStyleIds.ROKAS_2D_ID_SUFFIX
import ch.sbb.maps.floor.SBBMapFloorStyleIds.ROKAS_BACKGROUND_MASK_LAYER
import ch.sbb.maps.floor.SBBMapFloorStyleIds.ROKAS_FLOOR_ID_SUFFIX
import ch.sbb.maps.floor.SBBMapFloorStyleIds.SERVICE_POINTS_SOURCE
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.Style
import org.maplibre.android.style.expressions.Expression
import org.maplibre.android.style.layers.BackgroundLayer
import org.maplibre.android.style.layers.CircleLayer
import org.maplibre.android.style.layers.FillExtrusionLayer
import org.maplibre.android.style.layers.FillLayer
import org.maplibre.android.style.layers.HeatmapLayer
import org.maplibre.android.style.layers.Layer
import org.maplibre.android.style.layers.LineLayer
import org.maplibre.android.style.layers.Property
import org.maplibre.android.style.layers.PropertyFactory
import org.maplibre.android.style.layers.PropertyValue
import org.maplibre.android.style.layers.SymbolLayer
import org.maplibre.android.style.sources.GeoJsonSource

internal class SBBMapFloorSwitchController(
    private val map: MapLibreMap,
) {
    internal val floors: MutableLiveData<List<Int>> = MutableLiveData(emptyList())
    private var cachedStyle: Style? = null

    private val originalFilters = mutableMapOf<String, JsonArray>()

    private val originalOpacities = mutableMapOf<String, PropertyValue<Float>>()

    internal fun getAvailableFloorFilters(selectedFloor: Int) {
        floors.value = getAvailableFloors().value
        map.getStyle { style ->
            val styleLayers = parseStyleLayers(style)

            if (cachedStyle !== style) {
                cachedStyle = style
                originalFilters.clear()
                originalOpacities.clear()
            }

            collectFloorLayerIds(styleLayers).forEach { layerId ->
                val layer = style.getLayer(layerId) ?: return@forEach
                val original =
                    originalFilters.getOrPut(layerId) {
                        val filter = layer.filterOrNull ?: return@forEach
                        parseFilter(filter) ?: return@forEach
                    }
                layer.filterOrNull = Expression.raw(replaceFloor(original, selectedFloor).toString())
            }

            val indoorEnabled = floors.value?.isNotEmpty() == true || selectedFloor != 0

            updateTwoDAndLevelLayers(style, styleLayers, indoorEnabled)
            updateBackgroundOverlays(style, indoorEnabled, selectedFloor)
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
    private fun collectFloorLayerIds(styleLayers: List<JsonObject>): List<String> =
        styleLayers.mapNotNull { layerJson ->
            val id = layerJson.get("id")?.takeIf { it.isJsonPrimitive }?.asString ?: return@mapNotNull null
            val type = layerJson.get("type")?.takeIf { it.isJsonPrimitive }?.asString
            if (type !in FILTERABLE_LAYER_TYPES) return@mapNotNull null

            val isLevelLayer = layerJson.isFloorMetadata(FLOOR_METADATA_VALUE_LEVEL) ||
                id.endsWith(ROKAS_FLOOR_ID_SUFFIX)
            val matches = isLevelLayer || FLOOR_LAYER_ID_PREFIXES.any { id.startsWith(it) }
            if (matches) id else null
        }

    private fun parseStyleLayers(style: Style): List<JsonObject> {
        val json = runCatching { style.json }.getOrNull() ?: return emptyList()
        val root = runCatching { JsonParser.parseString(json) }.getOrNull() as? JsonObject ?: return emptyList()
        val layers = root.get("layers") as? JsonArray ?: return emptyList()
        return layers.mapNotNull { it as? JsonObject }
    }

    private fun JsonObject.floorMetadata(): String? {
        val metadata = get("metadata") as? JsonObject ?: return null
        val value = metadata.get(FLOOR_METADATA_KEY) ?: return null
        return value.takeIf { it.isJsonPrimitive }?.asString?.takeIf { it.isNotEmpty() }
    }

    private fun JsonObject.isFloorMetadata(expected: String): Boolean = floorMetadata() == expected


    private fun updateTwoDAndLevelLayers(
        style: Style,
        styleLayers: List<JsonObject>,
        indoorEnabled: Boolean,
    ) {
        styleLayers.forEach { layerJson ->
            val id = layerJson.get("id")?.takeIf { it.isJsonPrimitive }?.asString ?: return@forEach
            val floorMetadata = layerJson.floorMetadata()
            val is2D = floorMetadata?.let { it == FLOOR_METADATA_VALUE_2D } ?: id.endsWith(ROKAS_2D_ID_SUFFIX)
            val isLevel = floorMetadata?.let { it == FLOOR_METADATA_VALUE_LEVEL } ?: id.endsWith(ROKAS_FLOOR_ID_SUFFIX)

            val visibility =
                when {
                    is2D -> if (indoorEnabled) Property.NONE else Property.VISIBLE
                    isLevel -> if (indoorEnabled) Property.VISIBLE else Property.NONE
                    else -> return@forEach
                }
            style.getLayer(id)?.setProperties(PropertyFactory.visibility(visibility))
        }
    }


    private fun updateBackgroundOverlays(
        style: Style,
        indoorEnabled: Boolean,
        selectedFloor: Int,
    ) {
        setBackgroundShown(style, ROKAS_BACKGROUND_MASK_LAYER, selectedFloor < 0)
        setBackgroundShown(style, LEVEL_GREYOUT_LAYER, indoorEnabled && selectedFloor != 0)
    }


    private fun setBackgroundShown(
        style: Style,
        layerId: String,
        shown: Boolean,
    ) {
        val layer = style.getLayerAs<BackgroundLayer>(layerId) ?: return
        val original = originalOpacities.getOrPut(layerId) { layer.backgroundOpacity }
        val opacity =
            when {
                !shown -> PropertyFactory.backgroundOpacity(0f)
                original.expression != null -> PropertyFactory.backgroundOpacity(original.expression)
                else -> PropertyFactory.backgroundOpacity(original.value ?: 1f)
            }
        layer.setProperties(PropertyFactory.visibility(Property.VISIBLE), opacity)
    }

    private fun parseFilter(filter: Expression): JsonArray? =
        runCatching { JsonParser.parseString(filter.toString()) }.getOrNull() as? JsonArray

    private fun replaceFloor(
        node: JsonElement,
        floor: Int,
    ): JsonElement {
        if (node !is JsonArray || node.size() == 0) return node
        val operator = node[0].takeIf { it.isJsonPrimitive }?.asString
        return when {
            operator == "all" || operator == "any" || operator == "none" ->
                JsonArray().apply {
                    add(node[0])
                    node.drop(1).forEach { add(replaceFloor(it, floor)) }
                }

            isComparisonOperator(operator) && node.size() == 3 && isLevelLookup(node[1]) ->
                JsonArray().apply {
                    add(node[0])
                    add(node[1])
                    add(floor)
                }

            else -> node
        }
    }

    private fun isComparisonOperator(operator: String?): Boolean =
        operator == "==" || operator == "!=" ||
            operator == "<" || operator == "<=" ||
            operator == ">" || operator == ">="


    private fun isLevelLookup(operand: JsonElement): Boolean {
        if (operand.isJsonPrimitive && operand.asJsonPrimitive.isString) {

            val key = operand.asString
            return key == "floor" || key == "level"
        }
        val json = operand.toString()
        return json == """["get","level"]""" ||
            json == """["get","floor"]""" ||
            json.startsWith("""["case",["has","level"],["get","level"]""") ||
            json.startsWith("""["case",["has","floor"],["get","floor"]""")
    }

    private var Layer.filterOrNull: Expression?
        get() =
            when (this) {
                is SymbolLayer -> filter
                is CircleLayer -> filter
                is HeatmapLayer -> filter
                is FillLayer -> filter
                is FillExtrusionLayer -> filter
                is LineLayer -> filter
                else -> null
            }
        set(value) {
            value ?: return
            when (this) {
                is SymbolLayer -> setFilter(value)
                is CircleLayer -> setFilter(value)
                is HeatmapLayer -> setFilter(value)
                is FillLayer -> setFilter(value)
                is FillExtrusionLayer -> setFilter(value)
                is LineLayer -> setFilter(value)
            }
        }
}
