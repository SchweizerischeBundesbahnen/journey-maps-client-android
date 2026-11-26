// SPDX-License-Identifier: MIT
// Copyright (c) 2024 Schweizerische Bundesbahnen AG (Swiss Federal Railways)

package ch.sbb.maps.floor

import ch.sbb.maps.floor.SBBMapFloorStyleIds.FILTERABLE_LAYER_TYPES
import ch.sbb.maps.floor.SBBMapFloorStyleIds.FLOOR_LAYER_ID_PREFIXES
import ch.sbb.maps.floor.SBBMapFloorStyleIds.FLOOR_METADATA_KEY
import ch.sbb.maps.floor.SBBMapFloorStyleIds.FLOOR_METADATA_VALUE_2D
import ch.sbb.maps.floor.SBBMapFloorStyleIds.FLOOR_METADATA_VALUE_LEVEL
import ch.sbb.maps.floor.SBBMapFloorStyleIds.ROKAS_2D_ID_SUFFIX
import ch.sbb.maps.floor.SBBMapFloorStyleIds.ROKAS_FLOOR_ID_SUFFIX
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import org.maplibre.android.maps.Style

/**
 * Inspects the map style and identifies the layers that belong to a building floor,
 * reporting whether each is a full-floor ("level") or a flat ("2D") layer.
 */
internal object SBBMapFloorStyleLayers {
    fun parse(style: Style): List<JsonObject> {
        val json = runCatching { style.json }.getOrNull() ?: return emptyList()
        val root = runCatching { JsonParser.parseString(json) }.getOrNull() as? JsonObject ?: return emptyList()
        val layers = root.get("layers") as? JsonArray ?: return emptyList()
        return layers.mapNotNull { it as? JsonObject }
    }

    fun id(layer: JsonObject): String? = layer.get("id")?.takeIf { it.isJsonPrimitive }?.asString

    fun isLevel(layer: JsonObject): Boolean =
        role(layer)?.let { it == FLOOR_METADATA_VALUE_LEVEL } ?: (id(layer)?.endsWith(ROKAS_FLOOR_ID_SUFFIX) == true)

    fun is2d(layer: JsonObject): Boolean =
        role(layer)?.let { it == FLOOR_METADATA_VALUE_2D } ?: (id(layer)?.endsWith(ROKAS_2D_ID_SUFFIX) == true)

    fun filterableFloorIds(layers: List<JsonObject>): List<String> =
        layers.mapNotNull { layer ->
            val id = id(layer) ?: return@mapNotNull null
            val type = layer.get("type")?.takeIf { it.isJsonPrimitive }?.asString
            if (type !in FILTERABLE_LAYER_TYPES) return@mapNotNull null

            val isLevelLayer = role(layer) == FLOOR_METADATA_VALUE_LEVEL || id.endsWith(ROKAS_FLOOR_ID_SUFFIX)
            val matches = isLevelLayer || FLOOR_LAYER_ID_PREFIXES.any { id.startsWith(it) }
            if (matches) id else null
        }

    private fun role(layer: JsonObject): String? {
        val metadata = layer.get("metadata") as? JsonObject ?: return null
        return metadata.get(FLOOR_METADATA_KEY)?.takeIf { it.isJsonPrimitive }?.asString?.takeIf { it.isNotEmpty() }
    }
}
