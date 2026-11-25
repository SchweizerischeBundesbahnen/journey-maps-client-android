// SPDX-License-Identifier: MIT
// Copyright (c) 2024 Schweizerische Bundesbahnen AG (Swiss Federal Railways)

package ch.sbb.maps.poi

import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.Style
import org.maplibre.android.style.expressions.Expression
import org.maplibre.android.style.layers.Property
import org.maplibre.android.style.layers.PropertyFactory
import org.maplibre.android.style.sources.GeoJsonSource
import org.maplibre.geojson.Feature
import org.maplibre.geojson.Point
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * Handles POI selection: finds the POI nearest to a tap, marks it as selected, and
 * highlights it on the map. Also clears the current selection.
 */
internal class SBBMapPoiSelection(
    private val map: MapLibreMap,
) {
    private val source = map.style?.getSourceAs<GeoJsonSource>(SBBMapPoiLayerIds.SERVICE_POINTS_SOURCE)

    fun select(point: LatLng?): Feature? {
        point ?: return null
        val screenPoint = map.projection.toScreenLocation(point)
        val features =
            map.queryRenderedFeatures(
                screenPoint,
                Expression.has(SBBMapPoiLayerIds.SBB_ID),
                *SBBMapPoiLayerIds.ALL.toTypedArray(),
            )
        val poi = features.minByOrNull { distanceTo(it, point) } ?: return null

        poi.addStringProperty(SBBMapPoiLayerIds.ICON_PROPERTY, SBBMapPoiLayerIds.SELECTED_MARKER)
        source?.setGeoJson(poi)
        highlight(map.style, poi.getStringProperty(SBBMapPoiLayerIds.SBB_ID))
        return poi
    }

    fun deselect() = highlight(map.style, null)

    private fun highlight(
        style: Style?,
        sbbId: String?,
    ) {
        style
            ?.layers
            ?.filter { it.id.startsWith(SBBMapPoiLayerIds.SELECTED_LAYER_PREFIX) }
            ?.forEach { layer ->
                if (sbbId == null) {
                    layer.setProperties(PropertyFactory.visibility(Property.NONE))
                } else {
                    layer.setProperties(PropertyFactory.visibility(Property.VISIBLE))
                    layer.setProperties(
                        PropertyFactory.iconOpacity(
                            Expression.raw("[\"case\", [\"boolean\", [\"==\", [\"get\", \"sbbId\"], \"$sbbId\"],], 1, 0]"),
                        ),
                    )
                }
            }
    }

    private fun distanceTo(
        feature: Feature,
        point: LatLng,
    ): Double {
        val geometry = feature.geometry() as Point
        return sqrt(
            (geometry.latitude() - point.latitude).pow(2) +
                (geometry.longitude() - point.longitude).pow(2),
        )
    }
}
