package jp.qais.coinz

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_splash)

        hide()
        verifyUser(this) {
            val intent = Intent(this, GameActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    fun show() {
        supportActionBar?.show()
    }

    private fun hide() {
        // Hide UI first
        supportActionBar?.hide()
    }
}
