package com.example.esp22

import android.content.res.Configuration
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate.*
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat


class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity)
        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.settings, SettingsFragment())
                .commit()
        }
        val back : ImageView = findViewById(R.id.settings_back_button)
        back.setOnClickListener{
            finish()
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        recreate()
    }

    class SettingsFragment : PreferenceFragmentCompat() {

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {

            setPreferencesFromResource(R.xml.root_preferences, rootKey)

            findPreference<Preference>("language")?.onPreferenceChangeListener =
                Preference.OnPreferenceChangeListener { preference: Preference, any: Any ->

                    true
                }

            findPreference<Preference>("theme")!!.onPreferenceChangeListener =
                Preference.OnPreferenceChangeListener { preference: Preference, theme: Any ->
                    when (theme) {
                        "dark_theme" -> setDefaultNightMode(MODE_NIGHT_YES)
                        "light_theme" -> setDefaultNightMode(MODE_NIGHT_NO)
                        "system_default" -> setDefaultNightMode(MODE_NIGHT_FOLLOW_SYSTEM)
                    }
                    true
                }
        }
    }
}