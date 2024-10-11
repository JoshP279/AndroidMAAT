package com.radaee.activities

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.radaee.objects.RegexUtils
import com.radaee.objects.RetrofitClient
import com.radaee.objects.SharedPref
import com.radaee.pdfmaster.R
import com.radaee.objects.SnackbarUtil

/**
 * LogInActivity is the first activity that is launched when the app is opened.
 * It is responsible for handling the login process of the user, and also checks for the permissions required by the app.
 * Successful login will take the user to the MainActivity.
 */

class LogInActivity : AppCompatActivity() {
    private lateinit var loginBtn: Button
    private lateinit var offlineBtn: Button
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var headingTextView: TextView

    companion object {
        private const val REQUEST_CODE_MANAGE_STORAGE = 1002
        private const val REQUEST_CODE_STORAGE_PERMISSIONS = 1001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_log_in)
        //Saves the dark mode preference of the user. If the user has enabled dark mode, it will be saved as true, else false. Will also be true if the user's operating system is in dark mode.
        SharedPref.saveBoolean(this,"DARK_MODE",(resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES)
        loginBtn = findViewById(R.id.loginBtn)
        offlineBtn = findViewById(R.id.btnOffline)
        emailEditText = findViewById(R.id.loginEmail)
        passwordEditText = findViewById(R.id.loginPassword)
        headingTextView = findViewById(R.id.headingTxtView)
        //Highlights the first letter of the heading in blue color
        val spannableString = SpannableString(headingTextView.text)
        val colorSpan = ForegroundColorSpan(resources.getColor(R.color.colorPrimary, null))
        spannableString.setSpan(colorSpan, 0, 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        headingTextView.text = spannableString
        offlineBtn.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            SharedPref.saveBoolean(this,"OFFLINE_MODE",true)
            startActivity(intent)
        }
        loginBtn.setOnClickListener {
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()
            if (checkEmptyInput()) return@setOnClickListener
            if (!RegexUtils.isValidEmail(email)) {
                emailEditText.error =  getString(R.string.email_invalid)
                return@setOnClickListener
            }
            RetrofitClient.attemptLogin(this, email, password)
        }
        checkSavedLogin()
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_help -> {
                displayHelperDialog()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_help, menu)
        return true
    }

    /**
     * Displays a dialog box with a message that helps the user understand how to log in.
     */
    private fun displayHelperDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(R.string.helperHeading)
        builder.setMessage(R.string.logInHelperMessage)
        builder.setPositiveButton(R.string.ok) { dialog, _ -> dialog.dismiss() }
        val alertDialog: AlertDialog = builder.create()
        alertDialog.setCancelable(false)
        alertDialog.show()
    }
    /**
     * Checks if the user has saved their login credentials. If they have, it will automatically fill the email and password fields with the saved credentials from previous logins.
     */
    private fun checkSavedLogin() {
        val savedEmail = SharedPref.getString(this@LogInActivity,"email", null)
        val savedPassword = SharedPref.getString(this@LogInActivity,"password", null)
        if (!savedEmail.isNullOrEmpty() && !savedPassword.isNullOrEmpty()) {
            offlineBtn.visibility = View.VISIBLE
            emailEditText.setText(savedEmail)
            passwordEditText.setText(savedPassword)
            if (checkEmptyInput()) { return }
        }else{
            offlineBtn.visibility = View.INVISIBLE
        }
    }
    /**
     * Checks if the email and password fields are empty. If they are, it will display a SnackBar message to the user.
     * @return true if any of the fields are empty, false otherwise
     */
    private fun checkEmptyInput(): Boolean {
        val emptyEmail = emailEditText.text.toString().isEmpty()
        val emptyPassword = passwordEditText.text.toString().isEmpty()
        val rootView = findViewById<View>(android.R.id.content)
        return when {
            emptyEmail && emptyPassword -> {
                SnackbarUtil.showErrorSnackBar(rootView, getString(R.string.enter_email_address_and_password), this)
                true
            }
            emptyEmail -> {
                SnackbarUtil.showErrorSnackBar(rootView, getString(R.string.enter_email_address), this)
                true
            }
            emptyPassword -> {
                SnackbarUtil.showErrorSnackBar(rootView, getString(R.string.enter_password), this)
                true
            }
            else -> false
        }
    }
}