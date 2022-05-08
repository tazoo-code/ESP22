package com.example.esp22

import android.Manifest
import android.content.ContentValues.TAG
import android.content.pm.PackageManager
import android.opengl.GLSurfaceView
import android.os.Bundle
import android.util.Log
import android.view.KeyCharacterMap
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.ar.core.ArCoreApk
import com.google.ar.core.Session
import com.threed.jpct.*
import javax.microedition.khronos.egl.EGL10
import javax.microedition.khronos.egl.EGLConfig


class MainActivity : AppCompatActivity() {

    var fpsText : TextView? = null
    private var mGLView: GLSurfaceView? = null      //View dell'openGl
    //private var renderer: MyRenderer? = null       //Renderer dell'openGL
    val MY_CAMERA_REQUEST_CODE = 100
    var isCameraPermissionsOk = false

    var session : Session? = null
    companion object{

    }

    override fun onCreate(savedInstanceState: Bundle?) {


        //OpenGl Renderer
        mGLView = GLSurfaceView(this)

        //Impostazioni dell'Engine dell'OpenGl
        mGLView!!.setEGLConfigChooser(GLSurfaceView.EGLConfigChooser { egl, display ->

            // Ensure that we get a 16bit framebuffer. Otherwise, we'll fall
            // back to Pixelflinger on some device (read: Samsung I7500)
            val attributes = intArrayOf(EGL10.EGL_DEPTH_SIZE, 16, EGL10.EGL_NONE)
            val configs = arrayOfNulls<EGLConfig>(1)
            val result = IntArray(1)
            egl.eglChooseConfig(display, attributes, configs, 1, result)
            configs[0]!!
        })




        super.onCreate(savedInstanceState)


        //Check della disponibilità
        /*val availability = ArCoreApk.getInstance().checkAvailability(this)
        if(!availability.isSupported){
            //TODO agire in qualche modo quando l'applicazione non supporta ArCore
        }
        */

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            == PackageManager.PERMISSION_DENIED)
        {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), MY_CAMERA_REQUEST_CODE);
        }

        if(isARCoreSupportedAndUpToDate() && isCameraPermissionsOk){
            createSession()
        }



        setContentView(R.layout.activity_main)
        /*
        //Renderer
        renderer = MyRenderer()
        mGLView!!.setRenderer(renderer)

        //Imposto la view dell'openGL nel primo posto del linearLayout
        val l = findViewById<LinearLayout>(R.id.linearL)
        l.addView(mGLView,0)
        */


        //fpsText = findViewById(R.id.FpsTextView)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == MY_CAMERA_REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "camera permission granted", Toast.LENGTH_LONG).show()
                isCameraPermissionsOk = true
            } else {
                Toast.makeText(this, "camera permission denied", Toast.LENGTH_LONG).show()
                isCameraPermissionsOk = false
            }
        }
    }

    override fun onPause() {
        super.onPause()
        //mGLView!!.onPause()
    }

    override fun onResume() {
        super.onResume()
        //mGLView!!.onResume()
    }

    override fun onStop() {
        super.onStop()

    }

    // Verify that ARCore is installed and using the current version.
    private fun isARCoreSupportedAndUpToDate(): Boolean {
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

    private fun createSession() {
        // Create a new ARCore session.
        session = Session(this)

        // Create a session config.
        val config = com.google.ar.core.Config(session)

        // Do feature-specific operations here, such as enabling depth or turning on
        // support for Augmented Faces.

        // Configure the session.
        session!!.configure(config)
    }


}