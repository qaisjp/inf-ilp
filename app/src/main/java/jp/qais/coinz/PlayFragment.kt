package jp.qais.coinz


import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.mapbox.android.core.location.LocationEnginePriority
import com.mapbox.android.core.location.LocationEngineProvider
import com.mapbox.android.core.permissions.PermissionsListener
import com.mapbox.android.core.permissions.PermissionsManager
import com.mapbox.geojson.FeatureCollection
import com.mapbox.geojson.Point
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.annotations.MarkerOptions
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.location.LocationComponent
import com.mapbox.mapboxsdk.location.modes.CameraMode
import com.mapbox.mapboxsdk.location.modes.RenderMode
import com.mapbox.mapboxsdk.maps.MapFragment.OnMapViewReadyCallback
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback
import timber.log.Timber
import java.lang.Math.floor
import kotlin.math.floor

/**
 * A simple [Fragment] subclass.
 *
 */
class PlayFragment: Fragment(), OnMapReadyCallback, PermissionsListener, OnMapViewReadyCallback {
    internal lateinit var context: Context

    private lateinit var permissionsManager: PermissionsManager
    private var coinFinder: CoinFinder = CoinFinder(this)
    private lateinit var locationComponent: LocationComponent
    private lateinit var mapView: MapView
    internal lateinit var map: MapboxMap
    private lateinit var mapFragment: MapFragment

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        this.context = requireContext()

        val tok = getString(R.string.app_mapbox_pk)
        Mapbox.getInstance(context, tok)

        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_play, container, false)

        mapFragment = childFragmentManager.findFragmentById(R.id.mapFragment) as MapFragment
        mapFragment.getMapViewAsync(this)

        return view
    }

    override fun onResume() {
        super.onResume()
        (activity as AppCompatActivity).supportActionBar?.hide()
    }

    override fun onStop() {
        super.onStop()
        (activity as AppCompatActivity).supportActionBar?.show()
    }

    override fun onMapViewReady(mapView: MapView) {
        this.mapView = mapView
        mapFragment.getMapAsync(this)
    }

    /**
     * Mapbox stuff
     */
    override fun onMapReady(map: MapboxMap) {
        this.map = map

        // Set camera bounds & min zoom (preferred)
        map.setLatLngBoundsForCameraTarget(CoinFinder.bounds)
        map.animateCamera(CameraUpdateFactory.zoomTo(15.3))
        map.setMinZoomPreference(15.3)

//        val t = Timer()
//        val act = this.activity!!
//        t.scheduleAtFixedRate(0, 1000) {
//            act.runOnUiThread {
//                Toast.makeText(context, String.format("Zoom %f", map.cameraPosition.zoom), Toast.LENGTH_SHORT).show()
//            }
//        }

        // Customize map with markers, polylines, etc.
        // map.setStyle(Style.SATELLITE_STREETS)

        enableLocationComponent()

        for (coin in DataManager.coins) {
           map.addMarker(MarkerOptions().apply {
               position = coin.latLng
               title = floor(coin.value).toString()
           })
        }

    }

    @SuppressLint("MissingPermission")
    private fun enableLocationComponent() {
        if (!PermissionsManager.areLocationPermissionsGranted(context)) {
            Timber.d("Permissions not granted yet")
            permissionsManager = PermissionsManager(this)
            permissionsManager.requestLocationPermissions(activity)
            return
        }

        Timber.d("Permissions granted!")

        val locationEngine = LocationEngineProvider(context).obtainBestLocationEngineAvailable()

        coinFinder.locationEngine = locationEngine

        locationEngine.addLocationEngineListener(coinFinder)
        locationEngine.apply {
            priority = LocationEnginePriority.HIGH_ACCURACY
            fastestInterval = 1 * 1000 // at most every 1s
            interval = 5 * 1000 // preferably every 5s
            activate()
        }

        locationComponent = map.locationComponent
        locationComponent.activateLocationComponent(context, locationEngine)
        locationComponent.isLocationComponentEnabled = true
        locationComponent.cameraMode = CameraMode.TRACKING
        locationComponent.renderMode = RenderMode.COMPASS

        Timber.d("locationEngine.isConnected is %s", locationComponent.locationEngine?.isConnected)
    }

    /**
     * Permissions stuff
     */
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        Timber.d("onRequestPermissionsResult")
        permissionsManager.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onExplanationNeeded(permissionsToExplain: MutableList<String>?) {
        Toast.makeText(context, R.string.user_location_permission_explanation, Toast.LENGTH_LONG).show()
    }

    override fun onPermissionResult(granted: Boolean) {
        if (!granted) {
            Toast.makeText(context, R.string.user_location_permission_not_granted, Toast.LENGTH_LONG).show()
            requireActivity().finish()
            return
        }

        enableLocationComponent()
    }
}
