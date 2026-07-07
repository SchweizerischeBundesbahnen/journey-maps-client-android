// SPDX-License-Identifier: MIT
// Copyright (c) 2024 Schweizerische Bundesbahnen AG (Swiss Federal Railways)

package ch.sbb.maps.annotation

import androidx.annotation.ColorInt
import org.maplibre.android.geometry.LatLng

/**
 * A circle marker rendered on the map with optional floor filtering.
 *
 * Circles are scaled between [minRadius] and [maxRadius] depending on the
 * current zoom level. When [floor] is set, the circle is only visible on
 * that floor level.
 *
 * @property id unique identifier used as the MapLibre layer/source suffix.
 * @property coordinates center point of the circle.
 * @property minRadius minimum radius in density independent pixels at low zoom.
 * @property maxRadius maximum radius in density independent pixels at high zoom.
 * @property color fill color as an ARGB packed integer.
 * @property disabledColor color used when the circle is on a non selected floor.
 * @property floor optional floor level. When null, the circle is shown on all floors.
 */
public data class SBBCircle(
    val id: String,
    val coordinates: LatLng,
    val minRadius: Float = 5f,
    val maxRadius: Float = 10f,
    @param:ColorInt val color: Int,
    @param:ColorInt val disabledColor: Int,
    val floor: Int? = null,
)
