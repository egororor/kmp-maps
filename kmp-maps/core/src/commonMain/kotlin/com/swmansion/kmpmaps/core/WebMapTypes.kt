package com.swmansion.kmpmaps.core

import androidx.compose.ui.graphics.Color

public data class WebMapProperties(
    val mapId: String? = null,
    val gestureHandling: WebMapGesture = WebMapGesture.AUTO,
    val disableDoubleClickZoom: Boolean = false,
    val keyboardShortcuts: Boolean = true,
    val minZoom: Float? = null,
    val maxZoom: Float? = null,
    val clickableIcons: Boolean = true,
    val restriction: WebMapRestriction? = null,
    val styles: GoogleMapsMapStyleOptions? = null,
    val backgroundColor: Color? = null,
)

public data class WebUISettings(
    val zoomControl: Boolean = true,
    val mapTypeControl: Boolean = false,
    val streetViewControl: Boolean = false,
    val rotateControl: Boolean = false,
    val disableDefaultUI: Boolean = false,
    val zoomControlPosition: WebControlPosition? = WebControlPosition.LEFT_TOP,
    val mapTypeControlPosition: WebControlPosition? = null,
    val streetViewControlPosition: WebControlPosition? = null,
    val rotateControlPosition: WebControlPosition? = null,
    /**
     * List of custom controls to render over the map.
     * Each control can use built-in JS functions:
     * - `kmpZoomIn()` / `kmpZoomOut()` - zoom controls
     * - `kmpSetCenter(lat, lng, zoom)` - move camera to coordinates (zoom optional)
     * - `kmpCallNative(eventName, jsonData)` - send custom events to Kotlin
     * - Any custom functions defined in [customJavaScript]
     */
    val customControls: List<WebMapControl> = emptyList(),
    /**
     * Custom JavaScript code to inject into the map page.
     * Define global functions that your custom controls can call.
     * You have access to the `map` variable (Google Maps instance).
     *
     * Example:
     * ```kotlin
     * customJavaScript = """
     *     function goToMunich() {
     *         map.setCenter({ lat: 48.1351, lng: 11.5820 });
     *         map.setZoom(12);
     *     }
     * """
     * ```
     */
    val customJavaScript: String? = null,
)

/**
 * Represents a custom HTML control to be rendered over the map.
 *
 * @property id Unique identifier for the control. Used to track and update the control.
 * @property html HTML content to render. Can use built-in JS functions like:
 *                - `onclick="kmpZoomIn()"` / `onclick="kmpZoomOut()"`
 *                - `onclick="kmpSetCenter(48.1351, 11.5820, 12)"`
 *                - `onclick="kmpCallNative('myEvent', '{\"key\": \"value\"}')"`
 * @property position Position on the map where the control should be placed.
 */
public data class WebMapControl(
    val id: String,
    val html: String,
    val position: WebControlPosition,
)

public data class WebMapRestriction(
    val north: Double,
    val south: Double,
    val east: Double,
    val west: Double,
    val strictBounds: Boolean = false,
)

public enum class WebMapGesture {
    COOPERATIVE,
    AUTO,
    GREEDY,
    NONE,
}

public enum class WebControlPosition {
    TOP_LEFT,
    TOP_CENTER,
    TOP_RIGHT,
    LEFT_TOP,
    LEFT_CENTER,
    LEFT_BOTTOM,
    RIGHT_TOP,
    RIGHT_CENTER,
    RIGHT_BOTTOM,
    BOTTOM_LEFT,
    BOTTOM_CENTER,
    BOTTOM_RIGHT,
}
