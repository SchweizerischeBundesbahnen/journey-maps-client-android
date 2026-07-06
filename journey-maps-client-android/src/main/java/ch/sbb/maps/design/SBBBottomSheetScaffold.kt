// SPDX-License-Identifier: MIT
// Copyright (c) 2024 Schweizerische Bundesbahnen AG (Swiss Federal Railways)

package ch.sbb.maps.design

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.BottomSheetScaffoldState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

/**
 * Based on [SBB Mobile Design System](https://digital.sbb.ch/de/design-system/mobile/overview/).
 * Note: This component may be replaced by the official SBB Design System library in the future.
 *
 * [BottomSheetScaffold] wrapper with no drag handle and zero peek height.
 * Fully collapsed by default; expand programmatically via [bottomSheetScaffoldState].
 *
 * @param bottomSheetScaffoldState controls the sheet expansion state.
 * @param sheetContent composable content rendered inside the bottom sheet.
 * @param content main screen content above the sheet.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
public fun SBBBottomSheetScaffold(
    bottomSheetScaffoldState: BottomSheetScaffoldState,
    sheetContent: @Composable ColumnScope.() -> Unit,
    content: @Composable (PaddingValues) -> Unit,
) {
    BottomSheetScaffold(
        scaffoldState = bottomSheetScaffoldState,
        sheetPeekHeight = 0.dp, // disable partly Expanded view with drag
        sheetDragHandle = null, // removed handle bar on default BottomSheet
        sheetContent = sheetContent,
        content = content,
    )
}
