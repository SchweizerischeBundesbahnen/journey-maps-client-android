// SPDX-License-Identifier: MIT
// Copyright (c) 2024 Schweizerische Bundesbahnen AG (Swiss Federal Railways)

package ch.sbb.maps.floor

import androidx.lifecycle.MutableLiveData
import ch.sbb.maps.floor.SBBMapFloorStyleIds.FLOOR_LIST_PROPERTY
import ch.sbb.maps.floor.SBBMapFloorStyleIds.ROKAS_BACKGROUND_MASK_LAYER
import ch.sbb.maps.floor.SBBMapFloorStyleIds.ROKAS_FLOOR_ID_SUFFIX
import ch.sbb.maps.floor.SBBMapFloorStyleIds.SERVICE_POINTS_SOURCE
import com.google.gson.JsonArray
import com.google.gson.JsonElement
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

    private var maskStyle: Style? = null
    private var maskOpacity: PropertyValue<Float>? = null

    internal fun getAvailableFloorFilters(selectedFloor: Int) {
        floors.value = getAvailableFloors().value
        map.getStyle { style ->
            updateBackgroundMask(style, selectedFloor)
            style.layers
                .filter { it.id.endsWith(ROKAS_FLOOR_ID_SUFFIX) }
                .forEach { layer ->
                    val filter = layer.filterOrNull ?: return@forEach
                    layer.filterOrNull = rewriteFloor(filter, selectedFloor)
                }
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

    /**
     * Keeps the mask VISIBLE and toggles its opacity between 0
     * and the value the style defines.
     **/
    private fun updateBackgroundMask(
        style: Style,
        selectedFloor: Int,
    ) {
        val mask = style.getLayerAs<BackgroundLayer>(ROKAS_BACKGROUND_MASK_LAYER) ?: return
        if (maskStyle !== style) {
            maskStyle = style
            maskOpacity = mask.backgroundOpacity
        }
        mask.setProperties(
            PropertyFactory.visibility(Property.VISIBLE),
            if (selectedFloor < 0) styleOpacity() else PropertyFactory.backgroundOpacity(0f),
        )
    }

    private fun styleOpacity(): PropertyValue<*> {
        val expression = maskOpacity?.expression
        return when {
            expression != null -> PropertyFactory.backgroundOpacity(expression)
            else -> PropertyFactory.backgroundOpacity(maskOpacity?.value ?: 1f)
        }
    }

    private fun rewriteFloor(
        filter: Expression,
        floor: Int,
    ): Expression {
        val tree = runCatching { JsonParser.parseString(filter.toString()) }.getOrNull() ?: return filter
        return Expression.raw(replaceFloor(tree, floor).toString())
    }

    private fun replaceFloor(
        node: JsonElement,
        floor: Int,
    ): JsonElement {
        if (node !is JsonArray || node.size() == 0) return node
        val operator = node[0].takeIf { it.isJsonPrimitive }?.asString
        return when (operator) {
            "all", "any", "none" ->
                JsonArray().apply {
                    add(node[0])
                    node.drop(1).forEach { add(replaceFloor(it, floor)) }
                }
            "==" if node.size() == 3 && isLevelLookup(node[1]) ->
                JsonArray().apply {
                    add(node[0])
                    add(node[1])
                    add(floor)
                }
            else -> node
        }
    }

    /** Whether the expression looks up the floor of a feature. */
    private fun isLevelLookup(operand: JsonElement): Boolean {
        val json = operand.toString()
        return json == """["get","level"]""" ||
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