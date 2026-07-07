// SPDX-License-Identifier: MIT
// Copyright (c) 2024 Schweizerische Bundesbahnen AG (Swiss Federal Railways)

package ch.sbb.maps.design

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Based on [SBB Mobile Design System](https://digital.sbb.ch/de/design-system/mobile/overview/).
 * Note: This component may be replaced by the official SBB Design System library in the future.
 *
 * Horizontal radio button group for selecting one option from a list.
 *
 * @param radioOptions selectable option labels.
 * @param selectedOption the currently selected label.
 * @param onOptionSelected callback invoked with the newly selected label.
 */
@Composable
public fun SBBRadioButtonGroup(
    radioOptions: List<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
    ) {
        radioOptions.forEach { themeMode ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(end = 20.dp), // Add padding between radio button groups
            ) {
                RadioButton(
                    selected = (themeMode == selectedOption),
                    onClick = { onOptionSelected(themeMode) },
                )
                Text(
                    text = themeMode,
                    style = SBBTypography.bodyMedium, // Replace with your typography
                    modifier = Modifier.padding(start = 5.dp),
                )
            }
        }
    }
}
