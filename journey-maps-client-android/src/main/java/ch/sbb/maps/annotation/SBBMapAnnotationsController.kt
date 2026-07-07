// SPDX-License-Identifier: MIT
// Copyright (c) 2024 Schweizerische Bundesbahnen AG (Swiss Federal Railways)

package ch.sbb.maps.annotation

import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.style.expressions.Expression
import org.maplibre.android.style.layers.CircleLayer
import org.maplibre.android.style.layers.LineLayer
import org.maplibre.android.style.layers.Property
import org.maplibre.android.style.layers.PropertyFactory
import org.maplibre.android.style.layers.SymbolLayer
import org.maplibre.android.style.sources.GeoJsonSource
import org.maplibre.geojson.Feature
import org.maplibre.geojson.FeatureCollection
import org.maplibre.geojson.LineString
import org.maplibre.geojson.Point

internal class SBBMapAnnotationsController(
    private val map: MapLibreMap,
) {
    private var currentLines: List<SBBLine> = emptyList()
    private var currentCircles: List<SBBCircle> = emptyList()
    private var currentFloorConnectors: List<SBBFloorConnector> = emptyList()

    private val addedSourceIds = mutableSetOf<String>()
    private val addedLayerIds = mutableSetOf<String>()
    private val addedImageIds = mutableSetOf<String>()

    internal fun updateLines(lines: List<SBBLine>, selectedFloor: Int) {
        removeAllLayers()
        currentLines = lines
        currentCircles = emptyList()
        currentFloorConnectors = emptyList()
        addLineLayers(selectedFloor)
    }

    internal fun updateCircles(circles: List<SBBCircle>, selectedFloor: Int) {
        removeCircleLayers()
        currentCircles = circles
        addCircleLayers(selectedFloor)
    }

    internal fun updateFloorConnectors(connectors: List<SBBFloorConnector>, selectedFloor: Int) {
        removeFloorConnectorLayers()
        currentFloorConnectors = connectors
        addFloorConnectorLayers(selectedFloor)
    }

    internal fun updateFloorVisibility(selectedFloor: Int) {
        map.getStyle { style ->
            currentLines.forEach { line ->
                val layerId = lineLayerId(line.id)
                val layer = style.getLayerAs<LineLayer>(layerId)
                layer?.setProperties(
                    PropertyFactory.visibility(
                        if (isVisibleOnFloor(line.floor, selectedFloor)) Property.VISIBLE else Property.NONE,
                    ),
                )
                // Also update outline layer
                val outlineLayerId = lineOutlineLayerId(line.id)
                val outlineLayer = style.getLayerAs<LineLayer>(outlineLayerId)
                outlineLayer?.setProperties(
                    PropertyFactory.visibility(
                        if (isVisibleOnFloor(line.floor, selectedFloor)) Property.VISIBLE else Property.NONE,
                    ),
                )
            }
            currentCircles.forEach { circle ->
                val layerId = circleLayerId(circle.id)
                val layer = style.getLayerAs<CircleLayer>(layerId)
                val visible = isVisibleOnFloor(circle.floor, selectedFloor)
                layer?.setProperties(
                    PropertyFactory.circleColor(if (visible) circle.color else circle.disabledColor),
                )
            }
            currentFloorConnectors.forEach { connector ->
                val layerId = connectorLayerId(connector.id)
                val layer = style.getLayerAs<SymbolLayer>(layerId)
                layer?.setProperties(
                    PropertyFactory.visibility(
                        if (connector.originFloor == selectedFloor) Property.VISIBLE else Property.NONE,
                    ),
                )
            }
        }
    }

    internal fun removeAllLayers() {
        map.getStyle { style ->
            addedLayerIds.forEach { layerId ->
                style.removeLayer(layerId)
            }
            addedSourceIds.forEach { sourceId ->
                style.removeSource(sourceId)
            }
            addedImageIds.forEach { imageId ->
                style.removeImage(imageId)
            }
        }
        addedLayerIds.clear()
        addedSourceIds.clear()
        addedImageIds.clear()
        currentLines = emptyList()
        currentCircles = emptyList()
        currentFloorConnectors = emptyList()
    }

    private fun addLineLayers(selectedFloor: Int) {
        map.getStyle { style ->
            currentLines.forEach { line ->
                val sourceId = lineSourceId(line.id)
                val layerId = lineLayerId(line.id)

                val points = line.coordinates.map { Point.fromLngLat(it.longitude, it.latitude) }
                if (points.size < 2) return@forEach

                val lineString = LineString.fromLngLats(points)
                val feature = Feature.fromGeometry(lineString)
                val source = GeoJsonSource(sourceId, FeatureCollection.fromFeature(feature))
                style.addSource(source)
                addedSourceIds.add(sourceId)

                val visible = isVisibleOnFloor(line.floor, selectedFloor)

                // Add outline layer first (renders behind main line)
                if (line.config.outlineColor != null) {
                    val outlineLayerId = lineOutlineLayerId(line.id)
                    val outlineLayer = LineLayer(outlineLayerId, sourceId).withProperties(
                        PropertyFactory.lineColor(line.config.outlineColor),
                        PropertyFactory.lineWidth(
                            Expression.interpolate(
                                Expression.linear(),
                                Expression.zoom(),
                                Expression.stop(10, line.config.minLineWidth + 2f),
                                Expression.stop(20, line.config.maxLineWidth + 2f),
                            ),
                        ),
                        PropertyFactory.lineCap(Property.LINE_CAP_ROUND),
                        PropertyFactory.lineJoin(Property.LINE_JOIN_ROUND),
                        PropertyFactory.visibility(if (visible) Property.VISIBLE else Property.NONE),
                    )
                    if (line.minZoom > 0.0) {
                        outlineLayer.minZoom = line.minZoom.toFloat()
                    }
                    style.addLayer(outlineLayer)
                    addedLayerIds.add(outlineLayerId)
                }

                // Add main line layer
                val lineLayer = LineLayer(layerId, sourceId).withProperties(
                    PropertyFactory.lineColor(line.config.color),
                    PropertyFactory.lineWidth(
                        Expression.interpolate(
                            Expression.linear(),
                            Expression.zoom(),
                            Expression.stop(10, line.config.minLineWidth),
                            Expression.stop(20, line.config.maxLineWidth),
                        ),
                    ),
                    PropertyFactory.lineCap(Property.LINE_CAP_ROUND),
                    PropertyFactory.lineJoin(Property.LINE_JOIN_ROUND),
                    PropertyFactory.visibility(if (visible) Property.VISIBLE else Property.NONE),
                )
                if (line.minZoom > 0.0) {
                    lineLayer.minZoom = line.minZoom.toFloat()
                }
                style.addLayer(lineLayer)
                addedLayerIds.add(layerId)
            }
        }
    }

    private fun addCircleLayers(selectedFloor: Int) {
        map.getStyle { style ->
            currentCircles.forEach { circle ->
                val sourceId = circleSourceId(circle.id)
                val layerId = circleLayerId(circle.id)

                val point = Point.fromLngLat(circle.coordinates.longitude, circle.coordinates.latitude)
                val feature = Feature.fromGeometry(point)
                val source = GeoJsonSource(sourceId, FeatureCollection.fromFeature(feature))
                style.addSource(source)
                addedSourceIds.add(sourceId)

                val visible = isVisibleOnFloor(circle.floor, selectedFloor)

                val circleLayer = CircleLayer(layerId, sourceId).withProperties(
                    PropertyFactory.circleColor(if (visible) circle.color else circle.disabledColor),
                    PropertyFactory.circleRadius(
                        Expression.interpolate(
                            Expression.linear(),
                            Expression.zoom(),
                            Expression.stop(10, circle.minRadius),
                            Expression.stop(20, circle.maxRadius),
                        ),
                    ),
                )
                style.addLayer(circleLayer)
                addedLayerIds.add(layerId)
            }
        }
    }

    private fun addFloorConnectorLayers(selectedFloor: Int) {
        map.getStyle { style ->
            currentFloorConnectors.forEach { connector ->
                val sourceId = connectorSourceId(connector.id)
                val layerId = connectorLayerId(connector.id)
                val imageId = connectorImageId(connector.id)

                val point = Point.fromLngLat(connector.coordinates.longitude, connector.coordinates.latitude)
                val feature = Feature.fromGeometry(point)
                val source = GeoJsonSource(sourceId, FeatureCollection.fromFeature(feature))
                style.addSource(source)
                addedSourceIds.add(sourceId)

                style.addImage(imageId, connector.icon)
                addedImageIds.add(imageId)

                val visible = connector.originFloor == selectedFloor

                val symbolLayer = SymbolLayer(layerId, sourceId).withProperties(
                    PropertyFactory.iconImage(imageId),
                    PropertyFactory.iconAllowOverlap(true),
                    PropertyFactory.iconIgnorePlacement(true),
                    PropertyFactory.visibility(if (visible) Property.VISIBLE else Property.NONE),
                )
                if (connector.minZoom > 0.0) {
                    symbolLayer.minZoom = connector.minZoom.toFloat()
                }
                style.addLayer(symbolLayer)
                addedLayerIds.add(layerId)
            }
        }
    }

    private fun removeCircleLayers() {
        map.getStyle { style ->
            currentCircles.forEach { circle ->
                val layerId = circleLayerId(circle.id)
                val sourceId = circleSourceId(circle.id)
                style.removeLayer(layerId)
                style.removeSource(sourceId)
                addedLayerIds.remove(layerId)
                addedSourceIds.remove(sourceId)
            }
        }
    }

    private fun removeFloorConnectorLayers() {
        map.getStyle { style ->
            currentFloorConnectors.forEach { connector ->
                val layerId = connectorLayerId(connector.id)
                val sourceId = connectorSourceId(connector.id)
                val imageId = connectorImageId(connector.id)
                style.removeLayer(layerId)
                style.removeSource(sourceId)
                style.removeImage(imageId)
                addedLayerIds.remove(layerId)
                addedSourceIds.remove(sourceId)
                addedImageIds.remove(imageId)
            }
        }
    }

    private fun isVisibleOnFloor(itemFloor: Int?, selectedFloor: Int): Boolean =
        itemFloor == null || itemFloor == selectedFloor

    private companion object {
        fun lineSourceId(id: String) = "sbb-path-line-source-$id"
        fun lineLayerId(id: String) = "sbb-path-line-layer-$id"
        fun lineOutlineLayerId(id: String) = "sbb-path-line-outline-$id"
        fun circleSourceId(id: String) = "sbb-path-circle-source-$id"
        fun circleLayerId(id: String) = "sbb-path-circle-layer-$id"
        fun connectorSourceId(id: String) = "sbb-path-connector-source-$id"
        fun connectorLayerId(id: String) = "sbb-path-connector-layer-$id"
        fun connectorImageId(id: String) = "sbb-path-connector-image-$id"
    }
}
