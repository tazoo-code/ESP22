package com.unipd.dei.esp22.appName


import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Configuration
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate.*
import com.unipd.dei.esp22.appName.AugmentedImagesActivity
import java.util.*


class MainActivity : AppCompatActivity() {

    override fun attachBaseContext(newBase: Context) {
        val currentLang = "it"
        newBase.resources.configuration.setLocale(Locale(currentLang))
        applyOverrideConfiguration(newBase.resources.configuration)

        super.attachBaseContext(newBase)
    }

    override fun onCreate(savedInstanceState: Bundle?) {

        val preferences : SharedPreferences = androidx.preference.PreferenceManager.getDefaultSharedPreferences(this)

        //Imposta il tema dell'applicazione in base alla configurazione del sistema
        when (preferences.getString("theme", "system_default")) {
            "light_theme" -> setDefaultNightMode(MODE_NIGHT_NO)
            "dark_theme" -> setDefaultNightMode(MODE_NIGHT_YES)
        }

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //In caso della pressione del tasto setting viene avviata l'activity delle impostazioni
        val settingsButton : ImageView = findViewById(R.id.settings_button)
        settingsButton.setOnClickListener{
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }

        //In caso della pressione del tasto planeDetect viene avviata l'activity del plane detection
        val planeDetectButton : ImageView = findViewById(R.id.rendering_3d)
        planeDetectButton.setOnClickListener{
            val intent = Intent(this, SessionActivity::class.java)
            startActivity(intent)
        }

        //In caso della pressione del tasto augmImage viene avviata l'activity dell' augmented images
        val augmImagesButton : ImageView = findViewById(R.id.augmented_images)
        augmImagesButton.setOnClickListener{
            val intent = Intent(this, AugmentedImagesActivity::class.java)
            startActivity(intent)
        }


    }

    //In caso di configurazioni cambiate viene aggiornata l'activity
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        recreate()
    }
}