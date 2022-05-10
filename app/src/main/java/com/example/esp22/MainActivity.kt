package com.example.esp22

import android.Manifest
import android.app.Activity
import android.content.ContentValues.TAG
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.camera2.*
import android.media.ImageReader
import android.opengl.GLSurfaceView
import android.os.Build.VERSION_CODES.Q
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.KeyCharacterMap
import android.view.Surface
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.ar.core.ArCoreApk
import com.google.ar.core.ImageFormat
import com.google.ar.core.Session
import com.google.ar.core.SharedCamera
import com.threed.jpct.*
import java.util.*
import javax.microedition.khronos.egl.EGL10
import javax.microedition.khronos.egl.EGLConfig


class MainActivity : AppCompatActivity() {

    var fpsText : TextView? = null
    private var mGLView: GLSurfaceView? = null      //View dell'openGl
    //private var renderer: MyRenderer? = null       //Renderer dell'openGL

    val MY_CAMERA_REQUEST_CODE = 100
    var isCameraPermissionsOk = false

    var session : Session? = null

    // Camera device. Usato per modalità AR e non-AR
    private var cameraDevice: CameraDevice? = null

    // Gestione del servizio fotocamera
    private val cameraManager: CameraManager? =null

    // Istanza shareCamera, ottenuta da una sessione ARCore che supporta lo sharing.
    private var sharedCamera: SharedCamera? = null

    var sharedSession: Session?=null

    // Sessione di acquisizione, usata nella modalità ARCore e non ARCore
    private var captureSession: CameraCaptureSession? = null

    // ID della fotocamera usata da ARCore
    private var cameraId: String? = null

    //Looper handler.
    private var appHandler: Handler?= null;


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
        setContentView(R.layout.activity_main)

