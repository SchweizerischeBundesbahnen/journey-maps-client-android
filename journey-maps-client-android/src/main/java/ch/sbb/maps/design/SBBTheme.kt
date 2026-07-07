// SPDX-License-Identifier: MIT
// Copyright (c) 2024 Schweizerische Bundesbahnen AG (Swiss Federal Railways)

package ch.sbb.maps.design

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.ColorScheme

/** Based on [SBB Mobile Design System](https://digital.sbb.ch/de/design-system/mobile/overview/).
 * Note: This may be replaced by the official SBB Design System library in the future.
 *
 * Light [ColorScheme] configured with [SBBColors].
 */
public val lightColorScheme: ColorScheme =
    lightColorScheme(
        primary = SBBColors.red,
        onPrimary = SBBColors.black,
        primaryContainer = SBBColors.red125,
        // disabled secondary button container Color
        inversePrimary = SBBColors.white,
        // secondary button content color
        onPrimaryContainer = SBBColors.red,
        // secondary button onPressed color
        secondary = SBBColors.graphite,
        // secondary button container color default
        secondaryContainer = SBBColors.white,
        // disabled secondary button text color
        onSecondaryContainer = SBBColors.graphite,
        // FloorSwitch outlines
        surfaceVariant = SBBColors.cloud,
        // modal background
        inverseSurface = SBBColors.milk,
        background = SBBColors.white,
        onBackground = SBBColors.black,
        surface = SBBColors.cloud,
        onSurface = SBBColors.black,
        outline = SBBColors.red,
        outlineVariant = SBBColors.cloud,
    )

/**
 * Dark [ColorScheme] configured with [SBBColors].
 */
public val darkColorScheme: ColorScheme =
    darkColorScheme(
        primary = SBBColors.redDarkMode,
        onPrimary = SBBColors.white,
        primaryContainer = SBBColors.red125,
        inversePrimary = SBBColors.black,
        onPrimaryContainer = SBBColors.white,
        secondary = SBBColors.charcoal,
        secondaryContainer = SBBColors.iron,
        onSecondaryContainer = SBBColors.smoke,
        surfaceVariant = SBBColors.metal,
        inverseSurface = SBBColors.midnight,
        background = SBBColors.black,
        onBackground = SBBColors.white,
        surface = SBBColors.cloud,
        onSurface = SBBColors.black,
        outline = SBBColors.smoke,
        outlineVariant = SBBColors.iron,
    )
