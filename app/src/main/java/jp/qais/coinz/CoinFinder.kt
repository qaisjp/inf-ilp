package jp.qais.coinz

import android.annotation.SuppressLint
import android.graphics.Color
import android.location.Location
import android.support.design.widget.Snackbar
import android.view.View
import android.widget.Toast
import com.mapbox.android.core.location.LocationEngine
import com.mapbox.android.core.location.LocationEngineListener
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.annotations.Polygon
import com.mapbox.mapboxsdk.annotations.PolygonOptions
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.geometry.LatLngBounds
import kotlinx.android.synthetic.main.content_map.*
import kotlinx.android.synthetic.main.fragment_play.*
import timber.log.Timber

class CoinFinder(val fragment: PlayFragment) : LocationEngineListener {
    lateinit var locationEngine: LocationEngine

    companion object {
        private val topLeft = LatLng(55.946233, -3.192473)
        private val bottomRight = LatLng(55.942617, -3.184319)
        private val topRight = LatLng(topLeft.latitude, bottomRight.longitude)
        private val bottomLeft = LatLng(bottomRight.latitude, topLeft.longitude)

        private val farTopLeft = LatLng(85.0, -180.0)
        private val farBottomRight = LatLng(-85.0, 180.0)
        private val farTopRight = LatLng(farTopLeft.latitude, farBottomRight.longitude)
        private val farBottomLeft = LatLng(farBottomRight.latitude, farTopLeft.longitude)

        val bounds = LatLngBounds.from(topLeft.latitude, bottomRight.longitude, bottomRight.latitude, topLeft.longitude)!!

        val boundaryPolygonOptions = PolygonOptions()
                .add(farTopLeft, farTopRight, farBottomRight, farBottomLeft, farTopLeft)
                .fillColor(Color.valueOf(0.toFloat(), 0.toFloat(), 0.toFloat(), .5.toFloat()).toArgb())
                .strokeColor(Color.RED)
                .addHole(listOf(topLeft, topRight, bottomRight, bottomLeft, topLeft))!!
    }

    @SuppressLint("MissingPermission")
    override fun onConnected() {
        Timber.d("onConnected")
        locationEngine.requestLocationUpdates()
    }

    private var currentlyInside = true
    private var boundaryPolygon: Polygon? = null
    private var boundarySnackbar: Snackbar? = null

    override fun onLocationChanged(location: Location) {
        Timber.d("onLocationChanged %s", location)
        Toast.makeText(Mapbox.getApplicationContext(), String.format("Location: %.0f, %.0f", location.latitude, location.longitude), Toast.LENGTH_SHORT).show()

        fragment.map.setMinZoomPreference(15.3)

        val point = LatLng(location.latitude, location.longitude)
        val inside = bounds.contains(point)

        if (inside != currentlyInside) {
            currentlyInside = inside

            boundarySnackbar = if (inside) {
                boundarySnackbar?.dismiss()
                null
            } else {
                fragment.view?.findViewById<View>(R.id.playFrame)?.let {
                    val sb = Snackbar.make(it, "You are not in the game area!", Snackbar.LENGTH_INDEFINITE)
                    sb.show()
                    sb
                }
            }

            boundaryPolygon = if (inside) {
                boundaryPolygon?.let {
                    fragment.map.removePolygon(it)
                }
                null
            } else {
                fragment.map.addPolygon(boundaryPolygonOptions)
            }
        }
    }
}