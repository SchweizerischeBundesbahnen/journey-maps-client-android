// SPDX-License-Identifier: MIT
// Copyright (c) 2024 Schweizerische Bundesbahnen AG (Swiss Federal Railways)

package ch.sbb.maps.design

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonElevation
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Based on [SBB Mobile Design System](https://digital.sbb.ch/de/design-system/mobile/overview/).
 * Note: This component may be replaced by the official SBB Design System library in the future.
 *
 * Outlined secondary button with a full width rounded shape and text label.
 *
 * @param onClick callback invoked on button press.
 * @param label text label displayed inside the button.
 * @param enabled whether the button is interactable.
 */
@Composable
public fun SBBSecondaryButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    enabled: Boolean = true,
    shape: Shape = RoundedCornerShape(22.dp),
    colors: ButtonColors? = null,
    border: BorderStroke? = null,
    elevation: ButtonElevation? = ButtonDefaults.buttonElevation(),
    contentPadding: PaddingValues = ButtonDefaults.ContentPadding,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    label: String,
    textStyle: TextStyle? = null,
) {
    val colorScheme = MaterialTheme.colorScheme

    val defaultBorder =
        BorderStroke(1.dp, if (enabled) colorScheme.outline else colorScheme.outlineVariant)
    val defaultTextStyle =
        TextStyle(
            color = if (enabled) colorScheme.onPrimaryContainer else colorScheme.onSecondaryContainer,
            fontSize = 16.sp,
        )
    val isButtonPressed = interactionSource.collectIsPressedAsState().value
    val defaultButtonColors =
        ButtonDefaults.textButtonColors(
            containerColor = if (!isButtonPressed) colorScheme.secondaryContainer else colorScheme.secondary,
            contentColor = colorScheme.secondary,
            disabledContainerColor = colorScheme.inversePrimary,
        )

    Button(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(5.dp)
                .height(44.dp),
        onClick = onClick,
        enabled = enabled,
        shape = shape,
        colors = colors ?: defaultButtonColors,
        elevation = elevation,
        border = border ?: defaultBorder,
        contentPadding = contentPadding,
        interactionSource = interactionSource,
    ) {
        Text(
            text = label,
            style = textStyle ?: defaultTextStyle,
            textAlign = TextAlign.Center,
        )
    }
}

@Preview
@Composable
public fun SBBSecondaryButtonPreview() {
    SBBSecondaryButton(
        label = "SBB Secondary Button",
        onClick = {},
    )
}
