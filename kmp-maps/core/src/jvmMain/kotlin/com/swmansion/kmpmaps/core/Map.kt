package com.swmansion.kmpmaps.core

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.multiplatform.webview.jsbridge.IJsMessageHandler
import com.multiplatform.webview.jsbridge.JsMessage
import com.multiplatform.webview.jsbridge.WebViewJsBridge
import com.multiplatform.webview.web.LoadingState
import com.multiplatform.webview.web.WebView
import com.multiplatform.webview.web.WebViewNavigator
import com.multiplatform.webview.web.rememberWebViewNavigator
import com.multiplatform.webview.web.rememberWebViewStateWithHTMLData
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/** JVM implementation of the Map composable using Google Maps with JCEF (via compose-webview-multiplatform). */
@Composable
public actual fun Map(
    modifier: Modifier,
    cameraPosition: CameraPosition?,
    properties: MapProperties,
    uiSettings: MapUISettings,
    clusterSettings: ClusterSettings,
    markers: List<Marker>,
    circles: List<Circle>,
    polygons: List<Polygon>,
    polylines: List<Polyline>,
    onCameraMove: ((CameraPosition) -> Unit)?,
    onMarkerClick: ((Marker) -> Unit)?,
    onMarkerDragEnd: ((Marker) -> Unit)?,
    onCircleClick: ((Circle) -> Unit)?,
    onPolygonClick: ((Polygon) -> Unit)?,
    onPolylineClick: ((Polyline) -> Unit)?,
    onMapClick: ((Coordinates) -> Unit)?,
    onMapLongClick: ((Coordinates) -> Unit)?,
    onPOIClick: ((Coordinates) -> Unit)?,
    onMapLoaded: (() -> Unit)?,
    onCustomEvent: ((name: String, params: String) -> Unit)?,
    geoJsonLayers: List<GeoJsonLayer>,
    customMarkerContent: Map<String, @Composable (Marker) -> Unit>,
    webCustomMarkerContent: Map<String, (Marker) -> String>,
) {
    var htmlContent by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        val apiKey = MapConfiguration.googleMapsApiKey
        htmlContent = loadHTMLContent(apiKey, cameraPosition, properties)
    }

    val currentOnCameraMove by rememberUpdatedState(onCameraMove)
    val currentOnMarkerClick by rememberUpdatedState(onMarkerClick)
    val currentOnCircleClick by rememberUpdatedState(onCircleClick)
    val currentOnPolygonClick by rememberUpdatedState(onPolygonClick)
    val currentOnPolylineClick by rememberUpdatedState(onPolylineClick)
    val currentOnMapClick by rememberUpdatedState(onMapClick)
    val currentOnPOIClick by rememberUpdatedState(onPOIClick)
    val currentOnMapLoaded by rememberUpdatedState(onMapLoaded)
    val currentOnCustomEvent by rememberUpdatedState(onCustomEvent)

    if (htmlContent != null) {
        val state = rememberWebViewStateWithHTMLData(data = htmlContent!!)
        val loadingState = state.loadingState
        state.webSettings.isJavaScriptEnabled = true

        val navigator = rememberWebViewNavigator()
        val jsBridge = com.multiplatform.webview.jsbridge.rememberWebViewJsBridge(navigator)

        LaunchedEffect(jsBridge) {
            registerMapEvents(
                jsBridge = jsBridge,
                markers = { markers },
                clusterSettings = clusterSettings,
                onCameraMove = { currentOnCameraMove?.invoke(it) },
                onMarkerClick = { currentOnMarkerClick?.invoke(it) },
                onCircleClick = { id -> circles.find { it.getId() == id }?.let { currentOnCircleClick?.invoke(it) } },
                onPolygonClick = { id -> polygons.find { it.getId() == id }?.let { currentOnPolygonClick?.invoke(it) } },
                onPolylineClick = { id -> polylines.find { it.getId() == id }?.let { currentOnPolylineClick?.invoke(it) } },
                onMapClick = { coords -> currentOnMapClick?.invoke(coords) },
                onPOIClick = { coords -> currentOnPOIClick?.invoke(coords) },
                onMapLoaded = { currentOnMapLoaded?.invoke() },
                onCustomEvent = { name, params -> currentOnCustomEvent?.invoke(name, params) },
            )
        }

        LaunchedEffect(markers, webCustomMarkerContent, clusterSettings.enabled, loadingState) {
            if (loadingState is LoadingState.Finished) {
                val json = markers.toJson(webCustomMarkerContent).toString()
                val hasCustomCluster = clusterSettings.webClusterContent != null
                navigator.evaluateJavaScript(
                    "updateMarkers($json, ${clusterSettings.enabled}, $hasCustomCluster)"
                )
            }
        }

        LaunchedEffect(circles, loadingState) {
            if (loadingState is LoadingState.Finished) {
                val json = circles.map(Circle::toJson).toJsonString()
                navigator.evaluateJavaScript("updateCircles($json)")
            }
        }

        LaunchedEffect(polygons, loadingState) {
            if (loadingState is LoadingState.Finished) {
                val json = polygons.map(Polygon::toJson).toJsonString()
                navigator.evaluateJavaScript("updatePolygons($json)")
            }
        }

        LaunchedEffect(polylines, loadingState) {
            if (loadingState is LoadingState.Finished) {
                val json = polylines.map(Polyline::toJson).toJsonString()
                navigator.evaluateJavaScript("updatePolylines($json)")
            }
        }

        LaunchedEffect(properties, loadingState) {
            if (loadingState is LoadingState.Finished) {
                val json = properties.toJson().toString()
                navigator.evaluateJavaScript("updateMapProperties($json)")
            }
        }

        LaunchedEffect(uiSettings, loadingState) {
            if (loadingState is LoadingState.Finished) {
                val json = uiSettings.toJson().toString()
                navigator.evaluateJavaScript("updateMapUISettings($json)")
            }
        }

        LaunchedEffect(geoJsonLayers, clusterSettings.enabled, loadingState) {
            if (loadingState is LoadingState.Finished) {
                val json = geoJsonLayers.map(GeoJsonLayer::toJson).toJsonString()
                navigator.evaluateJavaScript(
                    "updateGeoJsonLayers($json, ${clusterSettings.enabled})"
                )
            }
        }

        WebView(
            modifier = modifier,
            state = state,
            navigator = navigator,
            webViewJsBridge = jsBridge,
            onCreated = { _ -> },
        )
    } else {
        Box(modifier = modifier, contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    }
}

