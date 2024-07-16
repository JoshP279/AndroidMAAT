package com.radaee.activities

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

class MainActivity : AppCompatActivity() {

    private lateinit var toggle: ActionBarDrawerToggle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Global.Init(this)
        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        val navView: NavigationView = findViewById(R.id.navView)

        toggle = ActionBarDrawerToggle(this, drawerLayout, R.string.open, R.string.close)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction().replace(R.id.content_frame, ViewAssessmentsFragment())
                .commit()
            navView.setCheckedItem(R.id.nav_assessments)
        }
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setTitle(R.string.full_app_name)
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

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (toggle.onOptionsItemSelected(item)) {
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}