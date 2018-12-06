package jp.qais.coinz

import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_game.*

class GameActivity : AppCompatActivity() {
    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        lateinit var fragment: Fragment
        when (item.itemId) {
            R.id.navigation_home -> {
                fragment = MapFragment()
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
//                fragment = com.mapbox.mapboxsdk.maps.MapFragment()
            }
        }

        // todo: investigate why it breaks without addToBackStack
        supportFragmentManager.beginTransaction().replace(R.id.gameFrame, fragment).addToBackStack(null).commit()

        true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)
        setSupportActionBar(toolbar)


        BottomNavigationViewHelper.removeShiftMode(navigation)

        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
    }
}
