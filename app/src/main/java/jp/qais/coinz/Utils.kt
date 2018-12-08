package jp.qais.coinz

import android.app.Activity
import android.content.Intent
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
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

        mAuth.currentUser?.getIdToken(true)?.addOnSuccessListener {
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
}
