package jp.qais.coinz

import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Switch
import android.widget.ToggleButton

import kotlinx.android.synthetic.main.activity_main.*
import java.net.URL

class MainActivity : AppCompatActivity() {

    private var tally = 0

    private fun showTally(view: View) {
        textTally.text = "$tally"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        btnUpvote.setOnClickListener { view ->
            tally += 1
            showTally(view)
        }

        btnDownvote.setOnClickListener { view ->
            tally -= 1
            showTally(view)
        }

        val task = DownloadFileTask(DownloadCompleteRunner)
        task.execute(URL("http://homepages.inf.ed.ac.uk/stg/coinz/2018/12/01/coinzmap.geojson"))
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)

        val switchDarkMode = menu.findItem(R.id.action_darkmode).actionView.findViewById<Switch>(R.id.darkModeSwitch)
        switchDarkMode.setOnCheckedChangeListener { _, isChecked ->
            Log.d("darkMode", if (isChecked) "YEAH BOIS" else "-.-")
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }
}
