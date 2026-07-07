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
private const val POI_SBB_ID = "sbbId"
private const val POI_ICON_PROPERTY = "icon"
private const val POI_SELECTED_MARKER = "marker_poi_bright_selected"
private const val POI_SELECTED_LAYER_ID = "journey-pois-selected"
private const val GEO_JSON_SOURCE_ID = "service_points"

internal class SBBMapPoiController(
    private val map: MapLibreMap,
) {
    private var poiFilterSubCategoriesDefault = DEFAULT_POI_FILTER_SUB_CATEGORIES

    private var geoJsonSource = map.style?.getSourceAs<GeoJsonSource>(GEO_JSON_SOURCE_ID)

    internal fun setPoiResourcesVisible(
        enablePoi: Boolean,
        poiFilterSubCategories: List<String>?,
    ) {
        val categories = poiFilterSubCategories ?: poiFilterSubCategoriesDefault

        map.getStyle { style ->
            POI_LAYER_IDS.forEach { layerId ->
                val layer = style.getLayerAs<SymbolLayer>(layerId)
                layer?.let {
                    val expressionList =
                        categories.map { subCategory ->
                            Expression.eq(
                                Expression.get("subCategory"),
                                Expression.literal(subCategory),
                            )
                        }
                    val filter = Expression.any(*expressionList.toTypedArray())

                    it.setFilter(filter)
                    it.setProperties(PropertyFactory.visibility(if (enablePoi) Property.VISIBLE else Property.NONE))
                }
            }
        }
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
