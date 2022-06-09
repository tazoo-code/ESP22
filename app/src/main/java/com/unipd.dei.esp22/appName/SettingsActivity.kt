package com.unipd.dei.esp22.appName

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate.*
import androidx.fragment.app.FragmentManager
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import java.util.*


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

    /*
        Quando la configurazione dell'applicazione viene cambiata (es: quando viene invocato
        il metodo setDefaultNightMode(...)), viene ricreata l'activity.
    */
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        recreate()
    }

    /*
        Fragment che contiene le preferenze. Si occupa anche di salvare le preferenze in
        SharedPreference (per accederci PreferenceManager.getDefaultSharedPreferences(Context))
    */
    class SettingsFragment : PreferenceFragmentCompat() {

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {

            setPreferencesFromResource(R.xml.root_preferences, rootKey)

            val pref = this.context?.let { PreferenceManager.getDefaultSharedPreferences(it) }
            val prefs = pref?.all

            findPreference<Preference>("language")!!.onPreferenceChangeListener =
                Preference.OnPreferenceChangeListener { preference: Preference, lang: Any ->
                    activity?.recreate()
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

    // Prima della creazione dell'activity viene scelta la lingua
    // in base alle impostazioni scelte
    override fun attachBaseContext(newBase: Context) {
        // Metodo per la scelta della lingua, passando il contesto attuale
        val lang = LocaleHelper.chooseLanguage(newBase)
        // Imposta la lingua al contesto
        newBase.resources.configuration.setLocale(Locale(lang))
        applyOverrideConfiguration(newBase.resources.configuration)

        super.attachBaseContext(newBase)
    }
}