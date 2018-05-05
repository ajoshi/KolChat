package biz.ajoshi.kolchat

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.annotation.TargetApi
import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.support.v4.app.LoaderManager
import android.support.v7.app.AppCompatActivity
import android.text.TextUtils
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.TextView
import biz.ajoshi.kolchat.accounts.AccountLoader
import biz.ajoshi.kolchat.accounts.KolAccount
import biz.ajoshi.kolchat.accounts.KolAccountManager
import biz.ajoshi.kolchat.chat.ChatSingleton
import kotlinx.android.synthetic.main.activity_login.*
import java.lang.ref.WeakReference

/**
 * A login screen that offers login via username/password. Different activity so it's easier to have a different
 * actionbar UI for the rest of the app (where we have an account)
 */
class LoginActivity : AppCompatActivity(), LoaderManager.LoaderCallbacks<List<KolAccount>>, UserLoginTask.LoginFieldContainer {

    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private var mAuthTask: UserLoginTask? = null

    private var accountList: List<KolAccount>? = null

    // Loader id for list of users
    private val USERID_LOADER_ID = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        // Set up the login form.
        populateAutoComplete()
        password.setOnEditorActionListener(TextView.OnEditorActionListener { _, id, _ ->
            if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                attemptLogin()
                return@OnEditorActionListener true
            }
            false
        })
        email_sign_in_button.setOnClickListener { attemptLogin() }
    }

    private fun populateAutoComplete() {
        supportLoaderManager.initLoader(USERID_LOADER_ID, null, this)
        username.setOnClickListener { v: View ->
            if (v is AutoCompleteTextView) {
                v.showDropDown()
            }
        }
        // set up an item click listener for the dropdown
        username.setOnItemClickListener { _, _, position, _ ->
            val tempAcctList = accountList
            // if this list is not null && its size is more than the click position, go ahead
            tempAcctList?.takeIf { tempAcctList.size > position }?.apply {
                val account = tempAcctList[position]
                attemptLogin(account.username, account.password, WeakReference(this@LoginActivity))
            }
        }
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private fun attemptLogin() {
        // we don't want to log in if we're already logging in
        if (mAuthTask != null) {
            return
        }

        // Reset errors.
        username.error = null
        password.error = null

        // Store values at the time of the login attempt.
        val usernameStr = username.text.toString()
        val passwordStr = password.text.toString()

        var cancel = false
        var focusView: View? = null

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(passwordStr) && !isPasswordValid(passwordStr)) {
            password.error = getString(R.string.error_invalid_password)
            focusView = password
            cancel = true
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(usernameStr)) {
            username.error = getString(R.string.error_field_required)
            focusView = username
            cancel = true
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView?.requestFocus()
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            attemptLogin(userNameText = usernameStr, passwordText = passwordStr, weakReference = WeakReference(this))
        }
    }

    /**
     * Attempts to log in with the given credentials
     */
    private fun attemptLogin(userNameText: String, passwordText: String, weakReference: WeakReference<UserLoginTask.LoginFieldContainer>) {
        showProgress(true)
        mAuthTask = UserLoginTask(userNameText, passwordText, weakReference)
        mAuthTask!!.execute(null as Void?)
    }

    private fun isPasswordValid(password: String): Boolean {
        //TODO: maybe we don't need this at all. I have no idea what a valid password is
        return password.length > 1
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private fun showProgress(show: Boolean) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            val shortAnimTime = resources.getInteger(android.R.integer.config_shortAnimTime).toLong()

            login_form.visibility = if (show) View.GONE else View.VISIBLE
            login_form.animate()
                    .setDuration(shortAnimTime)
                    .alpha((if (show) 0 else 1).toFloat())
                    .setListener(object : AnimatorListenerAdapter() {
                        override fun onAnimationEnd(animation: Animator) {
                            login_form.visibility = if (show) View.GONE else View.VISIBLE
                        }
                    })

            login_progress.visibility = if (show) View.VISIBLE else View.GONE
            login_progress.animate()
                    .setDuration(shortAnimTime)
                    .alpha((if (show) 1 else 0).toFloat())
                    .setListener(object : AnimatorListenerAdapter() {
                        override fun onAnimationEnd(animation: Animator) {
                            login_progress.visibility = if (show) View.VISIBLE else View.GONE
                        }
                    })
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            login_progress.visibility = if (show) View.VISIBLE else View.GONE
            login_form.visibility = if (show) View.GONE else View.VISIBLE
        }
    }

    /**********************************************************************************
     * LOADER CALLBACKS START
     **********************************************************************************/
    override fun onLoadFinished(loader: android.support.v4.content.Loader<List<KolAccount>>, data: List<KolAccount>?) {
        // save the account list for later
        accountList = data
        // now go through the list (shouldn't be too big) and make list of usernames
        // if this ends up causing issues, we can change the loader to return a list of strings
        val usernameList = mutableListOf<String>()
        data?.let {
            for (account in data) {
                usernameList.add(account.username)
            }
        }
        addUsernamesToAutocomplete(usernameList)
    }

    override fun onLoaderReset(loader: android.support.v4.content.Loader<List<KolAccount>>) {
    }

    override fun onCreateLoader(id: Int, args: Bundle?): android.support.v4.content.Loader<List<KolAccount>> {
        return AccountLoader(this)
    }

    /**********************************************************************************
     * LOADER CALLBACKS END
     **********************************************************************************/

    override fun onLogin(success: Boolean?, userNameText: String, passwordText: String) {
        mAuthTask = null
        if (success!!) {
            startActivity(Intent(this, MainActivity::class.java))
            val acctMgr = KolAccountManager(this)
            // should i be sending these values in instead of re-finding them?
            if (acctMgr.getAccount(username = userNameText) == null) {
                acctMgr.addAccount(username = userNameText, password = passwordText)
            }
            finish()
        } else {
            // failed login, so highlight the password field in the UI
            password.setText(passwordText)
            password.error = getString(R.string.error_incorrect_password)
            password.requestFocus()
        }
    }

    override fun onLoginCancelled() {
        mAuthTask = null
        showProgress(false)
    }

    private fun addUsernamesToAutocomplete(emailAddressCollection: List<String>?) {
        //Create adapter to tell the AutoCompleteTextView what to show in its dropdown list.
        val adapter = ArrayAdapter(this@LoginActivity,
                android.R.layout.simple_dropdown_item_1line, emailAddressCollection)

        // set the new values
        username.setAdapter(adapter)
        adapter.setNotifyOnChange(true)
    }
}

