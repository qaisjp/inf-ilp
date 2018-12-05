package jp.qais.coinz

import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v7.app.AppCompatActivity
import com.mapbox.mapboxsdk.maps.MapFragment
import kotlinx.android.synthetic.main.activity_game.*

class GameActivity : AppCompatActivity() {

    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.navigation_home -> {
                val fragment = MapFragment()
                fragmentManager.beginTransaction().let {
                    it.replace(R.id.gameFrame, fragment as android.app.Fragment)
//                    it.addToBackStack(null) // this pushes to stack
                    it.commit()
                }

                return@OnNavigationItemSelectedListener true
            }
//            R.id.navigation_dashboard -> {
//                message.setText(R.string.title_leaderboard)
//                return@OnNavigationItemSelectedListener true
//            }
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
        false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)

        BottomNavigationViewHelper.removeShiftMode(navigation)

        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
    }
}
