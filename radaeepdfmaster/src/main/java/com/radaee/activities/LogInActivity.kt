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
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.radaee.objects.RetrofitClient
import com.radaee.objects.SharedPref
import com.radaee.pdfmaster.R

/**
 * LogInActivity is the first activity that is launched when the app is opened.
 * It is responsible for handling the login process of the user, and also checks for the permissions required by the app.
 * Successful login will take the user to the MainActivity.
 */

class LogInActivity : AppCompatActivity() {
    private lateinit var loginBtn: Button
    private lateinit var offlineBtn: Button
    private lateinit var emailTextview: TextView
    private lateinit var passwordTextView: TextView
    private lateinit var headingTextView: TextView
    private lateinit var loginHelper: TextView

    companion object {
        private const val REQUEST_CODE_PERMISSIONS = 1001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_log_in)
        //Saves the dark mode preference of the user. If the user has enabled dark mode, it will be saved as true, else false. Will also be true if the user's operating system is in dark mode.
        SharedPref.saveBoolean(this,"DARK_MODE",(resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES)
        loginBtn = findViewById(R.id.loginBtn)
        offlineBtn = findViewById(R.id.btnOffline)
        emailTextview = findViewById(R.id.loginEmail)
        passwordTextView = findViewById(R.id.loginPassword)
        headingTextView = findViewById(R.id.headingTxtView)
        loginHelper = findViewById(R.id.loginHelper)
        loginHelper.setOnClickListener {
            displayHelperDialog()
        }
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
            val email = emailTextview.text.toString()
            val password = passwordTextView.text.toString()
            RetrofitClient.attemptLogin(this, email, password)
        }
        checkSavedLogin()
        checkAndRequestPermissions()
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
            emailTextview.text = savedEmail
            passwordTextView.text = savedPassword

            if (checkEmptyInput()) { return }

        }
    }
    /**
     * Checks if the email and password fields are empty. If they are, it will display a toast message to the user.
     * @return true if any of the fields are empty, false otherwise
     */
    private fun checkEmptyInput(): Boolean {
        val emptyEmail = emailTextview.text.toString().isEmpty()
        val emptyPassword = passwordTextView.text.toString().isEmpty()
        return when {
            emptyEmail && emptyPassword -> {
                Toast.makeText(this, R.string.enter_email_address_and_password, Toast.LENGTH_SHORT).show()
                true
            }
            emptyEmail -> {
                Toast.makeText(this, R.string.enter_email_address, Toast.LENGTH_SHORT).show()
                true
            }
            emptyPassword -> {
                Toast.makeText(this, R.string.enter_password, Toast.LENGTH_SHORT).show()
                true
            }
            else -> false
        }
    }
    /**
     * Checks if the app has the required permissions. If the app does not have the required permissions, it will request the user to grant the permissions.
     */
    private fun checkAndRequestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                requestManageExternalStoragePermission()
            } else {
                requestReadWritePermissions()
            }
        } else {
            requestReadWritePermissions()
        }
    }
    /**
     * Requests the user to grant the permission to manage external storage. This permission is required to read and write files to the external storage.
     */
    private fun requestManageExternalStoragePermission() {
        val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
        intent.data = Uri.parse("package:$packageName")
        startActivityForResult(intent, REQUEST_CODE_PERMISSIONS)
    }
    /**
     * Requests the user to grant the permission to read and write to the external storage.
     */
    private fun requestReadWritePermissions() {
        val permissions = arrayOf(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.INTERNET
        )
        if (!hasReadWritePermissions()) {
            ActivityCompat.requestPermissions(this, permissions, REQUEST_CODE_PERMISSIONS)
        }
    }
    /**
     * Checks if the email or password fields are empty and shows appropriate messages.
     * @return true if any of the fields are empty, false otherwise
     */
    private fun hasReadWritePermissions(): Boolean {
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
    }
    /**
     * Checks if the app has the required internet permission.
     */
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                Toast.makeText(this, R.string.permissions_granted, Toast.LENGTH_SHORT).show()
            }
        }
    }
    /**
     * Checks if the user has granted the permission to manage external storage. If the user has granted the permission, it will request the user to grant the read and write permissions.
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                if (Environment.isExternalStorageManager()) {
                    requestReadWritePermissions()
                } else {
                    Toast.makeText(this, R.string.permissions_denied, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}