/**
 * Registers various map event handlers to the JavaScript bridge.
 */
internal fun registerMapEvents(
    jsBridge: WebViewJsBridge,
    markers: () -> List<Marker>,
    clusterSettings: ClusterSettings,
    onCameraMove: ((CameraPosition) -> Unit)?,
    onMarkerClick: ((Marker) -> Unit)?,
    onCircleClick: ((String) -> Unit)?,
    onPolygonClick: ((String) -> Unit)?,
    onPolylineClick: ((String) -> Unit)?,
    onMapClick: ((Coordinates) -> Unit)?,
    onPOIClick: ((Coordinates) -> Unit)?,
    onMapLoaded: (() -> Unit)?,
    onCustomEvent: ((name: String, params: String) -> Unit)?,
) {
    jsBridge.registerHandler("onCameraMove") { params, _ ->
        val position = Json.decodeFromString<CameraPosition>(params)
        onCameraMove?.invoke(position)
    }

    jsBridge.registerHandler("onMarkerClick") { params, _ ->
        val markerId = params
        val clickedMarker = markers().find { marker -> marker.getId() == markerId }
        clickedMarker?.let { onMarkerClick?.invoke(it) }
    }

    jsBridge.registerHandler("onMapClick") { params, _ ->
        val coords = Json.decodeFromString<Coordinates>(params)
        onMapClick?.invoke(coords)
    }

    jsBridge.registerHandler("onPOIClick") { params, _ ->
        val coords = Json.decodeFromString<Coordinates>(params)
        onPOIClick?.invoke(coords)
    }

    jsBridge.registerHandler("onMapLoaded") { _, _ -> onMapLoaded?.invoke() }

    jsBridge.registerHandler("kmpCallNative") { params, _ ->
        try {
            val json = Json.parseToJsonElement(params).jsonObject
            val method = json["method"]?.jsonPrimitive?.content ?: ""
            val data = json["data"]?.jsonPrimitive?.content ?: ""
            onCustomEvent?.invoke(method, data)
        } catch (e: Exception) {
            onCustomEvent?.invoke(params, "")
        }
    }

    jsBridge.registerHandler("onCircleClick") { id, _ ->
        onCircleClick?.invoke(id)
    }

    jsBridge.registerHandler("onPolygonClick") { id, _ ->
        onPolygonClick?.invoke(id)
    }

    jsBridge.registerHandler("onPolylineClick") { id, _ ->
        onPolylineClick?.invoke(id)
    }

    jsBridge.registerHandler("onClusterClick") { params, _ ->
        val cluster = Json.decodeFromString<Cluster>(params)
        clusterSettings.onClusterClick?.invoke(cluster)
    }

    jsBridge.registerHandler("renderCluster") { params, navigator ->
        val clusterJS = Json.decodeFromString<ClusterJS>(params)
        val cluster = clusterJS.toCluster()
        val html = clusterSettings.webClusterContent?.invoke(cluster)

        if (html != null) {
            val formattedHtml = html.trimIndent()
            val escapedHtmlJson = Json.encodeToString(formattedHtml)
            navigator?.evaluateJavaScript("applyClusterHtml(${clusterJS.id}, '$escapedHtmlJson')")
        }
    }
}

private fun WebViewJsBridge.registerHandler(
    methodName: String,
    handler: (String, WebViewNavigator?) -> Unit,
) {
    register(
        object : IJsMessageHandler {
            override fun methodName() = methodName

            override fun handle(
                message: JsMessage,
                navigator: WebViewNavigator?,
                callback: (String) -> Unit,
            ) {
                try {
                    handler(message.params, navigator)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    )
}

/**
 * Internal Data Transfer Object (DTO) used for deserializing cluster data received from the
 * JavaScript Google Maps bridge.
 */
@Serializable
private data class ClusterJS(
    val id: String,
    val coordinates: Coordinates,
    val size: Int,
    val items: List<Marker>,
)

/**
 * Converts a [ClusterJS] object to a [Cluster] object.
 */
private fun ClusterJS.toCluster() = Cluster(coordinates = coordinates, size = size, items = items)