        //Check della disponibilità
        /*val availability = ArCoreApk.getInstance().checkAvailability(this)
        if(!availability.isSupported){
            //TODO agire in qualche modo quando l'applicazione non supporta ArCore
        }
        */
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED){

            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), MY_CAMERA_REQUEST_CODE);
        }


        if(isARCoreSupportedAndUpToDate() /*&& isCameraPermissionsOk*/){
            createSession()
        }




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
        //session = Session(this)

        sharedSession = Session(this, EnumSet.of(Session.Feature.SHARED_CAMERA))

        //SharedCamera.createARDeviceStateCallback(android.hardware.camera2.CameraDevice.StateCallback, android.os.Handler)
        //SharedCamera.createARSessionStateCallback(android.hardware.camera2.CameraCaptureSession.StateCallback, android.os.Handler)
        // Create a session config.
        val config = com.google.ar.core.Config(sharedSession)

        // Do feature-specific operations here, such as enabling depth or turning on
        // support for Augmented Faces.

        // Configure the session.
        sharedSession!!.configure(config)


        openCameraForSharing()
        //createCameraCaptureSession()



    }

    private fun openCameraForSharing() {

        //Controllo permessi
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestCameraPermission(this)
        }


        //shareCamera permette di condividere il controllo della fotocamera con ARCore
        sharedCamera =  sharedSession!!.sharedCamera;

        //Id della fotocamera che usa ARCore
        cameraId= sharedSession!!.cameraConfig.cameraId

        //TODO guarda sta riga
        //sharedCamera!!.setAppSurfaces(this.cameraId, listOf(imageReader.surface))


        // Wrap the callback in a shared camera callback.
        val wrappedCallback =  sharedCamera!!.createARDeviceStateCallback(cameraDeviceCallback, appHandler)

        // Store a reference to the camera system service.
        val cameraManager = this.getSystemService(Context.CAMERA_SERVICE) as CameraManager

        Log.i("DEBUG","Id"+cameraId.toString())
        cameraManager.openCamera(
            cameraId!!,
            wrappedCallback,
            appHandler)
    }

    //Callback che permette di ricevere aggiornamenti sullo stato della fotocamera
    private val cameraDeviceCallback: CameraDevice.StateCallback =
        object : CameraDevice.StateCallback() {

            override fun onOpened(cd: CameraDevice) {
                Log.d(TAG, "Camera device ID " + cd.id + " opened.")
                cameraDevice = cd
                createCameraCaptureSession()

            }

            override fun onClosed(cd: CameraDevice) {
                Log.d(TAG, "Camera device ID " + cd.id + " closed.")
                this@MainActivity.cameraDevice = null
                //safeToExitApp.open()
            }

            override fun onDisconnected(cd: CameraDevice) {
                Log.w(TAG, "Camera device ID " + cd.id + " disconnected.")
                cd.close()
                this@MainActivity.cameraDevice = null
            }

            override fun onError(cd: CameraDevice, error: Int) {
                Log.e(TAG, "Camera device ID " + cd.id + " error " + error)
                cd.close()
                this@MainActivity.cameraDevice = null
                // Fatal error. Quit application.
                finish()
            }
        }

        private fun createCameraCaptureSession() {

            try {
                // Create an ARCore-compatible capture request using `TEMPLATE_RECORD`.
                var previewCaptureRequestBuilder = cameraDevice!!.createCaptureRequest(CameraDevice.TEMPLATE_RECORD)

                // Build a list of surfaces, starting with ARCore provided surfaces.
                val surfaceList: MutableList<Surface> = sharedCamera!!.arCoreSurfaces

                // (Optional) Add a CPU image reader surface.
                //surfaceList.add(cpuImageReader.getSurface())

                // The list should now contain three surfaces:
                // 0. sharedCamera.getSurfaceTexture()
                // 1. …
                // 2. cpuImageReader.getSurface()

                // Add ARCore surfaces and CPU image surface targets.
                for (surface in surfaceList) {
                    previewCaptureRequestBuilder.addTarget(surface)
                }

                // Wrap the callback in a shared camera callback.
                val wrappedCallback = sharedCamera!!.createARSessionStateCallback(cameraSessionStateCallback, appHandler)

                // Create a camera capture session for camera preview using an ARCore wrapped callback.
                cameraDevice!!.createCaptureSession(surfaceList, wrappedCallback, appHandler)
            } catch (e: CameraAccessException) {
                Log.e(TAG, "CameraAccessException", e)
            }

        /*
            // Get list of ARCore created surfaces. Required for ARCore tracking.
            var test = sharedSession!!.sharedCamera.arCoreSurfaces

            Session.Feature.SHARED_CAMERA

            var surfaceList: MutableList<Surface> =  sharedSession!!.sharedCamera.arCoreSurfaces

            // (Optional) Add a custom CPU image reader surface on devices that support CPU image access.
            var cpuImageReader : ImageReader = ImageReader.newInstance(540,540,android.graphics.ImageFormat.YUV_420_888,30)
            surfaceList.add(cpuImageReader.getSurface());
            // Use callback wrapper.

            val wrappedCallback=sharedCamera!!.createARSessionStateCallback(cameraSessionStateCallback, appHandler)

            cameraDevice!!.createCaptureSession(
                surfaceList,
                wrappedCallback,
                appHandler)
            */
        }

    //Callback che permette di ricevere gli aggiornamenti sullo stato di una sessione di acquisizione della telecamera.
    val cameraSessionStateCallback = object : CameraCaptureSession.StateCallback() {

        // Called when ARCore first configures the camera capture session after
        // initializing the app, and again each time the activity resumes.
        override fun onConfigured(session: CameraCaptureSession) {
            captureSession = session
            //setRepeatingCaptureRequest()
        }

        override fun onActive(session: CameraCaptureSession) {
            /*if (arMode && !arcoreActive) {
                resumeARCore()
            }*/
        }

        override fun onSurfacePrepared(
            session: CameraCaptureSession, surface: Surface
        ) { Log.d(TAG, "Camera capture surface prepared.")
        }

        override fun onReady(session: CameraCaptureSession) {
            Log.d(TAG, "Camera capture session ready.")
        }

        override fun onCaptureQueueEmpty(session: CameraCaptureSession) {
            Log.w(TAG, "Camera capture queue empty.")
        }

        override fun onClosed(session: CameraCaptureSession) {
            Log.d(TAG, "Camera capture session closed.")
        }
        override fun onConfigureFailed(session: CameraCaptureSession) {
            Log.e(TAG, "Failed to configure camera capture session.")
        }

    }


        fun hasCameraPermission(activity: Activity?): Boolean {
            return (ContextCompat.checkSelfPermission(activity!!, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED)
        }
        //Controlla se sono necessari permessi per l'app, e li richiede se non ci sono
        fun requestCameraPermission(activity: Activity?) {
            ActivityCompat.requestPermissions(activity!!, arrayOf(Manifest.permission.CAMERA), 0)
        }

    fun setRepeatingCaptureRequest() {

        /*captureSession.setRepeatingRequest(
            previewCaptureRequestBuilder.build(), cameraCaptureCallback, appHandler
        )*/
    }



}