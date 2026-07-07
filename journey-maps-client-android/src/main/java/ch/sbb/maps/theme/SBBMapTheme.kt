// SPDX-License-Identifier: MIT
// Copyright (c) 2024 Schweizerische Bundesbahnen AG (Swiss Federal Railways)

package ch.sbb.maps.theme

import android.os.Build
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import ch.sbb.maps.design.SBBTypography
import ch.sbb.maps.design.darkColorScheme
import ch.sbb.maps.design.lightColorScheme

/**
 * Material 3 theme wrapper configured with SBB brand colors and typography.
 *
 * @param darkTheme force dark mode; null defaults to light.
 * @param dynamicColor enable Android 12+ dynamic colour (default false).
 * @param content composable content tree themed by this function.
 */
@Composable
public fun SBBMapTheme(
    darkTheme: Boolean? = null,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val darkMode = darkTheme ?: false
    val colorScheme =
        when {
            dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
                val context = LocalContext.current
                if (darkMode) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
            }

            darkMode -> {
                darkColorScheme
            }

            else -> {
                lightColorScheme
            }
        }
    MaterialTheme(
        colorScheme = colorScheme,
        typography = SBBTypography,
        content = content,
    )
}
