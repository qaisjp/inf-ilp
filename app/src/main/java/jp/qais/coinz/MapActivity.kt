package jp.qais.coinz

import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Menu
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.constants.Style
import com.mapbox.mapboxsdk.maps.MapView

import kotlinx.android.synthetic.main.activity_map.*
import timber.log.Timber

class MapActivity : AppCompatActivity() {
    private lateinit var mapView: MapView
    private val tag = "MapActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val tok = getString(R.string.app_mapbox_pk)
        Mapbox.getInstance(this, tok)

        setContentView(R.layout.activity_map)
        setSupportActionBar(toolbar)

        mapView = findViewById(R.id.mapView)
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync({
            it.setStyle(Style.SATELLITE)
            // Customize map with markers, polylines, etc.
        })
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