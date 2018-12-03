package jp.qais.coinz

import android.annotation.SuppressLint
import android.location.Location
import android.widget.Toast
import com.mapbox.android.core.location.LocationEngine
import com.mapbox.android.core.location.LocationEngineListener
import com.mapbox.mapboxsdk.Mapbox
import timber.log.Timber

class CoinFinder : LocationEngineListener {
    lateinit var locationEngine: LocationEngine

    @SuppressLint("MissingPermission")
    override fun onConnected() {
        Timber.d("onConnected")
        locationEngine.requestLocationUpdates()
    }

    override fun onLocationChanged(location: Location) {
        Timber.d("onLocationChanged %s", location)
        Toast.makeText(Mapbox.getApplicationContext(), String.format("Location: %.0f, %.0f", location.latitude, location.longitude), Toast.LENGTH_SHORT).show()
    }
}