package jp.qais.coinz

import android.os.Bundle
import android.view.View
import com.mapbox.mapboxsdk.maps.MapFragment.OnMapViewReadyCallback
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.SupportMapFragment
import timber.log.Timber

class MapFragment : SupportMapFragment() {
    private var callback: OnMapViewReadyCallback? = null
    private var map: MapView? = null

    /**
     * Called when the fragment view hierarchy is created.
     *
     * @param view               The content view of the fragment
     * @param savedInstanceState THe saved instance state of the framgnt
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        this.map = view as MapView
        Timber.d("onViewCreated: called")

        // notify listeners about MapView creation
        this.callback?.let {
            Timber.d("onViewCreated: calling onMapViewReady callback")
            it.onMapViewReady(map)
            Timber.d("onViewCreated: onMapViewReady callback DONE!")
        }
    }

    fun getMapViewAsync(callback: OnMapViewReadyCallback) {
        if (this.callback != null) {
            Timber.d("getMapViewAsync: crashy washy")
            throw RuntimeException("Cannot set multiple MapView callbacks")
        }

        Timber.d("getMapViewAsync: called")
        this.callback = callback

        if (map != null) {
            Timber.d("getMapViewAsync: calling onMapViewReady callback")
            callback.onMapViewReady(map)
            Timber.d("getMapViewAsync: onMapViewReady callback DONE!")
        }
    }
}