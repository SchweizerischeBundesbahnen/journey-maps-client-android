// SPDX-License-Identifier: MIT
// Copyright (c) 2024 Schweizerische Bundesbahnen AG (Swiss Federal Railways)

package ch.sbb.maps.poi

import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.Style
import org.maplibre.android.style.expressions.Expression
import org.maplibre.android.style.layers.Property
import org.maplibre.android.style.layers.PropertyFactory
import org.maplibre.android.style.layers.SymbolLayer
import org.maplibre.android.style.sources.GeoJsonSource
import org.maplibre.geojson.Feature
import org.maplibre.geojson.Point
import kotlin.math.pow
import kotlin.math.sqrt

private val POI_LAYER_IDS =
    listOf(
        "journey-pois",
        "journey-pois-first",
        "journey-pois-second-lvl",
        "journey-pois-third-lvl",
        "journey-pois-second-2d",
    )

private val POI_CATEGORY_FILTER_LAYER_IDS =
    listOf(
        "journey-pois-second-lvl",
        "journey-pois-first",
    )
private const val POI_SBB_ID = "sbbId"
private const val POI_ICON_PROPERTY = "icon"
private const val POI_SELECTED_MARKER = "marker_poi_bright_selected"
private const val POI_SELECTED_LAYER_ID = "journey-pois-selected"
private const val GEO_JSON_SOURCE_ID = "service_points"

internal class SBBMapPoiController(
    private val map: MapLibreMap,
) {
    private val gson = com.google.gson.Gson()

    private var poiFilterSubCategoriesDefault = DEFAULT_POI_FILTER_SUB_CATEGORIES

    private var geoJsonSource = map.style?.getSourceAs<GeoJsonSource>(GEO_JSON_SOURCE_ID)

    internal fun setPoiResourcesVisible(
        enablePoi: Boolean,
        poiFilterSubCategories: List<String>?,
    ) {
        val categories = poiFilterSubCategories ?: poiFilterSubCategoriesDefault

        map.getStyle { style ->
            POI_CATEGORY_FILTER_LAYER_IDS.forEach { layerId ->
                style.getLayerAs<SymbolLayer>(layerId)?.let { layer ->
                    val oldFilter = layer.filter?.toArray()?.toList() ?: emptyList()
                    val newFilter = setPoiCategories(oldFilter, categories)
                    layer.setFilter(Expression.raw(gson.toJson(newFilter)))
                }
            }

            POI_LAYER_IDS.forEach { layerId ->
                style.getLayerAs<SymbolLayer>(layerId)?.setProperties(
                    PropertyFactory.visibility(if (enablePoi) Property.VISIBLE else Property.NONE),
                )
            }
        }
    }

    private fun setPoiCategories(
        filterExpression: List<Any?>,
        categories: List<String>,
    ): List<Any?> =
        if (hasGetSubCategory(gson.toJson(filterExpression))) {
            replaceExistingPoiCategories(filterExpression, categories)
        } else {
            insertNewPoiCategories(filterExpression, categories)
        }

    private fun insertNewPoiCategories(
        filterExpression: List<Any?>,
        categories: List<String>,
    ): List<Any?> {
        val categoryClause = listOf("in", listOf("get", "subCategory"), listOf("literal", categories))
        if (filterExpression.isEmpty()) return categoryClause

        val operator = (filterExpression.firstOrNull() as? String)?.lowercase()
        return if (operator == "all") {
            listOf(filterExpression.first()) + listOf(categoryClause) + filterExpression.drop(1)
        } else {
            listOf("all", categoryClause, filterExpression)
        }
    }

    private fun replaceExistingPoiCategories(
        filterExpression: List<Any?>,
        categories: List<String>,
    ): List<Any?> {
        val operator = (filterExpression.firstOrNull() as? String)?.lowercase() ?: return filterExpression

        return when (operator) {
            "all", "any" ->
                listOf(filterExpression.first()) +
                    filterExpression.drop(1).map { child ->
                        asList(child)?.let { replaceExistingPoiCategories(it, categories) } ?: child
                    }

            "in" -> {
                if (filterExpression.size == 3 && isGetSubCategory(gson.toJson(filterExpression[1]))) {
                    listOf(filterExpression[0], filterExpression[1], listOf("literal", categories))
                } else {
                    filterExpression
                }
            }

            else -> filterExpression
        }
    }

    private fun hasGetSubCategory(expression: String): Boolean =
        expression.contains("[\"in\",[\"get\",\"subCategory\"]")

    private fun isGetSubCategory(expression: String): Boolean =
        expression == "[\"get\",\"subCategory\"]"

    private fun asList(value: Any?): List<Any?>? =
        when (value) {
            is List<*> -> value
            is Array<*> -> value.toList()
            else -> null
        }

    internal fun setupPoiClickEvent(point: LatLng?): Feature? {
        if (point == null) return null

        val screenPoint = map.projection.toScreenLocation(point)

        screenPoint.let { screenLocation ->
            val layerIds = POI_LAYER_IDS.toTypedArray()
            val features =
                map.queryRenderedFeatures(
                    screenLocation,
                    Expression.has(POI_SBB_ID),
                    *layerIds,
                )
            if (features.isNotEmpty()) {
                val poi =
                    features.minByOrNull {
                        distance(
                            LatLng(
                                (it.geometry() as Point).latitude(),
                                (it.geometry() as Point).longitude(),
                            ),
                            point,
                        )
                    }!!

                poi.addStringProperty(POI_ICON_PROPERTY, POI_SELECTED_MARKER)
                geoJsonSource?.setGeoJson(poi)
                updatePoiSelectedLayer(map.style, poi.getStringProperty(POI_SBB_ID))
                return poi
            } else {
                return null
            }
        }
    }

    internal fun deselectPoi() {
        updatePoiSelectedLayer(map.style, null)
    }

    private fun updatePoiSelectedLayer(
        style: Style?,
        sbbId: String?,
    ) {
        val layerIds = style?.layers?.map { it.id }
        layerIds?.forEach { layerId ->
            val layer = style.getLayer(layerId)
            if (layer?.id?.startsWith(POI_SELECTED_LAYER_ID) == true) {
                if (sbbId != null) {
                    layer.setProperties(
                        PropertyFactory.visibility(
                            Property.VISIBLE,
                        ),
                    )
                    layer.setProperties(
                        PropertyFactory.iconOpacity(
                            Expression.raw("[\"case\", [\"boolean\", [\"==\", [\"get\", \"sbbId\"], \"$sbbId\"],], 1, 0]"),
                        ),
                    )
                } else {
                    layer.setProperties(
                        PropertyFactory.visibility(Property.NONE),
                    )
                }
            }
        }
    }

    internal fun updateSubcategories(newSelectedCategories: List<String>) {
        setPoiResourcesVisible(true, newSelectedCategories)
    }

    private fun distance(
        from: LatLng,
        to: LatLng,
    ): Double =
        sqrt(
            (from.latitude - to.latitude).pow(2) +
                (from.longitude - to.longitude).pow(
                    2,
                ),
        )
}