/**
 * Represents an asynchronous login/registration task used to authenticate
 * the user.
 */
class UserLoginTask constructor(private val userName: String, private val password: String, private val uiWeakRef: WeakReference<LoginFieldContainer>) : AsyncTask<Void, Void, Boolean>() {

    /**
     * Defines the requirements for the UI component that displays updates for this login task
     */
    interface LoginFieldContainer {
        /**
         * Called when the login task has either succeeded or failed
         * @param success true if login was successful. False if password was wrong or some other error
         * @param userNameText user name we tried to log in with
         * @param passwordText password we tried to log in with
         */
        fun onLogin(success: Boolean?, userNameText: String, passwordText: String)

        /**
         * Called when the login task has been cancelled (not when it fails)
         */
        fun onLoginCancelled()

        /**
         * Returns a context object so we can persist data in sharedprefs/file
         */
        fun getApplicationContext(): Context
    }

    override fun doInBackground(vararg params: Void): Boolean? {
        val activity = uiWeakRef.get()
        activity?.let {
            return ChatSingleton.login(username = userName, password = password, silent = true, context = activity.getApplicationContext())
        }
        return false
    }

    override fun onPostExecute(success: Boolean?) {
        // maybe use rx to notify the activity instead?
        val activity = uiWeakRef.get()
        activity?.let {
            activity.onLogin(success = success, userNameText = userName, passwordText = password)
        }
    }

    override fun onCancelled() {
        val activity = uiWeakRef.get()
        activity?.let {
            activity.onLoginCancelled()
        }
    }
}
