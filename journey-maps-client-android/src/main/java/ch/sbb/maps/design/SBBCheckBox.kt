// SPDX-License-Identifier: MIT
// Copyright (c) 2024 Schweizerische Bundesbahnen AG (Swiss Federal Railways)

package ch.sbb.maps.design

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxColors
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp

/**
 * Based on [SBB Mobile Design System](https://digital.sbb.ch/de/design-system/mobile/overview/).
 * Note: This component may be replaced by the official SBB Design System library in the future.
 *
 * Labeled checkbox where the entire row (checkbox + label) is clickable.
 *
 * @param label text displayed next to the checkbox.
 * @param checked initial checked state.
 * @param onCheckedChange callback fired when the state changes.
 * @param enabled whether the checkbox is interactable.
 */
@Composable
public fun SBBCheckbox(
    modifier: Modifier = Modifier,
    checkboxModifier: Modifier = Modifier,
    textModifier: Modifier = Modifier,
    label: String,
    checked: Boolean = false,
    onCheckedChange: ((Boolean?) -> Unit)? = null,
    enabled: Boolean = true,
    colors: CheckboxColors =
        CheckboxDefaults.colors(
            checkmarkColor = SBBColors.white,
        ),
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
) {
    val currentCheckedState = remember { mutableStateOf(checked) }

    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .height(40.dp)
                .clickable(
                    role = Role.Checkbox,
                    onClick = {
                        val newCheckedState = !(currentCheckedState.value)
                        currentCheckedState.value = newCheckedState
                        onCheckedChange?.invoke(newCheckedState)
                    },
                ).padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Checkbox(
            checked = currentCheckedState.value,
            onCheckedChange = null,
            modifier = checkboxModifier,
            enabled = enabled,
            colors = colors,
            interactionSource = interactionSource,
        )
        Text(
            modifier = textModifier,
            text = label,
            color = MaterialTheme.colorScheme.onBackground,
        )
    }
}
