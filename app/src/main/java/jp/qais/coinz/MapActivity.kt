package jp.qais.coinz

import android.annotation.SuppressLint
import android.location.Location
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.widget.Toast
import com.mapbox.android.core.location.LocationEngine
import com.mapbox.android.core.location.LocationEnginePriority
import com.mapbox.android.core.location.LocationEngineProvider
import com.mapbox.android.core.permissions.PermissionsListener
import com.mapbox.android.core.permissions.PermissionsManager
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.location.LocationComponent
import com.mapbox.mapboxsdk.location.modes.CameraMode
import com.mapbox.mapboxsdk.location.modes.RenderMode
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback

import kotlinx.android.synthetic.main.activity_map.*
import timber.log.Timber

class MapActivity : AppCompatActivity(), OnMapReadyCallback, PermissionsListener {
    private lateinit var mapView: MapView
    private lateinit var map: MapboxMap

    private lateinit var permissionsManager: PermissionsManager
    private var coinFinder: CoinFinder = CoinFinder()
    private lateinit var locationComponent: LocationComponent

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val tok = getString(R.string.app_mapbox_pk)
        Mapbox.getInstance(this, tok)

        setContentView(R.layout.activity_map)
        setSupportActionBar(toolbar)

        mapView = findViewById(R.id.mapView)
        mapView.onCreate(savedInstanceState)

        mapView.getMapAsync(this)
    }

    override fun onMapReady(map: MapboxMap) {
        this.map = map

        // Customize map with markers, polylines, etc.
        // map.setStyle(Style.SATELLITE_STREETS)

        enableLocationComponent()
    }

    @SuppressLint("MissingPermission")
    private fun enableLocationComponent() {
        if (!PermissionsManager.areLocationPermissionsGranted(this)) {
            Timber.d("Permissions not granted yet")
            permissionsManager = PermissionsManager(this)
            permissionsManager.requestLocationPermissions(this)
            return
        }

        Timber.d("Permissions granted!")

        val locationEngine = LocationEngineProvider(this).obtainBestLocationEngineAvailable()

        coinFinder.locationEngine = locationEngine

        locationEngine.addLocationEngineListener(coinFinder)
        locationEngine.apply {
            priority = LocationEnginePriority.HIGH_ACCURACY
            fastestInterval = 1 * 1000 // at most every 1s
            // interval = 5 * 1000 // preferably every 5s
            activate()
        }

        locationComponent = map.locationComponent
        locationComponent.activateLocationComponent(this, locationEngine)
        locationComponent.isLocationComponentEnabled = true
        locationComponent.cameraMode = CameraMode.TRACKING
        locationComponent.renderMode = RenderMode.NORMAL // try COMPASS

        Timber.d("locationEngine.isConnected is %s", locationComponent.locationEngine?.isConnected)
    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        Timber.d("onRequestPermissionsResult")
        permissionsManager.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onExplanationNeeded(permissionsToExplain: MutableList<String>?) {
        Toast.makeText(this, R.string.user_location_permission_explanation, Toast.LENGTH_LONG).show()
    }

    override fun onPermissionResult(granted: Boolean) {
        if (!granted) {
            Toast.makeText(this, R.string.user_location_permission_not_granted, Toast.LENGTH_LONG).show()
            finish()
            return
        }

        enableLocationComponent()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)

        return true
    }

    public override fun onStart() {
        super.onStart()
        mapView.onStart()
    }

    public override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    public override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    public override fun onStop() {
        super.onStop()
        mapView.onStop()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView.onSaveInstanceState(outState)
    }
}