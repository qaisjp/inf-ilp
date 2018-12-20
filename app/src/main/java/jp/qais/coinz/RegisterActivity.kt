package jp.qais.coinz

import android.Manifest.permission.READ_CONTACTS
import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.annotation.TargetApi
import android.app.LoaderManager.LoaderCallbacks
import android.content.CursorLoader
import android.content.Intent
import android.content.Loader
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.ContactsContract
import android.support.design.widget.Snackbar
import android.support.v4.app.NavUtils
import android.support.v7.app.AppCompatActivity
import android.text.TextUtils
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.*
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_register.*
import timber.log.Timber
import java.util.*
import kotlin.collections.HashMap

/**
 * A register screen that offers register via email/password.
 */
class RegisterActivity : AppCompatActivity(), LoaderCallbacks<Cursor> {
    private lateinit var mAuth: FirebaseAuth
    private lateinit var mDb : FirebaseFirestore

    companion object {

        /**
         * Id to identity READ_CONTACTS permission request.
         */
        private val REQUEST_READ_CONTACTS = 0
    }

    // From: https://developer.android.com/training/implementing-navigation/ancestral
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                // Respond to the action bar's Up/Home button
                NavUtils.navigateUpFromSameTask(this)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mAuth = FirebaseAuth.getInstance()
        mDb = FirebaseFirestore.getInstance()

        // Show the content
        setContentView(R.layout.activity_register)

        setSupportActionBar(toolbar)
//        setSupportActionBar(findViewById(R.menu.menu_register))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        email.setText(intent.getStringExtra("email"))
        password.setText(intent.getStringExtra("password"))

        // Set up the register form.
        populateAutoComplete()

        name.setOnEditorActionListener { _, id, _ ->
            if (id == EditorInfo.IME_ACTION_NEXT || id == EditorInfo.IME_NULL) {
                email.requestFocus()
                return@setOnEditorActionListener true
            }
            false
        }

        email.setOnEditorActionListener { _, id, _ ->
            if (id == EditorInfo.IME_ACTION_NEXT || id == EditorInfo.IME_NULL) {
                password.requestFocus()
                return@setOnEditorActionListener true
            }
            false
        }

