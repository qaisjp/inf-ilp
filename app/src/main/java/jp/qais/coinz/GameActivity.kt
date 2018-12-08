package jp.qais.coinz

import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import kotlinx.android.synthetic.main.activity_game.*

class GameActivity : AppCompatActivity() {
    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        startFragment(item.itemId)
        true
    }

    var currentMenu: Int? = null

    private fun startFragment(frag: Int) {
        lateinit var fragment: Fragment
        when (frag) {
            R.id.navigation_play -> {
                fragment = PlayFragment()
                currentMenu = null
            }
            R.id.navigation_scoreboard -> {
                fragment = ScoreboardFragment()
                toolbar.title = getText(R.string.title_leaderboard)
                currentMenu = null
            }
            R.id.navigation_account -> {
                fragment = AccountFragment()
                toolbar.title = getText(R.string.title_account)
                currentMenu = R.menu.menu_account
            }
            R.id.navigation_payments -> {
                fragment = PaymentsFragment()
                toolbar.title = getText(R.string.title_payments)
                currentMenu = R.menu.menu_payments
            }
        }

        invalidateOptionsMenu()
        supportFragmentManager.beginTransaction().replace(R.id.gameFrame, fragment).commit()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)
        setSupportActionBar(toolbar)

        BottomNavigationViewHelper.removeShiftMode(navigation)

        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)

        startFragment(R.id.navigation_play)
    }

    override fun onResume() {
        super.onResume()
        verifyUser(this)
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
}
