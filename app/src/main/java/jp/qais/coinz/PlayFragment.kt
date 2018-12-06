package jp.qais.coinz


import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.os.Looper
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
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.geometry.LatLngBounds
import com.mapbox.mapboxsdk.location.LocationComponent
import com.mapbox.mapboxsdk.location.modes.CameraMode
import com.mapbox.mapboxsdk.location.modes.RenderMode
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback
import com.mapbox.mapboxsdk.maps.SupportMapFragment
import com.mapbox.mapboxsdk.style.layers.Layer
import kotlinx.android.synthetic.main.fragment_play.*
import timber.log.Timber
import java.util.*
import kotlin.concurrent.scheduleAtFixedRate

/**
 * A simple [Fragment] subclass.
 *
 */
class PlayFragment : Fragment(), OnMapReadyCallback, PermissionsListener {

    internal lateinit var context: Context

    private lateinit var permissionsManager: PermissionsManager
    private var coinFinder: CoinFinder = CoinFinder()
    private lateinit var locationComponent: LocationComponent
    private lateinit var mapView: MapView
    private lateinit var map: MapboxMap

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        this.context = requireContext()

        val tok = getString(R.string.app_mapbox_pk)
        Mapbox.getInstance(context, tok)

        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_play, container, false)

        val mapFragment = childFragmentManager.findFragmentById(R.id.mapFragment) as SupportMapFragment
        mapFragment.getMapAsync(this)

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

    /**
     * Mapbox stuff
     */
    override fun onMapReady(map: MapboxMap) {
        this.map = map

        // Customize map with markers, polylines, etc.
        // map.setStyle(Style.SATELLITE_STREETS)

        val bounds = LatLngBounds.from(55.946233, -3.184319, 55.942617, -3.192473)
        map.setLatLngBoundsForCameraTarget(bounds)
        map.setMinZoomPreference(15.0)

        enableLocationComponent()
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
