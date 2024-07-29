package com.radaee.activities

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import com.radaee.comm.Global
import com.radaee.fragments.AboutFragment
import com.radaee.fragments.HelpFragment
import com.radaee.fragments.SettingsFragment
import com.radaee.fragments.ViewAssessmentsFragment
import com.radaee.pdfmaster.R
/*
    MainActivity is the main activity of the app. It is responsible for handling the navigation drawer and the fragments that are displayed in the app.
    The navigation drawer contains the following options:
    1. View Assessments
    2. Settings
    3. Help
    4. About
 */
class MainActivity : AppCompatActivity() {

    private lateinit var toggle: ActionBarDrawerToggle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        val navView: NavigationView = findViewById(R.id.navView)
        toggle = ActionBarDrawerToggle(this, drawerLayout, R.string.open, R.string.close)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
        // Display the ViewAssessmentsFragment when the app is launched, by default
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction().replace(R.id.content_frame, ViewAssessmentsFragment())
                .commit()
            navView.setCheckedItem(R.id.nav_assessments)
        }
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        //
        navView.setNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.nav_assessments -> supportFragmentManager.beginTransaction()
                    .replace(R.id.content_frame, ViewAssessmentsFragment()).commit()
                R.id.nav_settings -> supportFragmentManager.beginTransaction()
                    .replace(R.id.content_frame, SettingsFragment()).commit()
                R.id.nav_help -> supportFragmentManager.beginTransaction()
                    .replace(R.id.content_frame, HelpFragment()).commit()
                R.id.nav_about -> supportFragmentManager.beginTransaction()
                    .replace(R.id.content_frame, AboutFragment()).commit()
            }
            drawerLayout.closeDrawers()
            true
        }
    }

    /**
     * This method is called when the user selects an item from the navigation drawer.
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (toggle.onOptionsItemSelected(item)) {
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    /**
     * This method is called when the user presses the back button on the device.
     * @SupressLint("MissingSuperCall") is used to suppress the warning that the super.onBackPressed() method is not called.
     * This is intentional, because we want to show an alert dialog when the user presses the back button.
     * If the user presses "Yes" in the alert dialog, then the app will exit. Element "No" will dismiss the dialog.
     */
    @SuppressLint("MissingSuperCall")
    override fun onBackPressed() {
        AlertDialog.Builder(this@MainActivity)
            .setTitle(R.string.app_exit_header)
            .setMessage(R.string.app_exit_msg)
            .setCancelable(false)
            .setPositiveButton(R.string.yes) { dialog, _ ->
                dialog.dismiss()
                super.onBackPressed()
            }
            .setNegativeButton(R.string.no) { dialog, _ ->
                dialog.dismiss()
            }
            .create()
            .show()
    }
}