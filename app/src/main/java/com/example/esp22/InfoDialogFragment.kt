package com.example.esp22

import android.app.Dialog
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment

class InfoDialogFragment : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            //Riferimento al builder per costruire AlertDialog
            val builder = AlertDialog.Builder(it)
            Log.i("InfoFragment", "Info riferito all'activity" + requireActivity().localClassName)

            var s = ""
            //In base all'activity che ha chiamato questo fragment viene usato un messaggio di info diverso
            if (requireActivity().localClassName == "SessionActivity"){
                s = getString(R.string.info_session)
            }else if(requireActivity().localClassName == "AugmentedImagesActivity"){
                s = getString(R.string.info_augmented)
            }

            //Costruisce l'AlertDialog con il messaggio passato
            builder.setMessage(s)
                .setPositiveButton(R.string.ok
                ) { dialog, id ->
                    //Azione in caso di pulsante premuto
                }
                .setTitle("Info")

            // Crea l'oggetto AlertDialog e lo ritorna
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }
}
