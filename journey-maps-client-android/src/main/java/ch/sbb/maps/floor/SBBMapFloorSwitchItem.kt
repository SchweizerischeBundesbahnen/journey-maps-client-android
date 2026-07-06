// SPDX-License-Identifier: MIT
// Copyright (c) 2024 Schweizerische Bundesbahnen AG (Swiss Federal Railways)

package ch.sbb.maps.floor

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ch.sbb.maps.theme.SBBMapTheme

@Composable
internal fun SBBMapFloorSwitchItem(
    item: SBBMapFloorSwitchItemType,
    isFirstItem: Boolean,
    isLastItem: Boolean,
    isSelected: Boolean,
) {
    val topShape = if (isFirstItem) 24.dp else 0.dp
    val bottomShape = if (isLastItem) 24.dp else 0.dp
    val shape =
        RoundedCornerShape(
            topStart = topShape,
            topEnd = topShape,
            bottomEnd = bottomShape,
            bottomStart = bottomShape,
        )

    val selectedItemTopShape = if (isFirstItem) 24.dp else 8.dp
    val selectedItemBottomShape = if (isLastItem) 24.dp else 8.dp
    val selectedItemShape =
        RoundedCornerShape(
            topStart = selectedItemTopShape,
            topEnd = selectedItemTopShape,
            bottomEnd = selectedItemBottomShape,
            bottomStart = selectedItemBottomShape,
        )

    val colorScheme = MaterialTheme.colorScheme
    val buttonOutlineModifier =
        Modifier
            .size(width = 46.dp, height = 46.dp)

    val buttonInsideBoxModifier =
        Modifier
            .size(width = 44.dp, height = 45.dp)
            .background(color = colorScheme.background, shape = shape)

    val selectedItemModifier =
        Modifier
            .size(width = 36.dp, height = 36.dp)
            .background(color = colorScheme.onBackground, shape = selectedItemShape)

    Button(
        onClick = { item.onClick() },
        modifier = buttonOutlineModifier,
        shape = shape,
        colors =
            ButtonDefaults.buttonColors(
                containerColor = colorScheme.surfaceVariant, // stroke color
            ),
        contentPadding = PaddingValues(0.dp),
    ) {
        Box(
            modifier = buttonInsideBoxModifier,
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                if (isSelected) {
                    Box(
                        modifier = selectedItemModifier,
                    )
                }
                FloorSwitchItemContent(
                    item = item,
                    colorScheme = colorScheme,
                    isSelected = isSelected,
                )
            }
        }
    }
}

@Composable
internal fun FloorSwitchItemContent(
    item: SBBMapFloorSwitchItemType,
    colorScheme: ColorScheme,
    isSelected: Boolean,
) {
    when (item) {
        is SBBMapFloorSwitchItemType.TextItem -> {
            Text(
                text = item.text,
                color = if (isSelected) colorScheme.background else colorScheme.onBackground,
                // Add any additional modifiers for text styling if needed
            )
        }

        is SBBMapFloorSwitchItemType.IconItem -> {
            Icon(
                imageVector = item.icon,
                contentDescription = "SBB Map Level Switch Icon",
                tint = if (isSelected) colorScheme.background else colorScheme.onBackground,
                modifier = Modifier.size(24.dp), // Ensure the icon fits within the button
            )
        }
    }
}

@Preview
@Composable
private fun SBBMapFloorSwitchItemFirstItemPreview() {
    SBBMapTheme {
        SBBMapFloorSwitchItem(
            item = SBBMapFloorSwitchItemType.TextItem(text = "1", onClick = {}),
            isFirstItem = true,
            isLastItem = false,
            isSelected = false,
        )
    }
}

@Preview
@Composable
private fun SBBMapFloorSwitchItemMiddleItemPreview() {
    SBBMapTheme {
        SBBMapFloorSwitchItem(
            item = SBBMapFloorSwitchItemType.TextItem(text = "2", onClick = {}),
            isFirstItem = false,
            isLastItem = false,
            isSelected = false,
        )
    }
}

