# Migration Guide: 1.x.x → 2.0.0

## Overview

Version 2.0.0 is the first open-source release. Internal infrastructure has been removed, the public API surface is reduced, and the main composable signature is simplified.

## Build configuration

The SDK is now available on Maven Central. Replace your previous Artifactory dependency:

```gradle
// Before (1.x.x) : requires SBB internal Artifactory access
implementation 'ch.sbb.rokas.android:mapsdk:<VERSION>'

// After (2.0.0) : public Maven Central
implementation 'ch.sbb.maps:android-sdk:2.0.0'
```

In your project's settings.gradle, ensure you have:

```gradle
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
}
```

No Artifactory credentials, proxy settings, or VPN required.

## License

All source files now include an SPDX header and the project ships a top level `LICENSE` (MIT).

## Package restructure

The SDK has been reorganized from layer-based (`core/`, `ui/`) to package-by-feature:

| Old package | New package | Contents |
|---|---|---|
| `ch.sbb.maps.core` | `ch.sbb.maps.map` | `SBBMap`, `SBBMapStyle`, `SBBMapUiSettings` |
| `ch.sbb.maps.core` | `ch.sbb.maps.poi` | `SBBMapPoi`, `SBBPoiCategoryType` |
| `ch.sbb.maps.core` | `ch.sbb.maps.annotation` | `SBBMapAnnotations`, `SBBLine`, `SBBCircle`, `SBBFloorConnector`, `SBBGeoJson` |
| `ch.sbb.maps.ui` | `ch.sbb.maps.map` | `SBBMapView`, `SBBMapCallbacks` |
| `ch.sbb.maps.ui` | `ch.sbb.maps.controls` | `SBBMapControls`, `SBBMapControlsAlignment` |
| `ch.sbb.maps.ui` | `ch.sbb.maps.floor` | `SBBMapFloorSwitch` |
| `ch.sbb.maps.ui` | `ch.sbb.maps.poi` | `SBBPoiSelection` |

Update your imports accordingly.

## Class renames

| Old name | New name |
|---|---|
| `SBBMapControllerConfig` | `SBBMapControls` |
| `SBBMapPoiConfig` | `SBBMapPoi` |
| `SBBMapControllerOverlayAlignment` | `SBBMapControlsAlignment` |

## `SBBMapView` API refactoring

The 19 parameter signature has been consolidated into config objects.

### Parameter mapping

| 1.x.x parameter | 2.0.0 location |
|---|---|
| `tilesApiKey` | unchanged |
| `modifier` | unchanged |
| `uiSettings` | unchanged |
| `centerTo` | unchanged |
| `zoomLevel` | unchanged |
| `cameraPosition` | unchanged |
| `userLocationEnabled` | `controls.userLocationEnabled` |
| `mapStyleSwitchEnabled` | `controls.mapStyleSwitchEnabled` |
| `floorSwitchEnabled` | `controls.floorSwitchEnabled` |
| `compassEnabled` | `controls.compassEnabled` |
| `controllerOverlayAlignment` | `controls.alignment` |
| `controllerOverlayPadding` | `controls.padding` |
| `poiEnabled` | `poi.enabled` |
| `selectedPoi` | `poi.selectedPoi` |
| `poiSubcategories` | `poi.subcategories` |
| `geoJson` | `poi.geoJson` |
| `onMapClick` | `callbacks.onMapClick` |
| `onPoiClick` | `callbacks.onPoiClick` |
| `onMapLongClick` | `callbacks.onMapLongClick` |
| `onCameraIdle` | `callbacks.onCameraIdle` |
| `onMoveBegin` | `callbacks.onMoveBegin` |

### Before (1.x.x)

```kotlin
SBBMapView(
    tilesApiKey = apiKey,
    userLocationEnabled = false,
    floorSwitchEnabled = false,
    poiEnabled = true,
    selectedPoi = selectedPoi,
    poiSubcategories = subcategories,
    onPoiClick = { feature -> handlePoi(feature) },
    onCameraIdle = { resetSelection() },
)
```

### After (2.0.0)

```kotlin
SBBMapView(
    tilesApiKey = apiKey,
    controls = SBBMapControls(
        userLocationEnabled = false,
        floorSwitchEnabled = false,
    ),
    poi = SBBMapPoi(
        enabled = true,
        selectedPoi = selectedPoi,
        subcategories = subcategories,
    ),
    callbacks = SBBMapCallbacks(
        onPoiClick = { feature -> handlePoi(feature) },
        onCameraIdle = { resetSelection() },
    ),
)
```

### Config classes

| Class | Purpose | Defaults |
|---|---|---|
| `SBBMapControls` | Overlay button visibility, alignment, padding | All enabled, `TopEnd`, no padding |
| `SBBMapPoi` | POI toggle, selected state, subcategories, GeoJSON | Disabled, empty |
| `SBBMapCallbacks` | Map interaction event callbacks | All `null` |

All parameters have defaults, so you only specify what differs.

## Visibility changes

Made `internal` (no longer accessible to SDK consumers):

| Symbol | Reason |
|---|---|
| `SBBMapFloatingActionButton` | Internal overlay detail |
| `SBBMapFloorSwitch` | Internal floor switch UI |
| `SBBMapFloorSwitchItemType` | Internal model |
| `SBBMapConstants` | Replaced by module private constants |

If you referenced any of these directly, use `SBBMapView` with the appropriate config instead.

## Style URL

`SBBMapStyle` and `getStyleUrl` are now internal. If you need the tile style URL directly (e.g. for a standalone MapLibre instance), construct it manually:

```
https://journey-maps-tiles.api.sbb.ch/styles/{STYLE}/style.json?api_key={YOUR_API_KEY}
```

Available styles:
- `journey_maps_bright_v1`
- `journey_maps_dark_v1`
- `journey_maps_aerial_v1`

When using `SBBMapView`, style switching is handled automatically via the controls overlay.

## Other changes

- `SBBMapView` now throws a descriptive error instead of a bare `IllegalStateException`.
- Timber replaces `android.util.Log` for internal logging.
- Package name for MapLibre remains `org.maplibre.android.*` (unchanged since 1.0.6).
- KDoc added to all public symbols.
