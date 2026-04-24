package com.swmansion.kmpmaps.core

import com.swmansion.kmpmaps.core.generated.resources.Res
import org.jetbrains.compose.resources.ExperimentalResourceApi

/**
 * Injects configuration data into the HTML and JavaScript templates for the WebView.
 *
 * This function performs template substitution, replacing placeholders with the provided API key,
 * initial camera coordinates, and map properties.
 *
 * @param apiKey The Google Maps API key to be used in the JS SDK script tag.
 * @param cameraPosition The camera position to be set in the map.
 * @param properties The configuration properties for the map.
 * @return A complete HTML string with embedded JavaScript, ready to be loaded into a WebView.
 */
@OptIn(ExperimentalResourceApi::class)
internal suspend fun loadHTMLContent(
    apiKey: String,
    cameraPosition: CameraPosition?,
    properties: MapProperties?,
): String {
    val html = readResource("files/web/google_map.html")
    val bounds = cameraPosition?.bounds
    val fitBoundsCall =
        if (bounds != null) {
            "map.fitBounds(new google.maps.LatLngBounds(" +
                "{lat: ${bounds.southwest.latitude}, lng: ${bounds.southwest.longitude}}, " +
                "{lat: ${bounds.northeast.latitude}, lng: ${bounds.northeast.longitude}}));"
        } else {
            ""
        }
    val js =
        readResource("files/web/google_map.js")
            .replace("{{INITIAL_MAP_ID}}", properties?.webMapProperties?.mapId ?: "DEMO_MAP_ID")
            .replace("{{INITIAL_COLOR_SCHEME}}", properties?.mapTheme?.name ?: MapTheme.SYSTEM.name)
            .replace("{{INITIAL_LAT}}", (cameraPosition?.coordinates?.latitude ?: 0f).toString())
            .replace("{{INITIAL_LNG}}", (cameraPosition?.coordinates?.longitude ?: 0f).toString())
            .replace("{{INITIAL_ZOOM}}", (cameraPosition?.zoom ?: 0f).toString())
            .replace("{{FIT_BOUNDS_CALL}}", fitBoundsCall)

    return html.replace("{{API_KEY}}", apiKey).replace("{{LOCAL_JS_CONTENT}}", js)
}

/**
 * Reads a text resource using Compose Resources.
 *
 * @param path The path to the resource relative to composeResources (e.g., "files/web/google_map.html").
 * @return The content of the resource as a string.
 */

@OptIn(ExperimentalResourceApi::class)
private suspend fun readResource(path: String): String =
    Res.readBytes(path).decodeToString()
