package com.example.esp22


import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Configuration
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate.*


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {

        val preferences : SharedPreferences = androidx.preference.PreferenceManager.getDefaultSharedPreferences(this)

        when (preferences.getString("theme", "system_default")) {
            "light_theme" -> setDefaultNightMode(MODE_NIGHT_NO)
            "dark_theme" -> setDefaultNightMode(MODE_NIGHT_YES)
        }

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val settingsButton : ImageView = findViewById(R.id.settings_button)
        settingsButton.setOnClickListener{
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }

        val rendering3dButton : ImageView = findViewById(R.id.rendering_3d)
        rendering3dButton.setOnClickListener{
            val intent = Intent(this, SessionActivity::class.java)
            startActivity(intent)
        }


        val augmImagesButton : ImageView = findViewById(R.id.augmented_images)
        augmImagesButton.setOnClickListener{
            val intent = Intent(this, AugmentedImagesActivity::class.java)
            startActivity(intent)
        }


    }


    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        recreate()
    }
}