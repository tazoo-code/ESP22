package com.example.esp22

import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import android.view.KeyCharacterMap
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior.*
import com.google.ar.core.ArCoreApk


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        //RecyclerView dello slider
        val recyclerView : RecyclerView = findViewById(R.id.recycler_view)
        // TODO prendere/salvare le preview
        val list = arrayOf("1","2","3","4","5")
        //Applica l'adapter alla recyclerView
        recyclerView.adapter = GalleryAdapter(list)

        val bottomSheet : LinearLayout = findViewById(R.id.bottom_sheet_layout)
        val bottomSheetBehavior = from(bottomSheet)
        //Rileva quando lo slider cambia di stato
        bottomSheetBehavior.addBottomSheetCallback(object : BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, i: Int) {
                //cambia immagine alla freccia
                changeArrow(i)
            }
            override fun onSlide(bottomSheet: View, slideOffset: Float) { }
        })

        //Rileva il tocco della freccia, e cambia lo stato dello slider
        val arrow : ImageView = findViewById(R.id.gallery_arrow)
        arrow.setOnClickListener {
            when (bottomSheetBehavior.state) {
                STATE_EXPANDED -> bottomSheetBehavior.state = STATE_COLLAPSED
                STATE_COLLAPSED -> bottomSheetBehavior.state = STATE_EXPANDED
            }
        }



        //Check della disponibilità
        val availability = ArCoreApk.getInstance().checkAvailability(this)
        if(!availability.isSupported){
            //TODO agire in qualche modo quando l'applicazione non supporta ArCore
        }



        // ARCore requires camera permission to operate.

        /*if (!CameraPermissionHelper.hasCameraPermission(this)) {
            CameraPermissionHelper.requestCameraPermission(this)
            return
        }*/


        // Verify that ARCore is installed and using the current version.
        fun isARCoreSupportedAndUpToDate(): Boolean {
            return when (ArCoreApk.getInstance().checkAvailability(this)) {
                ArCoreApk.Availability.SUPPORTED_INSTALLED -> true
                ArCoreApk.Availability.SUPPORTED_APK_TOO_OLD, ArCoreApk.Availability.SUPPORTED_NOT_INSTALLED -> {
                    try {
                        // Request ARCore installation or update if needed.
                        when (ArCoreApk.getInstance().requestInstall(this, true)) {
                            ArCoreApk.InstallStatus.INSTALL_REQUESTED -> {
                                Log.i(TAG, "ARCore installation requested.")
                                false
                            }
                            ArCoreApk.InstallStatus.INSTALLED -> true
                        }
                    } catch (e: KeyCharacterMap.UnavailableException) {
                        Log.e(TAG, "ARCore not installed", e)
                        false
                    }
                }

                ArCoreApk.Availability.UNSUPPORTED_DEVICE_NOT_CAPABLE ->
                    // This device is not supported for AR.
                    false

                ArCoreApk.Availability.UNKNOWN_CHECKING -> {

                    false
                    // ARCore is checking the availability with a remote query.
                    // This function should be called again after waiting 200 ms to determine the query result.
                }
                ArCoreApk.Availability.UNKNOWN_ERROR,  ArCoreApk.Availability.UNKNOWN_TIMED_OUT -> {

                    false
                    // There was an error checking for AR availability. This may be due to the device being offline.
                    // Handle the error appropriately.
                }
            }
        }

        /*fun createSession() {
            // Create a new ARCore session.
            session = Session(this)

            // Create a session config.
            val config = Config(session)

            // Do feature-specific operations here, such as enabling depth or turning on
            // support for Augmented Faces.

            // Configure the session.
            session.configure(config)
        }*/

    }


    //Cambia la freccia del bottom sheet verso l'alto o verso il basso quando lo stato cambia
    fun changeArrow(state: Int){
        //Se lo stato è EXPANDED, la freccia diventa verso il basso
        if (state == STATE_EXPANDED) {
            val iv : ImageView = findViewById(R.id.gallery_arrow)
            val myDrawable = ResourcesCompat.getDrawable(resources, R.drawable.ic_baseline_keyboard_arrow_down_40, null)
            iv.setImageDrawable(myDrawable)
        }
        //Se lo stato è EXPANDED, la freccia diventa verso l'alto
        if (state == STATE_COLLAPSED){
            val iv : ImageView = findViewById(R.id.gallery_arrow)
            val myDrawable = ResourcesCompat.getDrawable(resources, R.drawable.ic_baseline_keyboard_arrow_up_40, null)
            iv.setImageDrawable(myDrawable)
        }
    }
}