// SPDX-License-Identifier: MIT
// Copyright (c) 2024 Schweizerische Bundesbahnen AG (Swiss Federal Railways)

package ch.sbb.maps.map

internal sealed class SBBMapStyle(
    private val style: String?,
) {
    internal object BRIGHT : SBBMapStyle("journey_maps_bright_v1")

    internal object DARK : SBBMapStyle("journey_maps_dark_v1")

    internal object SATELLITE : SBBMapStyle("journey_maps_aerial_v1")

    internal class CUSTOM(
        internal var customUrl: String,
    ) : SBBMapStyle(null)

    internal fun getStyleUrl(tilesApiKey: String): String =
        when (this) {
            is CUSTOM -> customUrl
            else -> "https://journey-maps-tiles.api.sbb.ch/styles/$style/style.json?api_key=$tilesApiKey"
        }
}
