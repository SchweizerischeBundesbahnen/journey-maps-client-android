// SPDX-License-Identifier: MIT
// Copyright (c) 2024 Schweizerische Bundesbahnen AG (Swiss Federal Railways)

package ch.sbb.maps.design

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import ch.sbb.maps.R

/** Based on [SBB Mobile Design System](https://digital.sbb.ch/de/design-system/mobile/overview/).
 * Note: This may be replaced by the official SBB Design System library in the future.
 *
 * [FontFamily] built from bundled SBBWeb font resource files.
 */
public val SBBWebFontFamily: FontFamily =
    FontFamily(
        Font(R.font.sbbweb_italic, FontWeight.Normal, FontStyle.Italic),
        Font(R.font.sbbweb_light, FontWeight.Light),
        Font(R.font.sbbweb_roman, FontWeight.Normal),
        Font(R.font.sbbweb_thin, FontWeight.Thin),
        Font(R.font.sbbweb_ultra_light, FontWeight.ExtraLight),
    )

/**
 * [Typography] scale using SBB recommended font sizes and line heights.
 */
public val SBBTypography: Typography =
    Typography(
        displayLarge =
            TextStyle(
                fontFamily = SBBWebFontFamily,
                fontWeight = FontWeight.W300,
                fontSize = 30.sp,
                lineHeight = 38.sp,
            ),
        displayMedium =
            TextStyle(
                fontFamily = SBBWebFontFamily,
                fontWeight = FontWeight.W300,
                fontSize = 28.sp,
                lineHeight = 36.sp,
            ),
        displaySmall =
            TextStyle(
                fontFamily = SBBWebFontFamily,
                fontWeight = FontWeight.W300,
                fontSize = 26.sp,
                lineHeight = 34.sp,
            ),
        headlineLarge =
            TextStyle(
                fontFamily = SBBWebFontFamily,
                fontWeight = FontWeight.W700,
                fontSize = 18.sp,
                lineHeight = 24.sp,
            ),
        headlineMedium =
            TextStyle(
                fontFamily = SBBWebFontFamily,
                fontWeight = FontWeight.W700,
                fontSize = 16.sp,
                lineHeight = 22.sp,
            ),
        headlineSmall =
            TextStyle(
                fontFamily = SBBWebFontFamily,
                fontWeight = FontWeight.W700,
                fontSize = 14.sp,
                lineHeight = 20.sp,
            ),
        titleLarge =
            TextStyle(
                fontFamily = SBBWebFontFamily,
                fontWeight = FontWeight.W400,
                fontSize = 18.sp,
                lineHeight = 24.sp,
            ),
        titleMedium =
            TextStyle(
                fontFamily = SBBWebFontFamily,
                fontWeight = FontWeight.W400,
                fontSize = 16.sp,
                lineHeight = 22.sp,
            ),
        titleSmall =
            TextStyle(
                fontFamily = SBBWebFontFamily,
                fontWeight = FontWeight.W400,
                fontSize = 14.sp,
                lineHeight = 20.sp,
            ),
        bodyLarge =
            TextStyle(
                fontFamily = SBBWebFontFamily,
                fontWeight = FontWeight.W400,
                fontSize = 16.sp,
                lineHeight = 24.sp,
            ),
        bodyMedium =
            TextStyle(
                fontFamily = SBBWebFontFamily,
                fontWeight = FontWeight.W400,
                fontSize = 14.sp,
                lineHeight = 20.sp,
            ),
        bodySmall =
            TextStyle(
                fontFamily = SBBWebFontFamily,
                fontWeight = FontWeight.W400,
                fontSize = 12.sp,
                lineHeight = 18.sp,
            ),
        labelLarge =
            TextStyle(
                fontFamily = SBBWebFontFamily,
                fontWeight = FontWeight.W400,
                fontSize = 14.sp,
                lineHeight = 20.sp,
            ),
        labelMedium =
            TextStyle(
                fontFamily = SBBWebFontFamily,
                fontWeight = FontWeight.W400,
                fontSize = 12.sp,
                lineHeight = 16.sp,
            ),
        labelSmall =
            TextStyle(
                fontFamily = SBBWebFontFamily,
                fontWeight = FontWeight.W400,
                fontSize = 10.sp,
                lineHeight = 14.sp,
            ),
    )
