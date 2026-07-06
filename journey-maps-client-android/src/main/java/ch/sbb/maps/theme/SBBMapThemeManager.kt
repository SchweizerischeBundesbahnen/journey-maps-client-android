// SPDX-License-Identifier: MIT
// Copyright (c) 2024 Schweizerische Bundesbahnen AG (Swiss Federal Railways)

package ch.sbb.maps.theme

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Singleton managing the map's light/dark theme state.
 *
 * Observe [isDarkMode] to react to theme changes. Call [init] once at app startup
 * with the system dark mode value, then use [toggleDarkMode] for user driven switches.
 *
 */
public object SBBMapThemeManager {
    private val _isDarkMode = MutableStateFlow(false) // MutableStateFlow instead of LiveData
    public val isDarkMode: StateFlow<Boolean> = _isDarkMode

    public fun init(systemDarkMode: Boolean) {
        _isDarkMode.value = systemDarkMode
    }

    public fun toggleDarkMode() {
        _isDarkMode.value = !_isDarkMode.value
    }
}
