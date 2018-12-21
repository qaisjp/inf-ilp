package jp.qais.coinz

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import java.net.URL
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.*

internal object Utils {
    fun verifyUser(activity: Activity, callback: () -> Unit = {}) {
        val mAuth = FirebaseAuth.getInstance()

        if (mAuth.currentUser == null) {
            val intent = Intent(activity, LoginActivity::class.java)
            activity.startActivity(intent)
            activity.finish()
            return
        }

        mAuth.currentUser?.getIdToken(true)
            ?.addOnSuccessListener {
                callback()
            }
            ?.addOnFailureListener {
                Toast.makeText(activity, it.localizedMessage, Toast.LENGTH_LONG).show()

                val intent = Intent(activity, LoginActivity::class.java)
                activity.startActivity(intent)
                activity.finish()
            }
    }

    fun getToday() = Instant.now().truncatedTo(ChronoUnit.DAYS)
    fun getTomorrow() = getToday().plus(1, ChronoUnit.DAYS)

    // Debug version of getTomorrow (10 seconds from now)
    // TODO: Make sure this is not enabled
    // fun getTomorrow() = Instant.now().plus(10, ChronoUnit.SECONDS)

    /** getMapURL returns the URL for today's map **/
    @SuppressLint("SimpleDateFormat")
    fun getMapURL(): URL {
        // We suppress SimpleDateFormat as we don't care about locale here
        val subdir = SimpleDateFormat("yyyy/M/dd").format(Date.from(getToday()))
        return URL("http://homepages.inf.ed.ac.uk/stg/coinz/$subdir/coinzmap.geojson")
    }

    // From: https://stackoverflow.com/questions/332079/in-java-how-do-i-convert-a-byte-array-to-a-string-of-hex-digits-while-keeping-l
    fun toHexString(bytes: ByteArray): String {
        val hexString = StringBuilder()

        for (i in bytes.indices) {
            val hex = Integer.toHexString(0xFF and bytes[i].toInt())
            if (hex.length == 1) {
                hexString.append('0')
            }
            hexString.append(hex)
        }

        return hexString.toString()
    }
}
