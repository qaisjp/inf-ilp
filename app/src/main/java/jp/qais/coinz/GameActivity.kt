package jp.qais.coinz

import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import com.mapbox.mapboxsdk.maps.SupportMapFragment
import kotlinx.android.synthetic.main.activity_game.*

class GameActivity : AppCompatActivity() {
    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        startFragment(item.itemId)
        true
    }

    fun startFragment(frag: Int) {
        lateinit var fragment: Fragment
        when (frag) {
            R.id.navigation_play -> {
                fragment = PlayFragment()
            }
            R.id.navigation_scoreboard -> {
                fragment = ScoreboardFragment()
            }
            R.id.navigation_account -> {
                fragment = AccountFragment()
            }
            R.id.navigation_payments -> {
                fragment = PaymentsFragment()
            }
            R.id.navigation_notifications -> {
                fragment = SupportMapFragment()
            }
        }

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
}
