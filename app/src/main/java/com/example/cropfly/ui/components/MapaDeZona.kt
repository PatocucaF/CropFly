package com.example.cropfly.ui.components

import android.view.MotionEvent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.example.cropfly.model.Zona
import java.io.File
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.CopyrightOverlay
import org.osmdroid.views.overlay.Marker

/** Mapa de OpenStreetMap que muestra la zona referencial elegida con un marcador. */
@Composable
fun MapaDeZona(zona: Zona?, modifier: Modifier = Modifier) {
    val contexto = LocalContext.current

    val mapa = remember {
        // La caché va en la carpeta interna de la app, así no hace falta pedir permiso de almacenamiento.
        Configuration.getInstance().apply {
            userAgentValue = "CropFly/1.0 (proyecto academico DSY1105)"
            osmdroidBasePath = File(contexto.cacheDir, "osmdroid")
            osmdroidTileCache = File(contexto.cacheDir, "osmdroid/tiles")
        }
        MapView(contexto).apply {
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
            zoomController.setVisibility(CustomZoomButtonsController.Visibility.NEVER)
            controller.setZoom(7.0)
            overlays.add(CopyrightOverlay(contexto))
            // Evita que la pantalla se desplace cuando el usuario arrastra el mapa.
            setOnTouchListener { vista, evento ->
                val tocando = evento.action != MotionEvent.ACTION_UP && evento.action != MotionEvent.ACTION_CANCEL
                vista.parent?.requestDisallowInterceptTouchEvent(tocando)
                false
            }
        }
    }

    DisposableEffect(mapa) {
        mapa.onResume()
        onDispose {
            mapa.onPause()
            mapa.onDetach()
        }
    }

    AndroidView(
        modifier = modifier,
        factory = { mapa },
        update = { vista ->
            vista.overlays.removeAll { it is Marker }
            if (zona != null) {
                val punto = GeoPoint(zona.latitud, zona.longitud)
                val marcador = Marker(vista).apply {
                    position = punto
                    title = "Zona ${zona.nombre} (referencial, ficticia)"
                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                }
                vista.overlays.add(marcador)
                vista.controller.setCenter(punto)
            }
            vista.invalidate()
        }
    )
}