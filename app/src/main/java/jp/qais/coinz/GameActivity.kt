package jp.qais.coinz

import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import kotlinx.android.synthetic.main.activity_game.*
import timber.log.Timber
import java.net.URL
import java.time.Instant

class GameActivity : AppCompatActivity() {
    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        startFragment(item.itemId)
        true
    }

    private var currentMenu: Int? = null
    private lateinit var currentFragment: Fragment
    private var currentFragmentID = R.id.navigation_play // Default fragment is navigation_play
    private var dataReady = false

    /** The timer that ensures refreshCoins is always called when needed. **/
    private var mapUpdateTimer = MapUpdateTimer(::refreshCoins)

    /** startFragment starts the fragment defined by the navigation ID **/
    private fun startFragment(frag: Int) {
        currentFragmentID = frag
        when (frag) {
            R.id.navigation_play -> {
                currentFragment = PlayFragment()
                currentMenu = null
            }
            R.id.navigation_scoreboard -> {
                currentFragment = ScoreboardFragment()
                toolbar.title = getText(R.string.title_leaderboard)
                currentMenu = null
            }
            R.id.navigation_account -> {
                currentFragment = AccountFragment()
                toolbar.title = getText(R.string.title_account)
                currentMenu = R.menu.menu_account
            }
            R.id.navigation_payments -> {
                currentFragment = PaymentsFragment()
                toolbar.title = getText(R.string.title_payments)
                currentMenu = R.menu.menu_payments
            }
            else -> throw RuntimeException("Starting unknown fragment")
        }

        invalidateOptionsMenu()

        if (dataReady) {
            supportFragmentManager.beginTransaction().replace(R.id.gameFrame, currentFragment).commit()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)
        setSupportActionBar(toolbar)

        BottomNavigationViewHelper.removeShiftMode(navigation)

        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)

        refreshCoins()
    }

    override fun onResume() {
        super.onResume()

        Prefs.firstTime = false

        Utils.verifyUser(this)

        mapUpdateTimer.restart()
    }

    override fun onPause() {
        super.onPause()

        mapUpdateTimer.stop()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        currentMenu?.let { currentMenu ->
            // Inflate the menu; this adds items to the action bar if it is present.
            menuInflater.inflate(currentMenu, menu)
            return true
        }

        // Returning false here hides the menu
        return false
    }

    /**
     * refreshCoins ensures that GameActivity always has the latest CoinMap loaded.
     *
     * It downloads the map & refreshes the current tab.
     */
    private fun refreshCoins() {
        dataReady = true

        Timber.d("CALLED AT %d", Instant.now().epochSecond)

        val url = Utils.getMapURL()
        Timber.d("URL is: %s", url.toString())
        val task = DownloadFileTask(url) {
            Timber.d(it)

            // Refresh current fragment
            startFragment(currentFragmentID)
        }
    }
}
