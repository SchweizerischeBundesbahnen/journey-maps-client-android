// SPDX-License-Identifier: MIT
// Copyright (c) 2024 Schweizerische Bundesbahnen AG (Swiss Federal Railways)

package ch.sbb.maps.controls

import android.view.Gravity
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// Shared layout constants for MapControllerOverlay FABs
internal val mapFabPaddingHorizontal: Dp = 15.dp
internal val mapFabPaddingVertical: Dp = 10.dp
internal val mapFabSize: Dp = 46.dp

@Composable
internal fun rememberAttributionMarginLeft(): Int {
    val configuration = LocalConfiguration.current
    return with(LocalDensity.current) { (configuration.screenWidthDp.dp - 24.dp).toPx() }.toInt()
}

@Composable
internal fun MapCompassInsetSpacer() {
    Spacer(
        modifier =
            Modifier
                .padding(
                    horizontal = mapFabPaddingHorizontal,
                    vertical = mapFabPaddingVertical,
                ).size(mapFabSize),
    )
}

/**
 * Alignment options for the map controller overlay button column.
 *
 * Controls where the location, satellite toggle, and floor switcher buttons
 * are placed relative to the map edges.
 *
 */
public enum class SBBMapControlsAlignment {
    TopStart,
    TopEnd,
    BottomStart,
    BottomEnd,
    ;

    internal fun toAlignment(): Alignment =
        when (this) {
            TopStart -> Alignment.TopStart
            TopEnd -> Alignment.TopEnd
            BottomStart -> Alignment.BottomStart
            BottomEnd -> Alignment.BottomEnd
        }

    internal fun toGravity(): Int =
        when (this) {
            TopStart -> Gravity.TOP or Gravity.START
            TopEnd -> Gravity.TOP or Gravity.END
            BottomStart -> Gravity.BOTTOM or Gravity.START
            BottomEnd -> Gravity.BOTTOM or Gravity.END
        }

    internal fun isBottomAligned(): Boolean = this == BottomStart || this == BottomEnd
}
