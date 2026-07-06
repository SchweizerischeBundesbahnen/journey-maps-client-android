// SPDX-License-Identifier: MIT
// Copyright (c) 2024 Schweizerische Bundesbahnen AG (Swiss Federal Railways)

package ch.sbb.maps.annotation

import androidx.annotation.ColorInt
import androidx.annotation.Px
import org.maplibre.android.maps.Style
import org.maplibre.android.style.layers.FillLayer
import org.maplibre.android.style.layers.LineLayer
import org.maplibre.android.style.layers.PropertyFactory
import org.maplibre.android.style.sources.GeoJsonSource
import java.util.UUID

private const val JOURNEY_POIS_AREAS_SOURCE_ID: String = "journey-pois-areas-source"

/**
 * Represents a GeoJSON overlay that can be rendered on the SBB map.
 *
 * Obtain instances via the companion object factory operators:
 * - SBBGeoJson() → [EMPTY] (no overlay).
 * - SBBGeoJson(geoJson, fillColor, lineColor, lineWidth) → [CustomGeoJson].
 * - SBBGeoJson(geoJson, sourceId) → [IntegratedGeoJson], writes into an existing style source.
 *
 */
public sealed class SBBGeoJson {
    internal abstract val id: String
    internal abstract val geoJson: String

    internal val fillLayerId: String get() = "${id}_fill_layer"
    internal val lineLayerId: String get() = "${id}_line_layer"
    internal open val sourceId: String get() = "${id}_source"

    public val isEmpty: Boolean get() = this is Empty

    internal object Empty : SBBGeoJson() {
        override val id: String = ""
        override val geoJson: String = ""
    }

    public class CustomGeoJson internal constructor(
        override val id: String,
        override val geoJson: String,
        @get:ColorInt public val fillColor: Int,
        @get:ColorInt public val lineColor: Int,
        public val lineWidthPx: Float,
    ) : SBBGeoJson()

    public class IntegratedGeoJson internal constructor(
        override val id: String,
        override val geoJson: String,
        override val sourceId: String,
    ) : SBBGeoJson()

    override fun equals(other: Any?): Boolean = other is SBBGeoJson && other.id == id

    override fun hashCode(): Int = id.hashCode()

    public companion object {
        public val EMPTY: SBBGeoJson = Empty

        private const val EMPTY_FEATURE_COLLECTION =
            """{"type":"FeatureCollection","features":[]}"""

        public operator fun invoke(): SBBGeoJson = Empty

        /**
         * Renders on top of the style with fill, outline color, and outline width.
         */
        public operator fun invoke(
            geoJson: String,
            @ColorInt fillColor: Int,
            @ColorInt lineColor: Int,
            @Px lineWidth: Int,
        ): SBBGeoJson =
            CustomGeoJson(
                id = UUID.randomUUID().toString(),
                geoJson = geoJson,
                fillColor = fillColor,
                lineColor = lineColor,
                lineWidthPx = lineWidth.toFloat(),
            )

        /**
         * Places [geoJson] in an existing source of the currently displayed Journey Maps style.
         */
        public operator fun invoke(
            geoJson: String,
            sourceId: String = JOURNEY_POIS_AREAS_SOURCE_ID,
        ): SBBGeoJson =
            IntegratedGeoJson(
                id = UUID.randomUUID().toString(),
                geoJson = geoJson,
                sourceId = sourceId,
            )

        internal fun removeFromStyle(
            style: Style,
            geoJson: SBBGeoJson,
        ) {
            if (geoJson.isEmpty) return
            when (geoJson) {
                is IntegratedGeoJson -> {
                    style
                        .getSourceAs<GeoJsonSource>(geoJson.sourceId)
                        ?.setGeoJson(EMPTY_FEATURE_COLLECTION)
                }

                is CustomGeoJson -> {
                    if (style.getLayerAs<LineLayer>(geoJson.lineLayerId) != null) {
                        style.removeLayer(geoJson.lineLayerId)
                    }
                    if (style.getLayerAs<FillLayer>(geoJson.fillLayerId) != null) {
                        style.removeLayer(geoJson.fillLayerId)
                    }
                    if (style.getSourceAs<GeoJsonSource>(geoJson.sourceId) != null) {
                        style.removeSource(geoJson.sourceId)
                    }
                }
                else -> {}
            }
        }

        internal fun addToStyle(
            style: Style,
            geoJson: SBBGeoJson,
        ) {
            if (geoJson.isEmpty || geoJson.geoJson.isBlank()) return
            when (geoJson) {
                is IntegratedGeoJson -> {
                    style.getSourceAs<GeoJsonSource>(geoJson.sourceId)?.setGeoJson(geoJson.geoJson)
                }

                is CustomGeoJson -> {
                    if (style.getSourceAs<GeoJsonSource>(geoJson.sourceId) != null) return
                    val source = GeoJsonSource(geoJson.sourceId, geoJson.geoJson)
                    style.addSource(source)
                    val fillLayer =
                        FillLayer(geoJson.fillLayerId, geoJson.sourceId).withProperties(
                            PropertyFactory.fillColor(geoJson.fillColor),
                        )
                    style.addLayer(fillLayer)
                    val lineLayer =
                        LineLayer(geoJson.lineLayerId, geoJson.sourceId).withProperties(
                            PropertyFactory.lineColor(geoJson.lineColor),
                            PropertyFactory.lineWidth(geoJson.lineWidthPx),
                        )
                    style.addLayer(lineLayer)
                }
                else -> {}
            }
        }
    }
}
