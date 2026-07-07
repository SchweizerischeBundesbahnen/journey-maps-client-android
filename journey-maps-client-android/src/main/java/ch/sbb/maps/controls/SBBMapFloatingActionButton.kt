// SPDX-License-Identifier: MIT
// Copyright (c) 2024 Schweizerische Bundesbahnen AG (Swiss Federal Railways)

package ch.sbb.maps.controls

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ch.sbb.maps.R
import ch.sbb.maps.theme.SBBMapTheme

/**
 * A circular floating action button styled for SBB map overlay controls.
 *
 * @param onClick action performed when the button is tapped.
 * @param modifier optional [Modifier] for sizing and positioning.
 * @param backgroundColor overrides the default container colour.
 * @param contentColor overrides the default icon/text colour.
 * @param text optional text label displayed inside the FAB.
 * @param icon optional [ImageVector] icon displayed inside the FAB.
 * @param iconDescription accessibility content description for [icon].
 */
@Composable
internal fun SBBMapFloatingActionButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    backgroundColor: Color? = null,
    contentColor: Color? = null,
    text: String? = null,
    icon: ImageVector? = null,
    iconDescription: String? = null,
) {
    val colorScheme = MaterialTheme.colorScheme
    FloatingActionButton(
        onClick = onClick,
        shape = CircleShape,
        modifier =
            modifier
                .padding(
                    horizontal = mapFabPaddingHorizontal,
                    vertical = mapFabPaddingVertical,
                ).border(1.dp, colorScheme.surfaceVariant, shape = RoundedCornerShape(mapFabSize))
                .size(mapFabSize),
        containerColor = backgroundColor ?: colorScheme.inversePrimary,
        contentColor = contentColor ?: colorScheme.onPrimary,
    ) {
        Row {
            icon?.let {
                Icon(
                    imageVector = it,
                    contentDescription = iconDescription,
                )
                if (text != null) {
                    Spacer(Modifier.width(ButtonDefaults.IconSpacing))
                }
            }
            text?.let {
                Text(it)
            }
        }
    }
}

@Preview
@Composable
private fun SBBMapFloatingActionButtonPreviewLight() {
    SBBMapTheme {
        SBBMapFloatingActionButton(
            onClick = {},
            icon = ImageVector.vectorResource(R.drawable.switzerland_small),
        )
    }
}

@Preview
@Composable
private fun SBBMapFloatingActionButtonPreviewDark() {
    SBBMapTheme(darkTheme = true) {
        SBBMapFloatingActionButton(
            onClick = {},
            icon = ImageVector.vectorResource(R.drawable.switzerland_small),
        )
    }
}
