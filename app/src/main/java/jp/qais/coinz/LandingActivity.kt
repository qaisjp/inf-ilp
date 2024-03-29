package jp.qais.coinz

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Switch

import kotlinx.android.synthetic.main.activity_landing.*
import timber.log.Timber

/**
 * Unused debug activity.
 */
class LandingActivity : AppCompatActivity() {

    private val tag = "LandingActivity"
    private var downloadDate = "" // Format: YYYY/MM/DD
    private var darkMode = false
    private val preferencesFile = "MyPrefsFile"


    private var tally = 0

    private fun showTally(view: View) {
        textTally.text = "$tally"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_landing)
        setSupportActionBar(toolbar)

//        btnUpvote.setOnClickListener { view ->
//            tally += 1
//            showTally(view)
//        }

//        btnDownvote.setOnClickListener { view ->
//            tally -= 1
//            showTally(view)
//        }


        btnToMap.setOnClickListener { _ ->
            val myIntent = Intent(this, GameActivity::class.java)
            startActivity(myIntent)
        }

        btnToLogin.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

        btnWelcome.setOnClickListener {
            val intent = Intent(this, WelcomeActivity::class.java)
            startActivity(intent)
            finish()
        }

        btnGame.setOnClickListener {
            val intent = Intent(this, GameActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onStart() {
        super.onStart()

        // Restore preferences
        val settings = getSharedPreferences(preferencesFile, Context.MODE_PRIVATE)

        // Initialise default values
        downloadDate = settings.getString("lastDownloadDate", "")
        darkMode = Prefs.darkMode

        Timber.d("[onStart] lastDownloadDate is: %s", downloadDate)
    }

    override fun onStop() {
        super.onStop()

        Timber.d("[onStop] Storing preferences")

        val settings = getSharedPreferences(preferencesFile, Context.MODE_PRIVATE)
        val editor = settings.edit()
        editor.putString("lastDownloadDate", downloadDate)

        Prefs.darkMode = darkMode

        // Apply edits
        editor.apply()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_register, menu)

//        val switchDarkMode = menu.findItem(R.id.action_darkmode).actionView.findViewById<Switch>(R.id.darkModeSwitch)
//        switchDarkMode.isChecked = darkMode
//        switchDarkMode.setOnCheckedChangeListener { _, isChecked ->
//            darkMode = isChecked
//            Timber.d("darkMode: %s", darkMode)
//        }
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
