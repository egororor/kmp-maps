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
)

@Composable
internal fun MapWrapper(
    modifier: Modifier = Modifier,
    options: MapOptions,
    geoJsonLayers: List<GeoJsonLayer>,
) {
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
                    customControls = listOf(
                        WebMapControl(
                            id = "zoom-control",
                            position = WebControlPosition.RIGHT_CENTER,
                            html = """
                                <div style="margin-right: 10px; display: flex; flex-direction: column; gap: 8px;">
                                    <button onclick="kmpZoomIn()" style="background-color: #4285F4; color: white; border: none; border-radius: 8px; width: 44px; height: 44px; box-shadow: 0 2px 6px rgba(0,0,0,.3); cursor: pointer; font-size: 24px; font-weight: bold;">+</button>
                                    <button onclick="kmpZoomOut()" style="background-color: #4285F4; color: white; border: none; border-radius: 8px; width: 44px; height: 44px; box-shadow: 0 2px 6px rgba(0,0,0,.3); cursor: pointer; font-size: 24px; font-weight: bold;">-</button>
                                </div>
                            """.trimIndent(),
                        ),
                        WebMapControl(
                            id = "munich-button",
                            position = WebControlPosition.BOTTOM_LEFT,
                            html = """
                                <button onclick="kmpSetCenter(48.1351, 11.5820, 12)"
                                    style="margin-left: 10px; margin-bottom: 10px; background-color: #FF0000; color: white; border: none; border-radius: 50%; width: 60px; height: 60px; box-shadow: 0 2px 6px rgba(0,0,0,.3); cursor: pointer; font-size: 14px; font-weight: bold; display: flex; align-items: center; justify-content: center;">
                                    Munich
                                </button>
                            """.trimIndent(),
                        ),
                    ),
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
                geoJsonLayers = geoJsonLayers,
                customMarkerContent = customMarkerContent,
                webCustomMarkerContent = webCustomMarkerContent,
            )
    }
}
