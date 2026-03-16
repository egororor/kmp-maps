package com.swmansion.kmpmaps.sample

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.swmansion.kmpmaps.core.AndroidMapProperties
import com.swmansion.kmpmaps.core.AndroidUISettings
import com.swmansion.kmpmaps.core.CameraPosition
import com.swmansion.kmpmaps.core.Circle
import com.swmansion.kmpmaps.core.ClusterSettings
import com.swmansion.kmpmaps.core.Coordinates
import com.swmansion.kmpmaps.core.GeoJsonLayer
import com.swmansion.kmpmaps.core.Map as CoreMap
import com.swmansion.kmpmaps.core.MapProperties
import com.swmansion.kmpmaps.core.MapTheme
import com.swmansion.kmpmaps.core.MapType
import com.swmansion.kmpmaps.core.MapUISettings
import com.swmansion.kmpmaps.core.Marker
import com.swmansion.kmpmaps.core.Polygon
import com.swmansion.kmpmaps.core.Polyline
import com.swmansion.kmpmaps.core.WebControlPosition
import com.swmansion.kmpmaps.core.WebMapControl
import com.swmansion.kmpmaps.core.WebMapGesture
import com.swmansion.kmpmaps.core.WebMapProperties
import com.swmansion.kmpmaps.core.WebUISettings
import com.swmansion.kmpmaps.googlemaps.Map as GoogleMap

internal data class MapOptions(
    val mapType: MapType = MapType.NORMAL,
    val mapTheme: MapTheme = MapTheme.SYSTEM,
    val showUserLocation: Boolean = false,
    val cameraPosition: CameraPosition =
        CameraPosition(
            coordinates = Coordinates(latitude = 50.0619, longitude = 19.9373),
            zoom = 14f,
        ),
    val showAllComponents: Boolean = true,
    val useGoogleMapsMapView: Boolean = false,
    val showPointGeoJson: Boolean = false,
    val showPolygonGeoJson: Boolean = false,
    val showLineGeoJson: Boolean = false,
    val clusteringEnabled: Boolean = false,
    val onSettingsClick: (() -> Unit)? = null,
)

