// SPDX-License-Identifier: MIT
// Copyright (c) 2024 Schweizerische Bundesbahnen AG (Swiss Federal Railways)

package ch.sbb.maps.design

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import ch.sbb.maps.R

/**
 * Based on [SBB Mobile Design System](https://digital.sbb.ch/de/design-system/mobile/overview/).
 * Note: This component may be replaced by the official SBB Design System library in the future.
 *
 * Bottom sheet with a header, scrollable body, and action button.
 * Used to present POI details or contextual information overlaying the map.
 *
 * @param title text displayed in the sheet header.
 * @param body optional composable content for the scrollable body area.
 * @param contentColor background colour of the body section.
 * @param maxHeight maximum height as a fraction of screen height (0–1).
 * @param onCloseSheet callback invoked when the close icon is tapped.
 * @param buttonLabel label for the action button; null hides the label.
 * @param onButtonClick optional callback for the action button; defaults to [onCloseSheet].
 */
@Composable
public fun SBBBottomSheetContent(
    title: String,
    body: (@Composable BoxScope.() -> Unit)? = null,
    contentColor: Color = SBBColors.milk,
    maxHeight: Float = 0.45f,
    onCloseSheet: () -> Unit,
    buttonLabel: String?,
    onButtonClick: (() -> Unit?)? = null,
) {
    val scrollState = rememberScrollState()
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp
    val sheetMaxHeight = screenHeight * maxHeight

    val defaultPadding = 16.dp
    val closeIconSize = 32.dp
    val headerHeight = 64.dp
    val buttonHeight = 44.dp
    val maxBodyHeight = sheetMaxHeight - headerHeight - buttonHeight - defaultPadding * 3

    Box(
        Modifier
            .fillMaxWidth()
            .heightIn(max = sheetMaxHeight)
            .background(MaterialTheme.colorScheme.inverseSurface),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            Modifier
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // SBB Bottom Sheet Header
            Row(
                modifier =
                    Modifier
                        .height(headerHeight)
                        .padding(defaultPadding),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    modifier = Modifier.weight(1f),
                    text = title,
                    color = MaterialTheme.colorScheme.onPrimary,
                )
                Icon(
                    imageVector = ImageVector.vectorResource(id = R.drawable.circle_cross_medium),
                    contentDescription = "SBB Bottom Sheet Title",
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier =
                        Modifier
                            .size(closeIconSize)
                            .clickable {
                                onCloseSheet.invoke()
                            },
                )
            }
            // SBB Bottom Sheet Body
            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .heightIn(max = maxBodyHeight)
                        .verticalScroll(scrollState)
                        .background(contentColor)
                        .padding(
                            top = defaultPadding / 2,
                            bottom = defaultPadding,
                            start = defaultPadding,
                            end = defaultPadding,
                        ),
            ) {
                body?.invoke(this)
            }

            // SBB Bottom Sheet Button
            Button(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(defaultPadding / 2)
                        .height(buttonHeight),
                onClick = {
                    if (onButtonClick != null) onButtonClick.invoke() else onCloseSheet.invoke()
                },
            ) {
                if (buttonLabel != null) {
                    Text(
                        buttonLabel,
                        style = SBBTypography.titleMedium,
                        color = SBBColors.white,
                    )
                }
            }
        }
    }
}
