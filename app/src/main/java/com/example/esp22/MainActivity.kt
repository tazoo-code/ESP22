package com.example.esp22

import android.content.ContentValues.TAG
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.KeyCharacterMap
import com.google.ar.core.ArCoreApk
import com.threed.jpct.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
        setContentView(R.layout.activity_main)
    }
}