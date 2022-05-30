package com.example.esp22

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment


class InfoDialogFragment : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            // Use the Builder class for convenient dialog construction
            val builder = AlertDialog.Builder(it)
            Log.i("InfoFragment", requireActivity().localClassName)
            var s = ""
            if (requireActivity().localClassName == "SessionActivity"){
                s = getString(R.string.info_session)
            }else if(requireActivity().localClassName == "AugmentedImagesActivity"){
                s = getString(R.string.info_augmented)
            }

            builder.setMessage(s)
                .setPositiveButton(R.string.ok,
                    DialogInterface.OnClickListener { dialog, id ->
                        //START
                    })
                .setTitle("Info")

            // Create the AlertDialog object and return it
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }
}