@Composable
internal fun MapWrapper(
    modifier: Modifier = Modifier,
    options: MapOptions,
    settingsExpanded: Boolean = false,
    geoJsonLayers: List<GeoJsonLayer>,
) {
    val settingsButtonHtml = """
        <button onclick="kmpCallNative('onSettingsClick', '{}')"
            style="margin-right: 10px; margin-top: 10px; background-color: white; color: #616161; border: none; border-radius: 50%; width: 40px; height: 40px; box-shadow: 0 1px 4px rgba(0,0,0,0.3); cursor: pointer; display: flex; align-items: center; justify-content: center;">
            <svg viewBox="0 0 24 24" width="24" height="24" fill="currentColor">
                <path d="M19.14,12.94c0.04-0.3,0.06-0.61,0.06-0.94c0-0.32-0.02-0.64-0.07-0.94l2.03-1.58c0.18-0.14,0.23-0.41,0.12-0.61 l-1.92-3.32c-0.12-0.22-0.37-0.29-0.59-0.22l-2.39,0.96c-0.5-0.38-1.03-0.7-1.62-0.94L14.4,2.81c-0.04-0.24-0.24-0.41-0.48-0.41 h-3.84c-0.24,0-0.43,0.17-0.47,0.41L9.25,5.35C8.66,5.59,8.12,5.92,7.63,6.29L5.24,5.33c-0.22-0.08-0.47,0-0.59,0.22L2.74,8.87 C2.62,9.08,2.66,9.34,2.86,9.48l2.03,1.58C4.84,11.36,4.81,11.69,4.81,12c0,0.31,0.02,0.65,0.07,0.94l-2.03,1.58 c-0.18,0.14-0.23,0.41-0.12,0.61l1.92,3.32c0.12,0.22,0.37,0.29,0.59,0.22l2.39-0.96c0.5,0.38,1.03,0.7,1.62,0.94l0.36,2.54 c0.05,0.24,0.24,0.41,0.48,0.41h3.84c0.24,0,0.44-0.17,0.47-0.41l0.36-2.54c0.59-0.24,1.13-0.56,1.62-0.94l2.39,0.96 c0.22,0.08,0.47,0,0.59-0.22l1.92-3.32c0.12-0.22,0.07-0.47-0.12-0.61L19.14,12.94z M12,15.6c-1.98,0-3.6-1.62-3.6-3.6 s1.62-3.6,3.6-3.6s3.6,1.62,3.6,3.6S13.98,15.6,12,15.6z"/>
            </svg>
        </button>
    """.trimIndent()

    Map(
        modifier = modifier,
        mapProvider =
            if (options.useGoogleMapsMapView) MapProvider.GOOGLE_MAPS else MapProvider.NATIVE,
        cameraPosition = options.cameraPosition,
        properties =
            MapProperties(
                mapType = options.mapType,
                mapTheme = options.mapTheme,
                isMyLocationEnabled = options.showUserLocation,
                isTrafficEnabled = true,
                isBuildingEnabled = true,
                androidMapProperties =
                    AndroidMapProperties(
                        isIndoorEnabled = true,
                        minZoomPreference = 3f,
                        maxZoomPreference = 21f,
                    ),
                webMapProperties = WebMapProperties(mapId = "ee04f651cb7da64943cba589"),
            ),
        uiSettings =
            MapUISettings(
                compassEnabled = true,
                myLocationButtonEnabled = options.showUserLocation,
                scaleBarEnabled = true,
                androidUISettings = AndroidUISettings(zoomControlsEnabled = false),
                webUISettings = WebUISettings(
                    zoomControl = false,
                    customJavaScript = """
                        function kmpSetCenter(lat, lng, zoom) {
                            if (!map) return;
                            map.setCenter({ lat, lng });
                            if (zoom !== undefined) {
                                map.setZoom(zoom);
                            }
                        }
                        function kmpZoomIn() {
                            if (map) map.setZoom(map.getZoom() + 1);
                        }
                        function kmpZoomOut() {
                            if (map) map.setZoom(map.getZoom() - 1);
                        }
                    """.trimIndent(),
                    customControls = buildList {
                        if (!settingsExpanded) {
                            add(
                                WebMapControl(
                                    id = "zoom-control",
                                    position = WebControlPosition.RIGHT_CENTER,
                                    html = """
                                        <div style="margin-right: 10px; display: flex; flex-direction: column; gap: 8px;">
                                            <button onclick="kmpZoomIn()" style="background-color: #4285F4; color: white; border: none; border-radius: 8px; width: 44px; height: 44px; box-shadow: 0 2px 6px rgba(0,0,0,.3); cursor: pointer; font-size: 24px; font-weight: bold;">+</button>
                                            <button onclick="kmpZoomOut()" style="background-color: #4285F4; color: white; border: none; border-radius: 8px; width: 44px; height: 44px; box-shadow: 0 2px 6px rgba(0,0,0,.3); cursor: pointer; font-size: 24px; font-weight: bold;">-</button>
                                        </div>
                                    """.trimIndent(),
                                )
                            )
                            add(
                                WebMapControl(
                                    id = "munich-button",
                                    position = WebControlPosition.BOTTOM_LEFT,
                                    html = """
                                        <button onclick="kmpSetCenter(48.1351, 11.5820, 12)"
                                            style="margin-left: 10px; margin-bottom: 10px; background-color: #FF0000; color: white; border: none; border-radius: 50%; width: 60px; height: 60px; box-shadow: 0 2px 6px rgba(0,0,0,.3); cursor: pointer; font-size: 14px; font-weight: bold; display: flex; align-items: center; justify-content: center;">
                                            Munich
                                        </button>
                                    """.trimIndent(),
                                )
                            )
                        }
                        add(
                            WebMapControl(
                                id = "settings-button",
                                position = WebControlPosition.TOP_RIGHT,
                                html = settingsButtonHtml,
                            )
                        )
                    },
                ),
            ),
        markers = if (options.showAllComponents) clusterMarkers else emptyList(),
        clusterSettings =
            ClusterSettings(
                enabled = options.clusteringEnabled,
                clusterContent = customClusterContent,
                onClusterClick = { cluster ->
                    println("Cluster clicked: ${cluster.size} markers at ${cluster.coordinates}")
                    false
                },
                webClusterContent = webClusterContent,
            ),
        circles = if (options.showAllComponents) getExampleCircles() else emptyList(),
        polygons = if (options.showAllComponents) getExamplePolygons() else emptyList(),
        polylines = if (options.showAllComponents) getExamplePolylines() else emptyList(),
        onCameraMove = { position -> println("Camera moved: $position") },
        onCircleClick = { println("Circle clicked: ${it.center}") },
        onPolygonClick = { println("Polygon clicked: ${it.coordinates}") },
        onPolylineClick = { println("Polyline clicked: ${it.coordinates}") },
        onPOIClick = { println("POI clicked: $it") },
        onMapLoaded = { println("Map loaded") },
        onMapLongClick = { println("Map long clicked: $it") },
        onMarkerClick = { marker ->
            println("Marker clicked: ${marker.title} ${marker.coordinates}")
        },
        onCustomEvent = { name, data ->
            println("Custom event received: name=$name, data=$data")
            if (name == "onSettingsClick" || name == "openSettings") {
                println("Settings click triggered, callback=${options.onSettingsClick}")
                options.onSettingsClick?.invoke()
            }
        },
        onMapClick = { coordinates -> println("Map clicked at: $coordinates") },
        geoJsonLayers = geoJsonLayers,
        customMarkerContent = customMarkerContent,
        webCustomMarkerContent = customWebMarkerContent,
    )
}

