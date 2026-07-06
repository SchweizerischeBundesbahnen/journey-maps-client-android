// SPDX-License-Identifier: MIT
// Copyright (c) 2024 Schweizerische Bundesbahnen AG (Swiss Federal Railways)

package ch.sbb.maps.annotation

import androidx.annotation.ColorInt
import org.maplibre.android.geometry.LatLng

/**
 * Visual configuration for an [SBBLine].
 *
 * @property minLineWidth minimum line width in density independent pixels at low zoom.
 * @property maxLineWidth maximum line width in density independent pixels at high zoom.
 * @property color line color as an ARGB packed integer.
 * @property outlineColor optional outline color drawn behind the main line.
 */
public data class SBBLineConfig(
    val minLineWidth: Float = 2f,
    val maxLineWidth: Float = 8f,
    @param:ColorInt val color: Int,
    @param:ColorInt val outlineColor: Int? = null,
)

/**
 * A polyline segment rendered on the map with optional floor filtering.
 *
 * Lines that share a floor value are shown or hidden together when the user
 * switches floors via the floor selector. A line without a floor (null) is
 * always visible regardless of the selected floor.
 *
 * @property id unique identifier used as the MapLibre layer/source suffix.
 * @property coordinates ordered list of points forming the polyline.
 * @property config visual styling via [SBBLineConfig].
 * @property minZoom minimum map zoom level at which this line becomes visible.
 * @property floor optional floor level. When null, the line is shown on all floors.
 */
public data class SBBLine(
    val id: String,
    val coordinates: List<LatLng>,
    val config: SBBLineConfig,
    val minZoom: Double = 0.0,
    val floor: Int? = null,
)
