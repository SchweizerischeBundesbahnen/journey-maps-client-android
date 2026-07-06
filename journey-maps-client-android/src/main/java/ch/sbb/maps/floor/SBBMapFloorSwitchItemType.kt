// SPDX-License-Identifier: MIT
// Copyright (c) 2024 Schweizerische Bundesbahnen AG (Swiss Federal Railways)

package ch.sbb.maps.floor

import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Content model for a single item in the [SBBMapFloorSwitch].
 *
 * Subclasses represent different visual representations (text label or icon).
 */
internal sealed class SBBMapFloorSwitchItemType(
    internal open val onClick: () -> Unit,
) {
    internal data class TextItem(
        val text: String,
        override val onClick: () -> Unit,
    ) : SBBMapFloorSwitchItemType(onClick)

    internal data class IconItem(
        val icon: ImageVector,
        override val onClick: () -> Unit,
    ) : SBBMapFloorSwitchItemType(onClick)
}
