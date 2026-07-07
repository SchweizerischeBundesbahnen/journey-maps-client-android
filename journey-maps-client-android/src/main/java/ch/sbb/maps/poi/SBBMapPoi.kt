// SPDX-License-Identifier: MIT
// Copyright (c) 2024 Schweizerische Bundesbahnen AG (Swiss Federal Railways)

package ch.sbb.maps.poi

import androidx.compose.runtime.MutableState
import androidx.lifecycle.MutableLiveData
import ch.sbb.maps.annotation.SBBGeoJson
import org.maplibre.geojson.Feature

/**
 * POI configuration for [ch.sbb.maps.map.SBBMapView].
 *
 * @property enabled whether POI layers and click interaction are active.
 * @property selectedPoi state holder for the currently selected POI [Feature].
 * @property subcategories observable list of [SBBPoiCategoryType] values to filter visible POIs.
 * @property geoJson observable [SBBGeoJson] polygon overlay to render on the map.
 */
public data class SBBMapPoi(
    val enabled: Boolean = false,
    val selectedPoi: MutableState<Feature?>? = null,
    val subcategories: MutableLiveData<List<String>> = MutableLiveData(),
    val geoJson: MutableLiveData<SBBGeoJson> = MutableLiveData(SBBGeoJson.EMPTY),
)
