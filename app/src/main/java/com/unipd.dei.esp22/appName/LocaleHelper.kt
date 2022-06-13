package com.unipd.dei.esp22.appName

import android.content.Context
import androidx.preference.PreferenceManager
import java.util.*

// Classe per la gestione della lingua
class LocaleHelper {

    companion object {

        // Metodo per la scelta della lingua
        // Se la lingua è fissata (inglese o italiano) restituisce rispettivamente "en" o "it",
        // Se è impostata su system_default restituisce la lingua del sistema
        // Se la lingua del dispositivo non è italiano o inglese, imposta inglese di default
        fun chooseLanguage(context : Context) : String{
            // Prende il valore di language salvato in sharedpreferences
            val prefs = PreferenceManager.getDefaultSharedPreferences(context)
            if(!prefs.contains("language"))
            {
                with (prefs.edit()) {
                    putString("language", "system_default")
                    commit()
                }
            }

            var lang = when (prefs.getString("language", Locale.getDefault().language)) {
                "italian" -> "it"
                "english" -> "en"
                "system_default" -> Locale.getDefault().language
                else -> Locale.getDefault().language
            }

            // Se la lingua del dispositivo non è italiano o inglese, imposta inglese di default
            if (lang != "en" && lang != "it")
                lang = "en"

            return lang
        }
    }

}
