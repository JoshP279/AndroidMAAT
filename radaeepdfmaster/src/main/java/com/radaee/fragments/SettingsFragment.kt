package com.radaee.fragments

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Switch
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import com.radaee.pdfmaster.R
import java.util.Locale

class SettingsFragment : Fragment() {

    private lateinit var themeSwitch: Switch
    private lateinit var languageSpinner: Spinner
    private lateinit var markingChoiceSpinner: Spinner
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var settingsHelper: TextView
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_settings, container, false)
        sharedPreferences = requireActivity().getPreferences(Context.MODE_PRIVATE)
        themeSwitch = view.findViewById(R.id.theme_switch)
        languageSpinner = view.findViewById(R.id.language_spinner)
        markingChoiceSpinner = view.findViewById(R.id.markingstyle_spinner)
        settingsHelper = view.findViewById(R.id.settingsHelper)
        settingsHelper.setOnClickListener{
            displayHelperDialog()
        }
        setUpTheme()
        setLanguageSpinner()
        setMarkingCHoiceSpinner()
        return view
    }
    private fun setMarkingCHoiceSpinner(){
        val markingChoices = arrayOf(getString(R.string.marking_style1))
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, markingChoices)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        markingChoiceSpinner.adapter = adapter
    }
    private fun displayHelperDialog() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle(R.string.helperHeading)
        builder.setMessage(R.string.assessmentsHelperMessage)
        builder.setPositiveButton(resources.getString(R.string.ok)) { dialog, _ -> dialog.dismiss() }
        val alertDialog: AlertDialog = builder.create()
        alertDialog.setCancelable(false)
        alertDialog.show()
    }
    private fun setUpTheme() {
        val isSystemDarkMode = (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
        val isDarkModePref = sharedPreferences.getBoolean("DARK_MODE", isSystemDarkMode)
        themeSwitch.isChecked = isDarkModePref
        themeSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
            sharedPreferences.edit().putBoolean("DARK_MODE", isChecked).apply()
        }
    }
    private fun setLanguageSpinner() {
        val languages = arrayOf("English", "Afrikaans")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, languages)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        languageSpinner.adapter = adapter
        languageSpinner.setSelection(sharedPreferences.getInt("LANGUAGE", 0))
        languageSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent:AdapterView<*>, view: View?, position: Int, id: Long) {
                val currentLanguagePosition = sharedPreferences.getInt("LANGUAGE", 0)
                if (position != currentLanguagePosition) {
                    val languageCode = when (position) {
                        1 -> "af"
                        else -> "en"
                    }
                    val editor: SharedPreferences.Editor = sharedPreferences.edit()
                    editor.putInt("LANGUAGE", position)
                    editor.apply()
                    setLocale(languageCode)
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }
    private fun setLocale(languageCode: String) {
        var locale = Locale(languageCode)
        Locale.setDefault(locale)
        val config = resources.configuration
        config.setLocale(locale)
        resources.updateConfiguration(config, resources.displayMetrics)
        activity?.recreate()
    }
}