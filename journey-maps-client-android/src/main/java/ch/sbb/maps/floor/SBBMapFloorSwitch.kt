// SPDX-License-Identifier: MIT
// Copyright (c) 2024 Schweizerische Bundesbahnen AG (Swiss Federal Railways)

package ch.sbb.maps.floor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import ch.sbb.maps.theme.SBBMapTheme
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

/**
 * Vertical floor level switcher for indoor maps.
 *
 * Renders a column of buttons — one per available floor — and highlights
 * the currently selected floor.
 *
 * @param floors available floor numbers to display.
 * @param onFloorSelected callback invoked with the selected floor number.
 */
@Composable
internal fun SBBMapFloorSwitch(
    floors: List<Int>,
    onFloorSelected: (Int) -> Unit,
) {
    var selectedFloor by remember { mutableStateOf<Int?>(null) }
    val items =
        floors.sortedDescending().map { floor ->
            SBBMapFloorSwitchItemType.TextItem(
                text = floor.toString(),
                onClick = {
                    selectedFloor = floor
                    onFloorSelected(floor)
                },
            )
        }

    Column(
        modifier =
            Modifier
                .wrapContentSize()
                .padding(vertical = 15.dp, horizontal = 15.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.End,
    ) {
        items.forEachIndexed { index, item ->
            val isFirstItem = index == 0
            val isLastItem = index == items.lastIndex
            val isSelected = selectedFloor == item.text.toInt()

            SBBMapFloorSwitchItem(
                item = item,
                isFirstItem = isFirstItem,
                isLastItem = isLastItem,
                isSelected = isSelected,
            )
        }
    }
}

@Preview
@Composable
private fun ConfigurationBoardLight() {
    SBBMapTheme {
        SBBMapFloorSwitch(
            floors = listOf(1, 2, 3, 4, 5),
            onFloorSelected = { /* no-op for preview */ },
        )
    }
}

@Preview
@Composable
private fun ConfigurationBoardDark() {
    SBBMapTheme(darkTheme = true) {
        SBBMapFloorSwitch(
            floors = listOf(1, 2, 3, 4, 5),
            onFloorSelected = { /* no-op for preview */ },
        )
    }
}
