package com.example.esp22

import android.Manifest
import android.app.Activity
import android.content.ContentValues.TAG
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.camera2.*
import android.media.ImageReader
import android.opengl.GLSurfaceView
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import android.view.KeyCharacterMap
import android.view.Surface
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.ar.core.ArCoreApk
import com.google.ar.core.Session
import com.google.ar.core.SharedCamera
import java.util.*
import javax.microedition.khronos.egl.EGL10
import javax.microedition.khronos.egl.EGLConfig

class MainActivity : AppCompatActivity() {

    //View dell'openGl
    private var mGLView: GLSurfaceView? = null

    //Renderer dell'openGL
    //private var renderer: MyRenderer? = null

    var session : Session? = null

    //TextView che contiene fsp
    var fpsText : TextView? = null

    //Definizione delle variabili per l'accesso alla fotocamera

    //GL Surface utilizzata per l'immagine di anteprima della fotocamera
    private var surfaceView: GLSurfaceView? = null

    //Sessione ARCore che supporta la condivisione
    var sharedSession: Session?=null

    // Istanza sharedCamera, ottenuta da una sessione ARCore che supporta lo sharing.
    private var sharedCamera: SharedCamera? = null

    // Rappresentazione della telecamera collegata al dispositivo Android
    private var cameraDevice: CameraDevice? = null

    // ID della fotocamera usata da ARCore
    private var cameraId: String? = null

    // Per la gestione del servizio fotocamera
    private val cameraManager: CameraManager? =null

    // Sessione di acquisizione, per la cattura o rielaborazione delle immagini dalla fotocamera
    private var captureSession: CameraCaptureSession? = null

    /*Evita che vengano apportate modifiche alla sessione di acquisizione dopo l'apertura della
      fotocamera (CameraManager.open(...)) ma prima della sua attivazione */
    private var captureSessionChangesPossible = false;

    //Usato per le richieste di acquisizione dell'anteprima della fotocamera
    private var previewCaptureRequestBuilder : CaptureRequest.Builder? =null

    // Consente di accedere ai dati ricavati dal rendering di una superfice
    private var cpuImageReader: ImageReader? = null

    private var arcoreActive = false

    var isCameraPermissionsOk = false

    var cameraOpened = false

    val MY_CAMERA_REQUEST_CODE = 100

    //Looper Handler
    // Looper handler thread per apertura fotocamera
    private var backgroundThread: HandlerThread? = null

    // Looper handler per avvio callback
    private var backgroundHandler: Handler? = null

    companion object{

    }

    override fun onCreate(savedInstanceState: Bundle?) {

        //OpenGl Renderer
        mGLView = GLSurfaceView(this)

        //Usato per creare un thread per l'apertura della camera
        startBackgroundThread()

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

        /*
        surfaceView = findViewById(R.id.surfaceView);

        surfaceView!!.setPreserveEGLContextOnPause(true)
        surfaceView!!.setEGLContextClientVersion(2)
        surfaceView!!.setEGLConfigChooser(8, 8, 8, 8, 16, 0)
        surfaceView!!.setRenderer(this)
        surfaceView!!.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY)
        */

        /*Check della disponibilità
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED){

            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), MY_CAMERA_REQUEST_CODE);
        }*/

        //
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
        stopBackgroundThread()

    }

