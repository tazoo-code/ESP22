package com.unipd.dei.esp22.appName

import android.app.Dialog
import android.content.Intent
import android.net.Uri
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

            val s: String
            //In base all'activity che ha chiamato questo fragment viene usato un messaggio di info diverso
            if (requireActivity().localClassName == "SessionActivity"){
                s = getString(R.string.info_session)
                //Costruisce l'AlertDialog con il messaggio passato
                builder.setMessage(s)
                    .setPositiveButton(R.string.ok
                    ) { dialog, id ->
                        //Azione in caso di pulsante premuto
                    }
                    .setTitle("Info")

            }else if(requireActivity().localClassName == "AugmentedImagesActivity"){
                s = getString(R.string.info_augmented)
                //Costruisce l'AlertDialog con il messaggio passato
                builder.setMessage(s)
                    .setPositiveButton(R.string.ok
                    ) { dialog, id ->
                        //Azione in caso di pulsante premuto
                    }
                    .setNegativeButton(R.string.download
                    ){ dialog, id ->
                        val viewIntent = Intent(
                            "android.intent.action.VIEW",
                            Uri.parse("https://cdn.discordapp.com/attachments/969613466300190725/984166272717717594/NewPlanet.zip")
                        )
                        startActivity(viewIntent)
                    }
                    .setTitle("Info")
            }



            // Crea l'oggetto AlertDialog e lo ritorna
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }
}
