package com.unipd.dei.esp22.appName


import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Configuration
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate.*
import androidx.preference.PreferenceManager
import java.util.*


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {

        // Shared preferences
        val preferences : SharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)

        // Imposta il tema dell'applicazione in base alla configurazione del sistema
        when (preferences.getString("theme", "system_default")) {
            "light_theme" -> setDefaultNightMode(MODE_NIGHT_NO)
            "dark_theme" -> setDefaultNightMode(MODE_NIGHT_YES)
        }

        // Salva la preferenza per mostrare o meno l'info dialog
        if(!preferences.contains(InfoDialogFragment.DONT_SHOW_MAIN)){
            with (preferences.edit()) {
                putBoolean(InfoDialogFragment.DONT_SHOW_MAIN, false)
                commit()
            }
        }

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Se è stato selezionato il checkbox "don't show again", non viene mostrato l'info
        if(!preferences.getBoolean(InfoDialogFragment.DONT_SHOW_MAIN, false))
            InfoDialogFragment().show(supportFragmentManager,InfoDialogFragment.MAIN_ACTIVITY)

        // In caso della pressione del tasto setting viene avviata l'activity delle impostazioni
        val settingsButton : ImageView = findViewById(R.id.settings_button)
        settingsButton.setOnClickListener{
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }

        // In caso della pressione del tasto planeDetect viene avviata l'activity del plane detection
        val planeDetectButton : ImageView = findViewById(R.id.rendering_3d)
        planeDetectButton.setOnClickListener{
            val intent = Intent(this, PlaneDetectionActivity::class.java)
            startActivity(intent)
        }

        // In caso della pressione del tasto augmImage viene avviata l'activity dell'augmented images
        val augmImagesButton : ImageView = findViewById(R.id.augmented_images)
        augmImagesButton.setOnClickListener{
            val intent = Intent(this, AugmentedImagesActivity::class.java)
            startActivity(intent)
        }


    }

    // In caso di configurazioni cambiate viene aggiornata l'activity
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        recreate()
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