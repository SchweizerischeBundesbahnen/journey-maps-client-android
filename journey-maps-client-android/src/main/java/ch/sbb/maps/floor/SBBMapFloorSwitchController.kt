// SPDX-License-Identifier: MIT
// Copyright (c) 2024 Schweizerische Bundesbahnen AG (Swiss Federal Railways)

package ch.sbb.maps.floor

import androidx.lifecycle.MutableLiveData
import com.google.gson.GsonBuilder
import com.google.gson.ToNumberPolicy
import org.maplibre.android.maps.MapLibreMap
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

    private val gson =
        GsonBuilder()
            .setObjectToNumberStrategy(ToNumberPolicy.LONG_OR_DOUBLE)
            .create()

    private var maskVisibleOpacity: PropertyValue<*>? = null
    private var maskOpacityCaptured = false

    internal fun onStyleReloaded() {
        maskOpacityCaptured = false
        maskVisibleOpacity = null
    }

    internal fun getAvailableFloorFilters(selectedFloor: Int) {
        floors.value = getAvailableFloors().value
        map.style
            ?.layers
            ?.filter { layer -> layer.id.endsWith(ROKAS_FLOOR_ID_SUFFIX) }
            ?.map { layer -> layer.id }
            ?.also { knownLvlLayerIds ->
                updateMapLayers(knownLvlLayerIds, selectedFloor)
            }

        updateBackgroundMask(selectedFloor)
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
                layer?.let { currentLayer ->
                    val newFilter = getNewFilter(currentLayer, selectedFloor) ?: return@let
                    when (currentLayer) {
                        is SymbolLayer -> currentLayer.withFilter(newFilter)
                        is CircleLayer -> currentLayer.withFilter(newFilter)
                        is HeatmapLayer -> currentLayer.withFilter(newFilter)
                        is FillLayer -> currentLayer.withFilter(newFilter)
                        is FillExtrusionLayer -> currentLayer.withFilter(newFilter)
                        is LineLayer -> currentLayer.withFilter(newFilter)
                    }
                }
            }
        }
    }

    private fun updateBackgroundMask(selectedFloor: Int) {
        map.getStyle { style ->
            val mask = style.getLayer(ROKAS_BACKGROUND_MASK_LAYER) as? BackgroundLayer ?: return@getStyle

            if (!maskOpacityCaptured) {
                maskVisibleOpacity = mask.backgroundOpacity
                maskOpacityCaptured = true
                val prop = mask.backgroundOpacity
                android.util.Log.d(
                    "FloorMask",
                    "original backgroundOpacity value=${prop.value} " +
                        "isExpression=${prop.isExpression} expression=${prop.expression} " +
                        "color=${mask.backgroundColor.value}",
                )
            }

            val shownOpacity = resolveShownOpacity()
            mask.setProperties(
                PropertyFactory.visibility(Property.VISIBLE),
                if (selectedFloor < 0) shownOpacity else PropertyFactory.backgroundOpacity(0f),
            )
        }
    }

    private fun resolveShownOpacity(): PropertyValue<*> {
        val captured = maskVisibleOpacity ?: return PropertyFactory.backgroundOpacity(1f)
        val expression = if (captured.isExpression) captured.expression else null
        val constant = captured.value as? Float
        return when {
            expression != null -> PropertyFactory.backgroundOpacity(expression)
            constant != null -> PropertyFactory.backgroundOpacity(constant)
            else -> PropertyFactory.backgroundOpacity(1f)
        }
    }

    private fun getNewFilter(
        currentLayer: Layer,
        selectedFloor: Int,
    ): Expression? {
        val oldFilter =
            when (currentLayer) {
                is SymbolLayer -> currentLayer.filter
                is CircleLayer -> currentLayer.filter
                is HeatmapLayer -> currentLayer.filter
                is FillLayer -> currentLayer.filter
                is FillExtrusionLayer -> currentLayer.filter
                is LineLayer -> currentLayer.filter
                else -> null
            } ?: return null

        val tree = expressionToList(oldFilter) ?: return oldFilter
        val rewritten = replaceFloor(tree, selectedFloor)
        return Expression.raw(gson.toJson(rewritten))
    }

    private fun expressionToList(expression: Expression): List<*>? =
        runCatching { gson.fromJson(expression.toString(), List::class.java) }.getOrNull()

    private fun replaceFloor(
        expression: Any?,
        floor: Int,
    ): Any? {
        if (expression !is List<*> || expression.isEmpty()) return expression

        val operator = expression.first()
        if (isBooleanGroup(operator)) {
            return listOf(operator) + expression.drop(1).map { replaceFloor(it, floor) }
        }
        if (operator == "==" && expression.size == 3 && isLevelFilterIdiom(expression[1])) {
            return listOf(operator, expression[1], floor)
        }
        return expression
    }

    private fun isBooleanGroup(operator: Any?): Boolean = operator in BOOLEAN_GROUP_OPERATORS

    private fun isLevelFilterIdiom(operand: Any?): Boolean {
        val operandString = gson.toJson(operand)
        return operandString == LEVEL_GETTER ||
            isCaseFilter(operandString, LEVEL) ||
            isCaseFilter(operandString, FLOOR)
    }

    private fun isCaseFilter(
        operandString: String,
        property: String,
    ): Boolean = operandString.startsWith("[\"case\",[\"has\",\"$property\"],[\"get\",\"$property\"]")

    companion object SBBMapFloorSwitchControllerConsts {
        const val ROKAS_FLOOR_ID_SUFFIX = "-lvl"
        const val ROKAS_BACKGROUND_MASK_LAYER = "rokas_background_mask"
        const val SERVICE_POINTS_SOURCE = "service_points"
        const val FLOOR_LIST_PROPERTY = "floor_liststring"
        const val FLOOR = "floor"
        const val LEVEL = "level"
        const val LEVEL_GETTER = "[\"get\",\"level\"]"
        val BOOLEAN_GROUP_OPERATORS = setOf("all", "any", "none")
    }
}