        password.setOnEditorActionListener { _, id, _ ->
            if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                attemptRegister()
                return@setOnEditorActionListener true
            }
            false
        }

        email_sign_up_button.setOnClickListener { attemptRegister() }
    }

    fun updateUI(user: FirebaseUser) {
        Timber.d("updateUI(%s)", user.email)
        Snackbar.make(register_form, String.format("Hello, %s", user.email), Snackbar.LENGTH_INDEFINITE).show()

        val intent = Intent(this, GameActivity::class.java)
        startActivity(intent)

        finish()
    }

    private fun populateAutoComplete() {
        if (!mayRequestContacts()) {
            return
        }

        loaderManager.initLoader(0, null, this)
    }

    private fun mayRequestContacts(): Boolean {
        if (checkSelfPermission(READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
            return true
        }
        if (shouldShowRequestPermissionRationale(READ_CONTACTS)) {
            Snackbar.make(email, R.string.permission_rationale, Snackbar.LENGTH_INDEFINITE)
                    .setAction(android.R.string.ok
                    ) { requestPermissions(arrayOf(READ_CONTACTS), REQUEST_READ_CONTACTS) }.show()
        } else {
            requestPermissions(arrayOf(READ_CONTACTS), REQUEST_READ_CONTACTS)
        }
        return false
    }

    /**
     * Callback received when a permissions request has been completed.
     */
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>,
                                            grantResults: IntArray) {
        if (requestCode == REQUEST_READ_CONTACTS) {
            if (grantResults.size == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                populateAutoComplete()
            }
        }
    }

    private fun validateInput(): Triple<Boolean, String, String> {
        // Reset errors.
        name.error = null
        email.error = null
        password.error = null

        // Store values at the time of the register attempt.
        val nameStr = name.text.toString()
        val emailStr = email.text.toString()
        val passwordStr = password.text.toString()

        var focusView: View? = null

        // Check if name entered
        if (TextUtils.isEmpty(nameStr)) {
            name.error = getText(R.string.error_field_required)
            focusView = name
        }

        // Check for a valid password, if the user entered one.
        if (TextUtils.isEmpty(passwordStr)) {
            password.error = getString(R.string.error_invalid_password)
            focusView = password
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(emailStr)) {
            email.error = getString(R.string.error_field_required)
            focusView = email
        }

        if (focusView != null) {
            // There was an error; focus the first form field with an error.
            focusView.requestFocus()
            return Triple(false, "", "")
        }

        return Triple(true, emailStr, passwordStr)
    }

    /**
     * Attempts to register the account specified by the register form
     */
    private fun attemptRegister() {
        val (success, email, password) = validateInput()
        if (!success) {
            return
        }

        val nameStr = name.text.toString()

        showProgress(true)

        mAuth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                val id = it.user.uid
                val user = HashMap<String, Any>()
                user["email"] = email
                user["name"] = nameStr

                mDb.collection("users").document(id).set(user)
                    .addOnFailureListener { e ->
                        Toast.makeText(this, e.localizedMessage, Toast.LENGTH_LONG).show()
                        showProgress(false)
                    }
                    .addOnSuccessListener {
                        updateUI(mAuth.currentUser!!)
                    }

            }
            .addOnFailureListener { e ->
                var view: TextView? = null
                when (e) {
                    is FirebaseAuthUserCollisionException -> view = this.email
                    is FirebaseAuthWeakPasswordException -> view = this.password
                    is FirebaseAuthInvalidCredentialsException -> view = this.email
                    else -> Toast.makeText(this, e.localizedMessage, Toast.LENGTH_LONG).show()
                }

                view?.let {
                    it.error = e.localizedMessage
                    it.requestFocus()
                }

                showProgress(false)
            }
    }

    /**
     * Shows the progress UI and hides the register form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private fun showProgress(show: Boolean) {
        val shortAnimTime = resources.getInteger(android.R.integer.config_shortAnimTime).toLong()

        register_form.visibility = if (show) View.GONE else View.VISIBLE
        register_form.animate()
                .setDuration(shortAnimTime)
                .alpha((if (show) 0 else 1).toFloat())
                .setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        register_form.visibility = if (show) View.GONE else View.VISIBLE
                    }
                })

        register_progress.visibility = if (show) View.VISIBLE else View.GONE
        register_progress.animate()
                .setDuration(shortAnimTime)
                .alpha((if (show) 1 else 0).toFloat())
                .setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        register_progress.visibility = if (show) View.VISIBLE else View.GONE
                    }
                })
    }

    override fun onCreateLoader(i: Int, bundle: Bundle?): Loader<Cursor> {
        return CursorLoader(this,
                // Retrieve data rows for the device user's 'profile' contact.
                Uri.withAppendedPath(ContactsContract.Profile.CONTENT_URI,
                        ContactsContract.Contacts.Data.CONTENT_DIRECTORY), ProfileQuery.PROJECTION,

                // Select only email addresses.
                ContactsContract.Contacts.Data.MIMETYPE + " = ?", arrayOf(ContactsContract.CommonDataKinds.Email
                .CONTENT_ITEM_TYPE),

                // Show primary email addresses first. Note that there won't be
                // a primary email address if the user hasn't specified one.
                ContactsContract.Contacts.Data.IS_PRIMARY + " DESC")
    }

    override fun onLoadFinished(cursorLoader: Loader<Cursor>, cursor: Cursor) {
        val emails = ArrayList<String>()
        cursor.moveToFirst()
        while (!cursor.isAfterLast) {
            emails.add(cursor.getString(ProfileQuery.ADDRESS))
            cursor.moveToNext()
        }

        addEmailsToAutoComplete(emails)
    }

    override fun onLoaderReset(cursorLoader: Loader<Cursor>) {

    }

    private fun addEmailsToAutoComplete(emailAddressCollection: List<String>) {
        //Create adapter to tell the AutoCompleteTextView what to show in its dropdown list.
        val adapter = ArrayAdapter(this@RegisterActivity,
                android.R.layout.simple_dropdown_item_1line, emailAddressCollection)

        email.setAdapter(adapter)
    }

    object ProfileQuery {
        val PROJECTION = arrayOf(
                ContactsContract.CommonDataKinds.Email.ADDRESS,
                ContactsContract.CommonDataKinds.Email.IS_PRIMARY)
        val ADDRESS = 0
        val IS_PRIMARY = 1
    }
}
