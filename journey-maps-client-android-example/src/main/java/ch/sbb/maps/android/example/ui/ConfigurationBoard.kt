// SPDX-License-Identifier: MIT
// Copyright (c) 2024 Schweizerische Bundesbahnen AG (Swiss Federal Railways)

package ch.sbb.maps.android.example.ui

import android.content.res.Configuration
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import ch.sbb.maps.design.SBBCheckbox
import ch.sbb.maps.theme.SBBMapTheme

@Composable
fun ConfigurationBoard(
    userLocationEnabled: MutableState<Boolean> = remember { mutableStateOf(true) },
    mapStyleSwitchEnabled: MutableState<Boolean> = remember { mutableStateOf(true) },
    floorSwitchEnabled: MutableState<Boolean> = remember { mutableStateOf(true) },
) {
    Column(Modifier.fillMaxWidth()) {
        SBBCheckbox(
            checked = userLocationEnabled.value,
            onCheckedChange = {
                if (it != null) {
                    userLocationEnabled.value = it
                }
            },
            label = "User location",
        )
        SBBCheckbox(
            checked = mapStyleSwitchEnabled.value,
            onCheckedChange = {
                if (it != null) {
                    mapStyleSwitchEnabled.value = it
                }
            },
            label = "Basemap switcher",
        )
        SBBCheckbox(
            checked = floorSwitchEnabled.value,
            onCheckedChange = {
                if (it != null) {
                    floorSwitchEnabled.value = it
                }
            },
            label = "Floor switcher",
        )
    }
}

@Preview(showBackground = true, name = "Light Theme")
@Composable
fun ConfigurationBoardLight() {
    SBBMapTheme(darkTheme = false) {
        ConfigurationBoard()
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Dark Theme")
@Composable
fun ConfigurationBoardDark() {
    SBBMapTheme(darkTheme = true) {
        ConfigurationBoard()
    }
}
