// SPDX-License-Identifier: MIT
// Copyright (c) 2024 Schweizerische Bundesbahnen AG (Swiss Federal Railways)

package ch.sbb.maps.android.example

import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.tooling.preview.Preview
import ch.sbb.maps.theme.SBBMapTheme
import ch.sbb.maps.theme.SBBMapThemeManager

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            val systemMode = isSystemInDarkTheme()
            SBBMapThemeManager.init(systemDarkMode = systemMode)
            MyContent()
        }
    }

    @Composable
    fun MyContent() {
        val isDarkTheme =
            remember {
                mutableStateOf(SBBMapThemeManager.isDarkMode.value)
            }

        LaunchedEffect(SBBMapThemeManager.isDarkMode) {
            SBBMapThemeManager.isDarkMode.collect { isDark ->
                isDarkTheme.value = isDark
            }
        }

        SBBMapTheme(darkTheme = isDarkTheme.value) {
            SBBMapDemoApp()
        }
    }

    @Preview(showBackground = true)
    @Composable
    fun MyContentLightPreview() {
        SBBMapTheme(darkTheme = false) {
            SBBMapDemoApp()
        }
    }

    @Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
    @Composable
    fun MyContentDarkPreview() {
        SBBMapTheme(darkTheme = true) {
            SBBMapDemoApp()
        }
    }
}
