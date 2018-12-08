package jp.qais.coinz

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import java.time.Instant
import java.util.*

object Prefs {
    private val filename = "MyPrefsFile"
    private lateinit var prefs: SharedPreferences
    private var editor: SharedPreferences.Editor? = null

    fun init(context: Context) {
        prefs = context.getSharedPreferences(filename, Context.MODE_PRIVATE)
    }

    var firstTime: Boolean
        get() = prefs.getBoolean("firstTime", true)
        set(value) = edit {
            it.putBoolean("firstTime", value)
        }

    var darkMode: Boolean
        get() = prefs.getBoolean("darkMode", false)
        set(value) = edit {
            it.putBoolean("darkMode", value)
        }

    var mapLastUpdate: Instant
        get() = Instant.ofEpochSecond(prefs.getLong("mapLastUpdate", 0))
        set(value) = edit {
            it.putLong("mapLastUpdate", value.epochSecond)
        }

    @SuppressLint("CommitPrefEdits")
    private inline fun edit(action: (SharedPreferences.Editor) -> Unit) {
        val shouldCommit = editor == null
        if (shouldCommit) {
            editor = prefs.edit()
        }

        action(editor!!)

        if (shouldCommit) {
            editor!!.apply()
            editor = null
        }
    }

    // For batch operations
    fun perform(action: () -> Unit) {
        val editor = prefs.edit()
        this.editor = editor
        action()
        editor.apply()
        this.editor = null
    }
}