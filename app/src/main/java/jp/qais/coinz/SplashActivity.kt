package jp.qais.coinz

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import timber.log.Timber

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // General initialise
        Timber.plant(Timber.DebugTree())
        Prefs.init(this)

        // Set content
        setContentView(R.layout.activity_splash)

        // Hide action bar
        supportActionBar?.hide()

        if (Prefs.firstTime) {
            val intent = Intent(this, WelcomeActivity::class.java)
            startActivity(intent)
            finish()
            return
        }

        verifyUser(this) {
            val intent = Intent(this, GameActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}
