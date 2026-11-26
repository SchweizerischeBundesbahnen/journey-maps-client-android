// SPDX-License-Identifier: MIT
// Copyright (c) 2024 Schweizerische Bundesbahnen AG (Swiss Federal Railways)

package ch.sbb.maps.floor

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonParser
import org.maplibre.android.style.expressions.Expression
import org.maplibre.android.style.layers.CircleLayer
import org.maplibre.android.style.layers.FillExtrusionLayer
import org.maplibre.android.style.layers.FillLayer
import org.maplibre.android.style.layers.HeatmapLayer
import org.maplibre.android.style.layers.Layer
import org.maplibre.android.style.layers.LineLayer
import org.maplibre.android.style.layers.SymbolLayer

/**
 * Adjusts a layer's filter so it only shows the selected floor.
 *
 * A floor filter is a comparison such as `level == 0`; this replaces the floor
 * value with the selected one. MapLibre filters are [Expression]s, so they are
 * parsed to JSON, updated, and converted back.
 */
internal object SBBMapFloorFilter {
    fun parse(filter: Expression): JsonArray? =
        runCatching { JsonParser.parseString(filter.toString()) }.getOrNull() as? JsonArray

    fun withFloor(
        node: JsonElement,
        floor: Int,
    ): JsonElement {
        if (node !is JsonArray || node.size() == 0) return node
        val operator = node[0].takeIf { it.isJsonPrimitive }?.asString
        return when {
            operator == "all" || operator == "any" || operator == "none" ->
                JsonArray().apply {
                    add(node[0])
                    node.drop(1).forEach { add(withFloor(it, floor)) }
                }

            isComparison(operator) && node.size() == 3 && isLevelLookup(node[1]) ->
                JsonArray().apply {
                    add(node[0])
                    add(node[1])
                    add(floor)
                }

            else -> node
        }
    }

    private fun isComparison(operator: String?): Boolean =
        operator == "==" || operator == "!=" ||
            operator == "<" || operator == "<=" ||
            operator == ">" || operator == ">="

    private fun isLevelLookup(operand: JsonElement): Boolean {
        if (operand.isJsonPrimitive && operand.asJsonPrimitive.isString) {
            return operand.asString == "floor" || operand.asString == "level"
        }
        val json = operand.toString()
        return json == """["get","level"]""" ||
            json == """["get","floor"]""" ||
            json.startsWith("""["case",["has","level"],["get","level"]""") ||
            json.startsWith("""["case",["has","floor"],["get","floor"]""")
    }
}

/** Reads/writes the filter of a layer without caring about its concrete type. */
internal var Layer.floorFilter: Expression?
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
