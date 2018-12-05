package jp.qais.coinz

import android.annotation.SuppressLint
import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import com.mapbox.mapboxsdk.maps.MapFragment
import kotlinx.android.synthetic.main.activity_game.*
import android.support.design.internal.BottomNavigationItemView
import java.lang.reflect.AccessibleObject.setAccessible
import java.lang.reflect.Array.setBoolean
import android.support.design.internal.BottomNavigationMenuView
import timber.log.Timber


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

        removeShiftMode(navigation)

        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
    }

    // From https://stackoverflow.com/a/47407229/1517394
    @SuppressLint("RestrictedApi")
    private fun removeShiftMode(view: BottomNavigationView) {
        //this will remove shift mode for bottom navigation view
        val menuView = view.getChildAt(0) as BottomNavigationMenuView
        try {
            val shiftingMode = menuView.javaClass.getDeclaredField("mShiftingMode")
            shiftingMode.isAccessible = true
            shiftingMode.setBoolean(menuView, false)
            shiftingMode.isAccessible = false
            for (i in 0 until menuView.childCount) {
                val item = menuView.getChildAt(i) as BottomNavigationItemView
                item.setShiftingMode(false)
                // set once again checked value, so view will be updated
                item.setChecked(item.itemData.isChecked)
            }

        } catch (e: NoSuchFieldException) {
            Timber.e("ERROR NO SUCH FIELD: Unable to get shift mode field")
        } catch (e: IllegalAccessException) {
            Timber.e("ERROR ILLEGAL ALG: Unable to change value of shift mode")
        }

    }
}
