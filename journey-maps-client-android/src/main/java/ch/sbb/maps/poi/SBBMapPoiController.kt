// SPDX-License-Identifier: MIT
// Copyright (c) 2024 Schweizerische Bundesbahnen AG (Swiss Federal Railways)

package ch.sbb.maps.poi

import com.google.gson.Gson
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.style.expressions.Expression
import org.maplibre.android.style.layers.Property
import org.maplibre.android.style.layers.PropertyFactory
import org.maplibre.android.style.layers.SymbolLayer
import org.maplibre.geojson.Feature

/**
 * Entry point for POI handling: toggles POI visibility, applies category filters, and
 * delegates taps to the selection handler ([SBBMapPoiSelection]). Filter logic lives in
 * [SBBMapPoiCategoryFilter].
 */
internal class SBBMapPoiController(
    private val map: MapLibreMap,
) {
    private val gson = Gson()
    private val selection = SBBMapPoiSelection(map)

    internal fun setPoiResourcesVisible(
        enablePoi: Boolean,
        poiFilterSubCategories: List<String>?,
    ) {
        val categories = poiFilterSubCategories ?: DEFAULT_POI_FILTER_SUB_CATEGORIES

        map.getStyle { style ->
            SBBMapPoiLayerIds.CATEGORY_FILTERABLE.forEach { layerId ->
                style.getLayerAs<SymbolLayer>(layerId)?.let { layer ->
                    val oldFilter = layer.filter?.toArray()?.toList() ?: emptyList()
                    val newFilter = SBBMapPoiCategoryFilter.apply(oldFilter, categories)
                    layer.setFilter(Expression.raw(gson.toJson(newFilter)))
                }
            }

            SBBMapPoiLayerIds.ALL.forEach { layerId ->
                style.getLayerAs<SymbolLayer>(layerId)?.setProperties(
                    PropertyFactory.visibility(if (enablePoi) Property.VISIBLE else Property.NONE),
                )
            }
        }
    }

    internal fun updateSubcategories(newSelectedCategories: List<String>) =
        setPoiResourcesVisible(true, newSelectedCategories)

    internal fun setupPoiClickEvent(point: LatLng?): Feature? = selection.select(point)

    internal fun deselectPoi() = selection.deselect()
}