private enum class MapProvider {
    NATIVE,
    GOOGLE_MAPS,
}

@Composable
private fun Map(
    modifier: Modifier = Modifier,
    mapProvider: MapProvider,
    cameraPosition: CameraPosition? = null,
    properties: MapProperties = MapProperties(),
    uiSettings: MapUISettings = MapUISettings(),
    clusterSettings: ClusterSettings = ClusterSettings(),
    markers: List<Marker> = emptyList(),
    circles: List<Circle> = emptyList(),
    polygons: List<Polygon> = emptyList(),
    polylines: List<Polyline> = emptyList(),
    onCameraMove: ((CameraPosition) -> Unit)? = null,
    onMarkerClick: ((Marker) -> Unit)? = null,
    onMarkerDragEnd: ((Marker) -> Unit)? = null,
    onCircleClick: ((Circle) -> Unit)? = null,
    onPolygonClick: ((Polygon) -> Unit)? = null,
    onPolylineClick: ((Polyline) -> Unit)? = null,
    onMapClick: ((Coordinates) -> Unit)? = null,
    onMapLongClick: ((Coordinates) -> Unit)? = null,
    onPOIClick: ((Coordinates) -> Unit)? = null,
    onMapLoaded: (() -> Unit)? = null,
    onCustomEvent: ((name: String, params: String) -> Unit)? = null,
    geoJsonLayers: List<GeoJsonLayer> = emptyList(),
    customMarkerContent: Map<String, @Composable (Marker) -> Unit> = emptyMap(),
    webCustomMarkerContent: Map<String, (Marker) -> String> = emptyMap(),
) {
    when (mapProvider) {
        MapProvider.NATIVE ->
            CoreMap(
                modifier = modifier,
                cameraPosition = cameraPosition,
                properties = properties,
                uiSettings = uiSettings,
                clusterSettings = clusterSettings,
                markers = markers,
                circles = circles,
                polygons = polygons,
                polylines = polylines,
                onCameraMove = onCameraMove,
                onMarkerClick = onMarkerClick,
                onMarkerDragEnd = onMarkerDragEnd,
                onCircleClick = onCircleClick,
                onPolygonClick = onPolygonClick,
                onPolylineClick = onPolylineClick,
                onMapClick = onMapClick,
                onMapLongClick = onMapLongClick,
                onPOIClick = onPOIClick,
                onMapLoaded = onMapLoaded,
                onCustomEvent = onCustomEvent,
                geoJsonLayers = geoJsonLayers,
                customMarkerContent = customMarkerContent,
                webCustomMarkerContent = webCustomMarkerContent,
            )
        MapProvider.GOOGLE_MAPS ->
            GoogleMap(
                modifier = modifier,
                cameraPosition = cameraPosition,
                properties = properties,
                uiSettings = uiSettings,
                clusterSettings = clusterSettings,
                markers = markers,
                circles = circles,
                polygons = polygons,
                polylines = polylines,
                onCameraMove = onCameraMove,
                onMarkerClick = onMarkerClick,
                onMarkerDragEnd = onMarkerDragEnd,
                onCircleClick = onCircleClick,
                onPolygonClick = onPolygonClick,
                onPolylineClick = onPolylineClick,
                onMapClick = onMapClick,
                onMapLongClick = onMapLongClick,
                onPOIClick = onPOIClick,
                onMapLoaded = onMapLoaded,
                onCustomEvent = onCustomEvent,
                geoJsonLayers = geoJsonLayers,
                customMarkerContent = customMarkerContent,
                webCustomMarkerContent = webCustomMarkerContent,
            )
    }
}
