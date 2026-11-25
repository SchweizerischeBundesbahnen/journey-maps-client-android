// SPDX-License-Identifier: MIT
// Copyright (c) 2024 Schweizerische Bundesbahnen AG (Swiss Federal Railways)

package ch.sbb.maps.floor

import ch.sbb.maps.floor.SBBMapFloorStyleIds.LEVEL_GREYOUT_LAYER
import ch.sbb.maps.floor.SBBMapFloorStyleIds.ROKAS_BACKGROUND_MASK_LAYER
import com.google.gson.JsonObject
import org.maplibre.android.maps.Style
import org.maplibre.android.style.layers.BackgroundLayer
import org.maplibre.android.style.layers.Property
import org.maplibre.android.style.layers.PropertyFactory
import org.maplibre.android.style.layers.PropertyValue

/**
 * Controls layer visibility for the selected floor: shows the "level" layers and
 * hides the "2D" layers when indoors, and vice versa. Also adjusts the background
 * mask opacity, retaining each mask's original value so it can be restored.
 */
internal class SBBMapFloorVisibility {
    private val originalOpacities = mutableMapOf<String, PropertyValue<Float>>()

    fun reset() = originalOpacities.clear()

    fun applyLevelAnd2d(
        style: Style,
        layers: List<JsonObject>,
        indoorEnabled: Boolean,
    ) {
        layers.forEach { layer ->
            val id = SBBMapFloorStyleLayers.id(layer) ?: return@forEach
            val visibility =
                when {
                    SBBMapFloorStyleLayers.is2d(layer) -> if (indoorEnabled) Property.NONE else Property.VISIBLE
                    SBBMapFloorStyleLayers.isLevel(layer) -> if (indoorEnabled) Property.VISIBLE else Property.NONE
                    else -> return@forEach
                }
            style.getLayer(id)?.setProperties(PropertyFactory.visibility(visibility))
        }
    }

    fun applyBackgroundMasks(
        style: Style,
        indoorEnabled: Boolean,
        selectedFloor: Int,
    ) {
        setShown(style, ROKAS_BACKGROUND_MASK_LAYER, selectedFloor < 0)
        setShown(style, LEVEL_GREYOUT_LAYER, indoorEnabled && selectedFloor != 0)
    }

    private fun setShown(
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
}
