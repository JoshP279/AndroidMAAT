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

class LogInActivity : AppCompatActivity() {
    private lateinit var loginBtn: Button
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
        SharedPref.saveBoolean(this,"DARK_MODE",(resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES)
        loginBtn = findViewById(R.id.loginBtn)
        emailTextview = findViewById(R.id.loginEmail)
        passwordTextView = findViewById(R.id.loginPassword)
        headingTextView = findViewById(R.id.headingTxtView)
        loginHelper = findViewById(R.id.loginHelper)
        loginHelper.setOnClickListener {
            displayHelperDialog()
        }
        val spannableString = SpannableString(headingTextView.text)
        val colorSpan = ForegroundColorSpan(resources.getColor(R.color.colorPrimary, null))
        spannableString.setSpan(colorSpan, 0, 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        headingTextView.text = spannableString
        loginBtn.setOnClickListener {
            val email = emailTextview.text.toString()
            val password = passwordTextView.text.toString()
            RetrofitClient.attemptLogin(this, email, password)
        }
        checkSavedLogin()
        checkAndRequestPermissions()
    }

    private fun displayHelperDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(R.string.helperHeading)
        builder.setMessage(R.string.logInHelperMessage)
        builder.setPositiveButton(R.string.ok) { dialog, _ -> dialog.dismiss() }
        val alertDialog: AlertDialog = builder.create()
        alertDialog.setCancelable(false)
        alertDialog.show()
    }

    private fun checkSavedLogin() {
        val savedEmail = SharedPref.getString(this@LogInActivity,"email", null)
        val savedPassword = SharedPref.getString(this@LogInActivity,"password", null)
        if (!savedEmail.isNullOrEmpty() && !savedPassword.isNullOrEmpty()) {
            emailTextview.text = savedEmail
            passwordTextView.text = savedPassword

            if (checkEmptyInput()) { return }

        }
    }

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

    private fun requestManageExternalStoragePermission() {
        val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
        intent.data = Uri.parse("package:$packageName")
        startActivityForResult(intent, REQUEST_CODE_PERMISSIONS)
    }

    private fun requestReadWritePermissions() {
        val permissions = arrayOf(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
        ActivityCompat.requestPermissions(this, permissions, REQUEST_CODE_PERMISSIONS)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                Toast.makeText(this, R.string.permissions_granted, Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, R.string.permissions_denied, Toast.LENGTH_SHORT).show()
            }
        }
    }

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