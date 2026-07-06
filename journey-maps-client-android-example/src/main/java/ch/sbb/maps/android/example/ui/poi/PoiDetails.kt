// SPDX-License-Identifier: MIT
// Copyright (c) 2024 Schweizerische Bundesbahnen AG (Swiss Federal Railways)

package ch.sbb.maps.android.example.ui.poi

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import org.maplibre.android.geometry.LatLng
import org.maplibre.geojson.Feature
import org.maplibre.geojson.Point

@Composable
fun PoiDetails(
    feature: Feature? = null,
    geometry: LatLng? = null,
) {
    val textColor = MaterialTheme.colorScheme.onPrimary
    val point = feature?.geometry() as? Point
    val latitude = point?.latitude()?.toString() ?: geometry?.latitude ?: "Unknown"
    val longitude = point?.longitude()?.toString() ?: geometry?.longitude ?: "Unknown"

    val name = feature?.getStringProperty("name") ?: "Unknown"
    val subCategory = feature?.getStringProperty("subCategory") ?: "Unknown"
    val category = feature?.getStringProperty("category") ?: "Unknown"
    val icon = feature?.getStringProperty("icon") ?: "Unknown"
    val sbbId = feature?.getStringProperty("sbbId") ?: "Unknown"

    Column {
        if (feature != null) Text(text = "Name: $name", color = textColor)
        if (feature != null) Text(text = "SubCategory: $subCategory", color = textColor)
        if (feature != null) Text(text = "Category: $category", color = textColor)
        if (feature != null) Text(text = "Icon: $icon", color = textColor)
        if (feature != null) Text(text = "SBB ID: $sbbId", color = textColor)
        Text(text = "Latitude: $latitude", color = textColor)
        Text(text = "Longitude: $longitude", color = textColor)
    }
}