@Preview
@Composable
private fun SBBMapFloorSwitchItemLastItemPreview() {
    SBBMapTheme {
        SBBMapFloorSwitchItem(
            item = SBBMapFloorSwitchItemType.TextItem(text = "3", onClick = {}),
            isFirstItem = false,
            isLastItem = true,
            isSelected = false,
        )
    }
}

@Preview
@Composable
private fun SBBMapFloorSwitchItemFirstItemPreviewDark() {
    SBBMapTheme(darkTheme = true) {
        SBBMapFloorSwitchItem(
            item = SBBMapFloorSwitchItemType.TextItem(text = "1", onClick = {}),
            isFirstItem = true,
            isLastItem = false,
            isSelected = false,
        )
    }
}

@Preview
@Composable
private fun SBBMapFloorSwitchItemMiddleItemPreviewDark() {
    SBBMapTheme(darkTheme = true) {
        SBBMapFloorSwitchItem(
            item = SBBMapFloorSwitchItemType.TextItem(text = "2", onClick = {}),
            isFirstItem = false,
            isLastItem = false,
            isSelected = false,
        )
    }
}

@Preview
@Composable
private fun SBBMapFloorSwitchItemLastItemPreviewDark() {
    SBBMapTheme(darkTheme = true) {
        SBBMapFloorSwitchItem(
            item = SBBMapFloorSwitchItemType.TextItem(text = "3", onClick = {}),
            isFirstItem = false,
            isLastItem = true,
            isSelected = false,
        )
    }
}

@Preview
@Composable
private fun SBBMapFloorSwitchItemFirstItemSelectedPreview() {
    SBBMapTheme {
        SBBMapFloorSwitchItem(
            item = SBBMapFloorSwitchItemType.TextItem(text = "1", onClick = {}),
            isFirstItem = true,
            isLastItem = false,
            isSelected = true,
        )
    }
}

@Preview
@Composable
private fun SBBMapFloorSwitchItemMiddleItemSelectedPreview() {
    SBBMapTheme {
        SBBMapFloorSwitchItem(
            item = SBBMapFloorSwitchItemType.TextItem(text = "2", onClick = {}),
            isFirstItem = false,
            isLastItem = false,
            isSelected = true,
        )
    }
}

@Preview
@Composable
private fun SBBMapFloorSwitchItemLastItemSelectedPreview() {
    SBBMapTheme {
        SBBMapFloorSwitchItem(
            item = SBBMapFloorSwitchItemType.TextItem(text = "3", onClick = {}),
            isFirstItem = false,
            isLastItem = true,
            isSelected = true,
        )
    }
}

@Preview
@Composable
private fun SBBMapFloorSwitchItemFirstItemSelectedPreviewDark() {
    SBBMapTheme(darkTheme = true) {
        SBBMapFloorSwitchItem(
            item = SBBMapFloorSwitchItemType.TextItem(text = "1", onClick = {}),
            isFirstItem = true,
            isLastItem = false,
            isSelected = true,
        )
    }
}

@Preview
@Composable
private fun SBBMapFloorSwitchItemMiddleItemSelectedPreviewDark() {
    SBBMapTheme(darkTheme = true) {
        SBBMapFloorSwitchItem(
            item = SBBMapFloorSwitchItemType.TextItem(text = "2", onClick = {}),
            isFirstItem = false,
            isLastItem = false,
            isSelected = true,
        )
    }
}

@Preview
@Composable
private fun SBBMapFloorSwitchItemLastItemSelectedPreviewDark() {
    SBBMapTheme(darkTheme = true) {
        SBBMapFloorSwitchItem(
            item = SBBMapFloorSwitchItemType.TextItem(text = "3", onClick = {}),
            isFirstItem = false,
            isLastItem = true,
            isSelected = true,
        )
    }
}
