package com.radaee.fragments

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
import com.radaee.objects.SharedPref
import com.radaee.pdfmaster.R
import java.util.Locale

/**
 * SettingsFragment is a fragment that allows the user to change the app's theme, language and marking style.
 */
class SettingsFragment : Fragment() {

    private lateinit var themeSwitch: Switch
    private lateinit var languageSpinner: Spinner
    private lateinit var markingChoiceSpinner: Spinner
    private lateinit var settingsHelper: TextView
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_settings, container, false)
        themeSwitch = view.findViewById(R.id.theme_switch)
        languageSpinner = view.findViewById(R.id.language_spinner)
        markingChoiceSpinner = view.findViewById(R.id.markingstyle_spinner)
        settingsHelper = view.findViewById(R.id.settingsHelper)
        settingsHelper.setOnClickListener{
            displayHelperDialog()
        }
        setUpTheme()
        setUpLanguageSpinner()
        setUpMarkingChoiceSpinner()
        return view
    }

    /**
     * Set the marking style spinner to the default value.
     */
    private fun setUpMarkingChoiceSpinner(){
        val markingChoices = arrayOf(getString(R.string.marking_style1))
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, markingChoices)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        markingChoiceSpinner.adapter = adapter
    }

    /**
     * Displays a dialog that provides information on how to use the settings fragment.
     */
    private fun displayHelperDialog() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle(R.string.helperHeading)
        builder.setMessage(R.string.settingsHelperMessage)
        builder.setPositiveButton(resources.getString(R.string.ok)) { dialog, _ -> dialog.dismiss() }
        val alertDialog: AlertDialog = builder.create()
        alertDialog.setCancelable(false)
        alertDialog.show()
    }

    /**
     * Set up the theme switch to the default value. Note that the default value is saved in @LogInActivity
     */
    private fun setUpTheme() {
        val isSystemDarkMode = (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
        val isDarkModePref = SharedPref.getBoolean(requireContext(),"DARK_MODE", isSystemDarkMode)
        themeSwitch.isChecked = isDarkModePref
        themeSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
            SharedPref.saveBoolean(requireContext(),"DARK_MODE", isChecked)
        }
    }

    /**
     * Set up the language spinner to the default value.
     */
    private fun setUpLanguageSpinner() {
        val languages = arrayOf("English", "Afrikaans")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, languages)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        languageSpinner.adapter = adapter
        languageSpinner.setSelection(SharedPref.getInt(requireContext(),"LANGUAGE", 0))
        languageSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent:AdapterView<*>, view: View?, position: Int, id: Long) {
                val currentLanguagePosition = SharedPref.getInt(requireContext(),"LANGUAGE", 0)
                if (position != currentLanguagePosition) {
                    val languageCode = when (position) {
                        1 -> "af"
                        else -> "en"
                    }
                    SharedPref.saveInt(requireContext(),"LANGUAGE", position)
                    setLocale(languageCode)
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    /**
     * Set the app's language to the selected language.
     */
    private fun setLocale(languageCode: String) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        val config = resources.configuration
        config.setLocale(locale)
        resources.updateConfiguration(config, resources.displayMetrics)
        activity?.recreate()
    }
}