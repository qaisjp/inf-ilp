package jp.qais.coinz

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences

object Prefs {
    private val filename = "MyPrefsFile"
    private lateinit var prefs: SharedPreferences
    private var editor: SharedPreferences.Editor? = null
    private var shouldCommit = true

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

    @SuppressLint("CommitPrefEdits")
    private inline fun edit(action: (SharedPreferences.Editor) -> Unit) {
        if (shouldCommit == true) {
            editor = prefs.edit()
        }

        action(editor!!)

        if (shouldCommit == true) {
            editor!!.apply()
        }
    }

    // For batch operations
    fun perform(action: () -> Unit) {
        shouldCommit = false
        val editor = prefs.edit()
        this.editor = editor
        action()
        editor.apply()
        this.editor = null
        shouldCommit = true
    }
}