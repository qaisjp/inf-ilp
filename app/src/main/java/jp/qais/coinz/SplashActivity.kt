package jp.qais.coinz

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import timber.log.Timber

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // General initialise
        Timber.plant(Timber.DebugTree())
        Prefs.init(this)

        // Set content
        setContentView(R.layout.activity_splash)

        // If it's their first time running the app, show them the Welcome Activity
        if (Prefs.firstTime) {
            val intent = Intent(this, WelcomeActivity::class.java)
            startActivity(intent)
            finish()
            return
        }

        // Check if the user is valid.
        // - If valid, we start the GameActivity.
        // - Otherwise this function will show LoginActivity.
        Utils.verifyUser(this) {
            val intent = Intent(this, GameActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}
