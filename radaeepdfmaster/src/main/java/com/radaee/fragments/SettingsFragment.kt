package com.radaee.fragments

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.Switch
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.radaee.activities.LogInActivity
import com.radaee.interfaces.HelpHandler
import com.radaee.objects.RetrofitClient
import com.radaee.objects.SharedPref
import com.radaee.pdfmaster.R

class SettingsFragment : Fragment(), HelpHandler {

    private lateinit var themeSwitch: Switch
    private lateinit var markingChoiceSpinner: Spinner
    private lateinit var logoutBtn: Button
    private lateinit var changePasswordBtn: Button
    private var isFirstSelection = true

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_settings, container, false)
        themeSwitch = view.findViewById(R.id.theme_switch)
        markingChoiceSpinner = view.findViewById(R.id.markingstyle_spinner)
        logoutBtn = view.findViewById(R.id.btnLogout)
        changePasswordBtn = view.findViewById(R.id.btnChangePassword)

        if (SharedPref.getBoolean(requireContext(), "OFFLINE_MODE", false)){
            changePasswordBtn.visibility = View.INVISIBLE
        }else{
            changePasswordBtn.visibility = View.VISIBLE
        }
        logoutBtn.setOnClickListener {
            logOut()
        }

        changePasswordBtn.setOnClickListener{
            showChangePasswordDialog()
        }
        setUpTheme()
        setUpMarkingChoiceSpinner()

        return view
    }

    private fun setUpMarkingChoiceSpinner() {
        val savedMarkingStyle = SharedPref.getString(requireContext(), "marking_style", null)
        val markingChoices: Array<String> =
            if (savedMarkingStyle == getString(R.string.marking_style1)) {
                arrayOf(getString(R.string.marking_style2), getString(R.string.marking_style1))
            } else {
                arrayOf(getString(R.string.marking_style1), getString(R.string.marking_style2))
            }

        val adapter =
            ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, markingChoices)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        markingChoiceSpinner.adapter = adapter

        val savedPosition = markingChoices.indexOf(savedMarkingStyle)
        markingChoiceSpinner.setSelection(savedPosition)

        markingChoiceSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?,
                position: Int,
                id: Long
            ) {
                // Prevent triggering when fragment is recreated due to theme change
                if (isFirstSelection) {
                    isFirstSelection = false
                    return
                }

                // If the theme was changed, skip the spinner action
                val themeChanged = SharedPref.getBoolean(requireContext(), "THEME_CHANGED", false)
                if (themeChanged) {
                    SharedPref.saveBoolean(requireContext(), "THEME_CHANGED", false)
                    return
                }

                val markerEmail = SharedPref.getString(requireContext(), "email", "")
                val selectedItem = parent.getItemAtPosition(position).toString()

                if (!markerEmail.isNullOrEmpty()) {
                    when (selectedItem) {
                        getString(R.string.marking_style1) -> {
                            SharedPref.saveString(
                                requireContext(),
                                "marking_style",
                                getString(R.string.marking_style1)
                            )
                            RetrofitClient.updateMarkingStyle(
                                requireContext(),
                                requireView(),
                                markerEmail,
                                getString(R.string.marking_style1)
                            )
                        }

                        getString(R.string.marking_style2) -> {
                            SharedPref.saveString(
                                requireContext(),
                                "marking_style",
                                getString(R.string.marking_style2)
                            )
                            RetrofitClient.updateMarkingStyle(
                                requireContext(),
                                requireView(),
                                markerEmail,
                                getString(R.string.marking_style2)
                            )
                        }
                    }
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    override fun displayHelperDialog() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle(R.string.helperHeading)
        builder.setMessage(R.string.settingsHelperMessage)
        builder.setPositiveButton(resources.getString(R.string.ok)) { dialog, _ -> dialog.dismiss() }
        val alertDialog: AlertDialog = builder.create()
        alertDialog.setCancelable(false)
        alertDialog.show()
    }

    private fun setUpTheme() {
        val isSystemDarkMode =
            (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
        val isDarkModePref = SharedPref.getBoolean(requireContext(), "DARK_MODE", isSystemDarkMode)
        themeSwitch.isChecked = isDarkModePref

        themeSwitch.setOnCheckedChangeListener { _, isChecked ->
            val selectedMarkingStyle = markingChoiceSpinner.selectedItem.toString()
            SharedPref.saveString(requireContext(), "marking_style", selectedMarkingStyle)

            // Set a flag to indicate that the theme was changed
            SharedPref.saveBoolean(requireContext(), "THEME_CHANGED", true)

            // Reset isFirstSelection after theme change
            isFirstSelection = true

            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
            SharedPref.saveBoolean(requireContext(), "DARK_MODE", isChecked)
        }
    }
    /**
     * Function to display the Change Password dialog
     */
    private fun showChangePasswordDialog() {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_change_password, null)
        val currentPasswordLayout = dialogView.findViewById<TextInputLayout>(R.id.layoutCurrentPassword)
        val newPasswordLayout = dialogView.findViewById<TextInputLayout>(R.id.layoutNewPassword)
        val confirmPasswordLayout = dialogView.findViewById<TextInputLayout>(R.id.layoutConfirmPassword)

        val currentPasswordEditText = dialogView.findViewById<TextInputEditText>(R.id.etCurrentPassword)
        val newPasswordEditText = dialogView.findViewById<TextInputEditText>(R.id.etNewPassword)
        val confirmPasswordEditText = dialogView.findViewById<TextInputEditText>(R.id.etConfirmPassword)

        val dialogBuilder = AlertDialog.Builder(requireContext())
        dialogBuilder.setView(dialogView)
            .setTitle("Change Password")
            .setPositiveButton("Change", null)  // Set null initially to prevent auto-dismiss
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }

        val dialog = dialogBuilder.create()

        // Set custom click listener for the "Change" button to handle validation
        dialog.setOnShowListener {
            val changeButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            changeButton.setOnClickListener {
                val currentPassword = currentPasswordEditText.text.toString()
                val newPassword = newPasswordEditText.text.toString()
                val confirmPassword = confirmPasswordEditText.text.toString()

                // Clear previous errors
                currentPasswordLayout.error = null
                newPasswordLayout.error = null
                confirmPasswordLayout.error = null

                // Retrieve stored password and email from Shared Preferences
                val savedPassword = SharedPref.getString(requireContext(), "password", null)
                val markerEmail = SharedPref.getString(requireContext(), "email", null) ?: ""

                // Validate current password
                if (savedPassword != currentPassword) {
                    currentPasswordLayout.error = "This password is incorrect!"
                    return@setOnClickListener  // Prevent dialog from closing
                }

                // Validate new password and confirmation match
                if (newPassword != confirmPassword) {
                    confirmPasswordLayout.error = "Passwords do not match!"
                    return@setOnClickListener  // Prevent dialog from closing
                }

                // Validate new password length
                if (newPassword.length < 6) {
                    newPasswordLayout.error = "Password must be at least 6 characters long!"
                    return@setOnClickListener  // Prevent dialog from closing
                }

                RetrofitClient.updatePassword(
                    requireContext(),
                    requireView(),
                    markerEmail,
                    newPassword,
                    dialog
                )
            }
        }
        dialog.show()
    }

    /**
     * Helper function to show a simple message dialog
     */
    private fun showMessage(message: String) {
        AlertDialog.Builder(requireContext())
            .setMessage(message)
            .setPositiveButton("OK", null)
            .show()
    }


    private fun logOut() {
        val intent = Intent(requireContext(), LogInActivity::class.java)
        startActivity(intent)
        activity?.finish()
    }

    override fun onPause() {
        super.onPause()
        isFirstSelection = true
    }
}
