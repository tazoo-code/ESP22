package com.unipd.dei.esp22.appName

import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.CheckBox
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.preference.PreferenceManager

//Classe per Dialog delle info
class InfoDialogFragment : DialogFragment() {


    companion object {
        // Nomi delle key in shared preferences, per evitare errori di battitura
        const val DONT_SHOW_MAIN : String = "mainDontShow"
        const val DONT_SHOW_PLANES : String = "planesDontShow"
        const val DONT_SHOW_AUGM : String = "augmDontShow"

        // Nomi delle activity chiamanti, per evitare errori di battitura
        const val PLANES_ACTIVITY : String = "PlaneDetectionActivity"
        const val MAIN_ACTIVITY : String = "MainActivity"
        const val AUGM_ACTIVITY : String = "AugmentedImagesActivity"
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            //Riferimento al builder per costruire AlertDialog
            val builder = AlertDialog.Builder(it)
            Log.i("InfoFragment", "Info riferito all'activity" + requireActivity().localClassName)

            // View del dialog
            val view : View = layoutInflater.inflate(R.layout.info_dialog, null)
            // Riferimento a checkbox
            val checkBox: CheckBox = view.findViewById(R.id.checkBoxMain)
            // Shared Preferences
            val prefs = PreferenceManager.getDefaultSharedPreferences(view.context)

            // Rende la checkbox selezionata in base al valore salvato in prefs
            when(requireActivity().localClassName) {
                PLANES_ACTIVITY -> {
                    if (prefs.getBoolean(DONT_SHOW_PLANES, false))
                        checkBox.isChecked = true
                }
                AUGM_ACTIVITY -> {
                    if (prefs.getBoolean(DONT_SHOW_AUGM, false))
                        checkBox.isChecked = true
                }
                MAIN_ACTIVITY -> {
                    if (prefs.getBoolean(DONT_SHOW_MAIN, false))
                        checkBox.isChecked = true
                }
            }

            builder.setView(view)

            val s: String
            //In base all'activity che ha chiamato questo fragment viene usato un messaggio di info diverso
            when (requireActivity().localClassName) {
                PLANES_ACTIVITY -> {
                    s = getString(R.string.info_session)
                    //Costruisce l'AlertDialog con il messaggio passato
                    builder.setMessage(s)
                        .setPositiveButton(R.string.ok){ _, _ ->
                            // Sovrascrive il valore in prefs con lo stato di checkbox
                            with(prefs.edit()) {
                                putBoolean(DONT_SHOW_PLANES, checkBox.isChecked)
                                apply()
                            }
                        }
                        .setTitle("Info")

                }
                AUGM_ACTIVITY -> {
                    s = getString(R.string.info_augmented)
                    //Costruisce l'AlertDialog con il messaggio passato
                    builder.setMessage(s)
                        .setPositiveButton(R.string.ok) { _, _ ->
                            with(prefs.edit()) {
                                // Sovrascrive il valore in prefs con lo stato di checkbox
                                putBoolean(DONT_SHOW_AUGM, checkBox.isChecked)
                                apply()
                            }
                        }
                        .setTitle("Info")


                }
                MAIN_ACTIVITY -> {
                    s = getString(R.string.info_main)
                    //Costruisce l'AlertDialog con il messaggio passato
                    builder.setMessage(s)
                        .setPositiveButton(R.string.ok) { _, _ ->
                            with(prefs.edit()) {
                                // Sovrascrive il valore in prefs con lo stato di checkbox
                                putBoolean(DONT_SHOW_MAIN, checkBox.isChecked)
                                apply()
                            }
                        }
                        .setTitle("Info")

                }
            }

            // Crea l'oggetto AlertDialog e lo ritorna
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }
}