    // Verifica se ARCore è installato e usa la versione corrente
    private fun isARCoreSupportedAndUpToDate(): Boolean {

        return when (ArCoreApk.getInstance().checkAvailability(this)) {
            ArCoreApk.Availability.SUPPORTED_INSTALLED -> true
            ArCoreApk.Availability.SUPPORTED_APK_TOO_OLD, ArCoreApk.Availability.SUPPORTED_NOT_INSTALLED -> {
                try {

                    //Richiesta dell'installazione o aggiornamento di ARCore se c'è nè bisogno
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
                // Il dispositivo non supporta ARCore
                false

            ArCoreApk.Availability.UNKNOWN_CHECKING -> {
                false
                /*ARCore sta verificando la disponibilità con una query remota, questa funzione dovrebbe essere
                   richiamata dopo aver atteso 200 ms per determinare il risultato della query */
            }
            ArCoreApk.Availability.UNKNOWN_ERROR,  ArCoreApk.Availability.UNKNOWN_TIMED_OUT -> {

                false

                /*C'è stato un errore per verificare la disponibilità della AR, può essere dovuto al fatto che il
                dispositivo sia offline.
                //Gestione dell'errore in modo appropriato*/
            }
        }
    }

    //FORSE DA TOGLIERE PERCHE' NON FUNZIONA ALL'INTERNO DI OPENCAMERA
    private fun hasCameraPermission(activity: Activity?): Boolean {
        return (ContextCompat.checkSelfPermission(activity!!, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED)
    }

    //Controlla se sono necessari permessi per l'app, e li richiede se non ci sono
    private fun requestCameraPermission(activity: Activity?) {
        ActivityCompat.requestPermissions(activity!!, arrayOf(Manifest.permission.CAMERA), 0)
    }

    //Callback per la richiesta dei permessi della fotocamera
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

    //Metodo che crea e configura la sessione
    private fun createSession() {

        // Create a new ARCore session.
        //session = Session(this)

        //Creazione e configurazione della sessione
        sharedSession = Session(this, EnumSet.of(Session.Feature.SHARED_CAMERA))

        val config = com.google.ar.core.Config(sharedSession)

        //Aggiunta di specifiche per la configurazione

        sharedSession!!.configure(config)

        //Apertura della fotocamera e creazione di una sessione di acquiszione
        openCameraForSharing()
        waitUntilCameraCaptureSessionIsActive()
    }

    private fun openCameraForSharing() {

        //Controllo permessi della fotocamera
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestCameraPermission(this)
        }

        //shareCamera permette di condividere il controllo della fotocamera con ARCore
        sharedCamera = sharedSession!!.sharedCamera;

        //Id della fotocamera che usa ARCore
        cameraId = sharedSession!!.cameraConfig.cameraId

        // Wrap the callback in a shared camera callback.
        val wrappedCallback = sharedCamera!!.createARDeviceStateCallback(cameraDeviceCallback, backgroundHandler)

        // Riferimento al si stema di servizio della fotocamera
        val cameraManager = this.getSystemService(Context.CAMERA_SERVICE) as CameraManager

        Log.i("DEBUG","Id "+cameraId.toString())
        cameraManager.openCamera(
            cameraId!!,
            wrappedCallback,
            backgroundHandler)

        Log.i("DEBUG","Dato l'apertura di Id " + cameraId.toString())
    }

    //Callback che permette di ricevere aggiornamenti sullo stato della fotocamera
    private val cameraDeviceCallback: CameraDevice.StateCallback =
        object : CameraDevice.StateCallback() {

            override fun onOpened(cd: CameraDevice) {
                Log.i("CameraTag", "Camera device ID " + cd.id + " opened.")
                cameraDevice = cd
                cameraOpened = true
                captureSessionChangesPossible = true;
                createCameraCaptureSession()
            }

            override fun onClosed(cd: CameraDevice) {
                Log.i("CameraTag", "Camera device ID " + cd.id + " closed.")
                //this@MainActivity.cameraDevice = null
                //safeToExitApp.open()
            }

            override fun onDisconnected(cd: CameraDevice) {
                Log.w("CameraTag", "Camera device ID " + cd.id + " disconnected.")
                cd.close()
                //this@MainActivity.cameraDevice = null
            }

            override fun onError(cd: CameraDevice, error: Int) {
                Log.e("CameraTag", "Camera device ID " + cd.id + " error " + error)
                cd.close()
                //this@MainActivity.cameraDevice = null
                // Fatal error. Quit application.
                finish()
            }
        }

    private fun createCameraCaptureSession(){
        try {

            // Crea una richiesta di acquisizione compatibile con ARCore utilizzando `TEMPLATE_RECORD`
            if(cameraDevice == null){
                Log.e("Camera","cameraDevice is null")
            }

            cpuImageReader= ImageReader.newInstance(640,480,android.graphics.ImageFormat.YUV_420_888,16)

            //Imposta le superfici create dall'app, per ricevere immagini aggiuntive quando ARCore è attivo.
            sharedCamera!!.setAppSurfaces(this.cameraId, listOf(cpuImageReader!!.surface))

            previewCaptureRequestBuilder = cameraDevice!!.createCaptureRequest(CameraDevice.TEMPLATE_RECORD)

            // Costruisce una serie di superfici, a partire da quelle fornite da ARCore.
            val surfaceList: MutableList<Surface> = sharedCamera!!.arCoreSurfaces

            // (Optional) Add a CPU image reader surface.
            surfaceList.add(cpuImageReader!!.getSurface())

            // Add ARCore surfaces and CPU image surface targets.
            for (surface in surfaceList) {
                previewCaptureRequestBuilder!!.addTarget(surface)
            }

            // Wrap the callback in a shared camera callback.
            val wrappedCallback = sharedCamera!!.createARSessionStateCallback(cameraSessionStateCallback, backgroundHandler)

            //Crea una sessione di acquisizione per l'anteprima della fotocamera usando wrappedCallback
            cameraDevice!!.createCaptureSession(surfaceList, wrappedCallback, backgroundHandler)

        } catch (e: CameraAccessException) {
            Log.e(TAG, "CameraAccessException", e)
        }
    }

    //Callback che permette di ricevere gli aggiornamenti sullo stato di una sessione di acquisizione della telecamera.
    val cameraSessionStateCallback =
        object : CameraCaptureSession.StateCallback() {

        /*Chiamato quando ARCore configura per la prima volta la sessione do acquisizione, dopo
          l'inizializzazione dell'app, e ad ogni resume()*/
        override fun onConfigured(session: CameraCaptureSession) {
            captureSession = session
            setRepeatingCaptureRequest()
        }

        override fun onActive(session: CameraCaptureSession) {
            if(!arcoreActive){
                resumeARCore()
            }

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

    //Ripetizione della callback alla sessione di acquisizione della fotocamera
    val cameraCaptureCallback = object : CameraCaptureSession.CaptureCallback() {

        override fun onCaptureCompleted(
            session: CameraCaptureSession,
            request: CaptureRequest,
            result: TotalCaptureResult
        ) {
            //shouldUpdateSurfaceTexture.set(true);
        }
    }

    fun resumeARCore() {
        sharedSession!!.resume()
        arcoreActive = true
        //Imposta la callback della sessione di acquisizione in modalità AR.
        sharedCamera!!.setCaptureCallback(cameraCaptureCallback, backgroundHandler)
    }

    //Avvia background handler thread  per l'apertura della fotocamera (senza bloccare UI thread)
    private fun startBackgroundThread() {
        backgroundThread = HandlerThread("sharedCameraBackground")
        backgroundThread!!.start()
        backgroundHandler = Handler(backgroundThread!!.getLooper())
    }

    // Stop background handler thread.
    private fun stopBackgroundThread() {
        if (backgroundThread != null) {
            backgroundThread!!.quitSafely()
            try {
                backgroundThread!!.join()
                backgroundThread = null
                backgroundHandler = null
            } catch (e: InterruptedException) {
                Log.e(TAG, "Interrupted while trying to join background handler thread", e)
            }
        }
    }

    //Per attivare la fotocamera dopo 5ms
    @Synchronized
    private fun waitUntilCameraCaptureSessionIsActive() {
        while (!captureSessionChangesPossible) {
            try {
                Thread.sleep(5)
            } catch (e: InterruptedException) {
                Log.e(TAG, "Unable to wait for a safe time to make changes to the capture session", e)
            }
        }
    }

    //Richiede l'acquisizione di immagini (che si ripetono all'infinito) utilizzando la stessa sessione di acquisizione.
    fun setRepeatingCaptureRequest() {
        captureSession!!.setRepeatingRequest(
            previewCaptureRequestBuilder!!.build(), cameraCaptureCallback, backgroundHandler
        )
    }
}