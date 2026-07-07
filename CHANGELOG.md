# Changelog

All notable changes to this project will be documented in this file.

## 2.0.0

Open-source release under the MIT license. See [MIGRATION.md](MIGRATION.md) for all breaking changes and migration instructions.

## 1.0.10

### Changes
- Added `SBBMapUiSettings` attributes so clients can configure the map's UI behavior (compass, attribution visibility, and gesture controls).
- Displayed POI areas with GeoJSON provided as a `FeatureCollection` (MultiPolygon) with a category string via the input parameter `poiAreas`.

## 1.0.9

### Bugfixes
- Fixed compass FAB size so a sliver isn't briefly visible when the compass button disappears.

### Changes
- Migrated to the new map style and added a `parking_place` POI layer to the default layers.
- The `Location` and `MapStyle` buttons can now be positioned more flexibly to better fit design requirements.

## 1.0.8

### Bugfixes
- When the user's device GPS has been deactivated, the SDK also deactivates `enablingMapLocation`.

### Changes
- The user's current location is automatically activated when the map is opened.
- The map accepts and triggers an `onMoveBegin` callback when the user starts interacting with the map (based on MapLibre Gesture Detector).

## 1.0.7

### Changes
- Updated Android Gradle Plugin and other dependencies.
- Updated the MapLibre library.
- Migrated the UI to support edge-to-edge layouts.

### Bugfixes
- Ensured that all POIs are fully rendered on the map before performing operations like querying rendered features by waiting for the map to become idle. This improves the reliability of `queryRenderedFeatures`.
- Made map initialization asynchronous using `CompletableDeferred`.

## 1.0.6

### Changes
- Added a search POI function. With coordinates, clients can retrieve POI information.

### Bugfixes
- Fixed an issue where the input value of POI subcategory was not updating on `PoiMapView`. Updated logic in `SBBMap` and the `SBBMapPoi` controller to include subcategories as parameters in the `setPoiResourcesVisible` method.

### Technical Maintenance
- Updated Android Gradle Plugin and other dependencies. Note: with a newer version of MapLibre, the package name changed from `com.mapbox.mapboxsdk.*` to `org.maplibre.android.*`.
- Changed some dependencies from `implementation` to `api` to allow client access.

## 1.0.5

### First Release
- Initial release including demo and library.