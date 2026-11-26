// SPDX-License-Identifier: MIT
// Copyright (c) 2024 Schweizerische Bundesbahnen AG (Swiss Federal Railways)

package ch.sbb.maps.map

internal sealed class SBBMapStyle(
    private val style: String?,
) {
    internal object BRIGHT : SBBMapStyle("sbbmaps_bright")

    internal object DARK : SBBMapStyle("sbbmaps_dark")

    internal object SATELLITE : SBBMapStyle("sbbmaps_aerial")

    internal class CUSTOM(
        internal var customUrl: String,
    ) : SBBMapStyle(null)

    internal fun getStyleUrl(tilesApiKey: String): String =
        when (this) {
            is CUSTOM -> customUrl
            else -> "https://journey-maps-tiles.api.sbb.ch/styles/$style/style.json?api_key=$tilesApiKey"
        }
}
