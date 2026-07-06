// SPDX-License-Identifier: MIT
// Copyright (c) 2024 Schweizerische Bundesbahnen AG (Swiss Federal Railways)

package ch.sbb.maps.poi

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import ch.sbb.maps.design.SBBCheckbox

internal val DEFAULT_POI_FILTER_SUB_CATEGORIES: List<String> =
    listOf(
        SBBPoiCategoryType.PARK_RAIL.value,
        SBBPoiCategoryType.PARKING_PLACE.value,
        SBBPoiCategoryType.CAR_SHARING.value,
        SBBPoiCategoryType.P2P_CAR_SHARING.value,
        SBBPoiCategoryType.BIKE_PARKING.value,
        SBBPoiCategoryType.BIKE_SHARING.value,
        SBBPoiCategoryType.ON_DEMAND.value,
    )

/**
 * POI subcategory checkbox selection panel.
 *
 * Displays a vertical list of [ch.sbb.maps.design.SBBCheckbox] items for each default POI subcategory.
 * Reports the currently selected subcategories via [onSelectionChange] whenever
 * the user toggles a checkbox.
 *
 * @param selectedPoiSubcategories initially selected [SBBPoiCategoryType] values.
 * @param onSelectionChange callback with the full list of currently checked subcategories.
 */
@Composable
public fun SBBPoiSelection(
    selectedPoiSubcategories: List<String> = DEFAULT_POI_FILTER_SUB_CATEGORIES,
    onSelectionChange: (List<String>) -> Unit,
) {
    val checkboxStates = remember { mutableStateMapOf<String, MutableState<Boolean>>() }
    selectedPoiSubcategories.forEach { subcategory ->
        val isSubcategorySelected = selectedPoiSubcategories.contains(subcategory)
        checkboxStates.putIfAbsent(subcategory, mutableStateOf(isSubcategorySelected))
    }

    Column(
        Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background),
    ) {
        selectedPoiSubcategories.forEach { subcategory ->
            val checkboxState = checkboxStates[subcategory] ?: mutableStateOf(false)
            SBBCheckbox(
                checked = checkboxState.value,
                onCheckedChange = { isChecked ->
                    if (isChecked != null) {
                        checkboxState.value = isChecked
                    }
                    val selectedSubcategories =
                        checkboxStates.filter { it.value.value }.keys.toList()
                    onSelectionChange(selectedSubcategories)
                },
                label = subcategory,
            )
        }
    }
}
