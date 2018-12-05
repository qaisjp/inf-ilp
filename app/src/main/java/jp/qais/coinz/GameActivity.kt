package jp.qais.coinz

import android.app.Fragment
import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v7.app.AppCompatActivity
import com.mapbox.mapboxsdk.maps.MapFragment
import kotlinx.android.synthetic.main.activity_game.*

class GameActivity : AppCompatActivity() {
    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        lateinit var fragment: Fragment
        when (item.itemId) {
            R.id.navigation_home -> {
                fragment = MapFragment()
            }
            R.id.navigation_dashboard -> {
                fragment = jp.qais.coinz.MapFragment()
//                message.setText(R.string.title_leaderboard)
//                return@OnNavigationItemSelectedListener true
            }
//            R.id.navigation_account -> {
//                message.setText(R.string.title_account)
//                return@OnNavigationItemSelectedListener true
//            }
//            R.id.navigation_payments -> {
//                message.setText(R.string.title_payments)
//                return@OnNavigationItemSelectedListener true
//            }
//            R.id.navigation_notifications -> {
//                message.setText(R.string.title_notifications)
//                return@OnNavigationItemSelectedListener true
//            }
        }

        fragmentManager.beginTransaction().replace(R.id.gameFrame, fragment).commit()

        true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)

        BottomNavigationViewHelper.removeShiftMode(navigation)

        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
    }
}
