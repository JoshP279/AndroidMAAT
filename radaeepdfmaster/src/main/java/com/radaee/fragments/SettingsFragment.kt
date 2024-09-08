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
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import com.radaee.activities.LogInActivity
import com.radaee.objects.RetrofitClient
import com.radaee.objects.SharedPref
import com.radaee.pdfmaster.R

class SettingsFragment : Fragment() {

    private lateinit var themeSwitch: Switch
    private lateinit var markingChoiceSpinner: Spinner
    private lateinit var settingsHelper: TextView
    private lateinit var logoutBtn: Button
    private var isFirstSelection = true

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_settings, container, false)
        themeSwitch = view.findViewById(R.id.theme_switch)
        markingChoiceSpinner = view.findViewById(R.id.markingstyle_spinner)
        settingsHelper = view.findViewById(R.id.settingsHelper)
        logoutBtn = view.findViewById(R.id.btnLogout)

        logoutBtn.setOnClickListener {
            logOut()
        }
        settingsHelper.setOnClickListener {
            displayHelperDialog()
        }
        setUpTheme()
        setUpMarkingChoiceSpinner()
        return view
    }


    private fun setUpMarkingChoiceSpinner() {
        val savedMarkingStyle = SharedPref.getString(requireContext(), "marking_style", null)
        val markingChoices: Array<String> = if (savedMarkingStyle == getString(R.string.marking_style1)) {
            arrayOf(getString(R.string.marking_style2), getString(R.string.marking_style1))
        } else {
            arrayOf(getString(R.string.marking_style1), getString(R.string.marking_style2))
        }
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, markingChoices)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        markingChoiceSpinner.adapter = adapter

        val savedPosition = markingChoices.indexOf(savedMarkingStyle)
        markingChoiceSpinner.setSelection(savedPosition)

        markingChoiceSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                if (isFirstSelection) {
                    isFirstSelection = false
                    return
                }
                val markerEmail = SharedPref.getString(requireContext(), "email", "")
                val selectedItem = parent.getItemAtPosition(position).toString()
                if (!markerEmail.isNullOrEmpty()) {
                    when (selectedItem) {
                        getString(R.string.marking_style1) -> {
                            SharedPref.saveString(requireContext(), "marking_style", getString(R.string.marking_style1))
                            RetrofitClient.updateMarkingStyle(requireContext(), requireView(),markerEmail, getString(R.string.marking_style1))
                        }
                        getString(R.string.marking_style2) -> {
                            SharedPref.saveString(requireContext(), "marking_style", getString(R.string.marking_style2))
                            RetrofitClient.updateMarkingStyle(requireContext(), requireView(), markerEmail, getString(R.string.marking_style2))
                        }
                    }
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun displayHelperDialog() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle(R.string.helperHeading)
        builder.setMessage(R.string.settingsHelperMessage)
        builder.setPositiveButton(resources.getString(R.string.ok)) { dialog, _ -> dialog.dismiss() }
        val alertDialog: AlertDialog = builder.create()
        alertDialog.setCancelable(false)
        alertDialog.show()
    }

    private fun setUpTheme() {
        val isSystemDarkMode = (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
        val isDarkModePref = SharedPref.getBoolean(requireContext(), "DARK_MODE", isSystemDarkMode)
        themeSwitch.isChecked = isDarkModePref
        themeSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
            SharedPref.saveBoolean(requireContext(), "DARK_MODE", isChecked)
        }
    }

    private fun logOut() {
        val intent = Intent(requireContext(), LogInActivity::class.java)
        startActivity(intent)
        activity?.finish()
    }
}
