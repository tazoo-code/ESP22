package com.example.esp22

import android.Manifest
import android.app.Activity
import android.content.ContentValues.TAG
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.hardware.camera2.*
import android.hardware.camera2.params.OutputConfiguration
import android.hardware.camera2.params.SessionConfiguration
import android.media.Image
import android.media.ImageReader
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import android.util.TypedValue
import android.view.KeyCharacterMap
import android.view.Surface
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.Preview
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.ar.core.ArCoreApk
import com.google.ar.core.Config
import com.google.ar.core.Session
import com.google.ar.core.SharedCamera
import com.google.ar.sceneform.Scene
import java.nio.ByteBuffer
import java.util.*
import javax.microedition.khronos.egl.EGL10
import javax.microedition.khronos.egl.EGLConfig


class MainActivity : AppCompatActivity() {

    //View dell'openGl
    private var mGLView: GLSurfaceView? = null

    //Renderer dell'openGL
    private var renderer: MyRenderer? = null

    //var session : Session? = null

    //TextView che contiene fsp
    var fpsText : TextView? = null

    //Definizione delle variabili per l'accesso alla fotocamera

    //GL Surface utilizzata per l'immagine di anteprima della fotocamera
    private var surfaceView: GLSurfaceView? = null
    //private var surfaceHolder : SurfaceHolder? = null
    //private var mediaPlayer : MediaPlayer? = null

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
    private var combinedRequest : CaptureRequest.Builder? =null

    private var surfaceList : MutableList<Surface>? = null
    var imgView: ImageView? = null

    private var arcoreActive = false

    var isCameraPermissionsOk = false

    var cameraOpened = false

    val MY_CAMERA_REQUEST_CODE = 100

    var previewView : PreviewView? = null

    //Looper Handler
    // Looper handler thread per apertura fotocamera
    private var backgroundThread: HandlerThread? = null

    // Looper handler per avvio callback
    private var backgroundHandler: Handler? = null
    var ba : Bitmap? = null

    companion object{

        //Sessione ARCore che supporta la condivisione
        var sharedSession: Session?=null

        // Consente di accedere ai dati ricavati dal rendering di una superfice
        var cpuImageReader: ImageReader? = null

        var isAllReady = false

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

        //imgView = findViewById<ImageView>(R.id.imageView)


        //previewView = findViewById<PreviewView>(R.id.previewView)

        val button = findViewById<Button>(R.id.button)
        button.setOnClickListener {
            var toast = Toast.makeText(this,sharedCamera!!.arCoreSurfaces.toString()+ cpuImageReader!!.surface.toString(),Toast.LENGTH_SHORT)
            toast.show()

            ba = currentImageToBitmap()
            imgView!!.setImageBitmap(ba)
        }

        surfaceView = findViewById(R.id.surfaceView);

/*
        surfaceView = GLSurfaceView(this)

        surfaceView!!.setPreserveEGLContextOnPause(true)
        surfaceView!!.setEGLContextClientVersion(2)
        surfaceView!!.setEGLConfigChooser(8, 8, 8, 8, 16, 0)
        surfaceView!!.setRenderer(ImagePreviewRender(resources))
        surfaceView!!.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY)

*/
        //surfaceView = findViewById(R.id.surfaceView)
        //surfaceHolder = surfaceView!!.holder
        //surfaceHolder!!.addCallback(surfaceCallback)
        //Check della disponibilità fotocamera
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED){

            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), MY_CAMERA_REQUEST_CODE);
        }


        if(isARCoreSupportedAndUpToDate() /*&& isCameraPermissionsOk*/){
            createSession()
        }

         //Renderer
         renderer = MyRenderer()
         mGLView!!.setRenderer(renderer)

        //Imposto la view dell'openGL nel primo posto del linearLayout
        val l0 = findViewById<LinearLayout>(R.id.linearL0)
        //val l1 = findViewById<LinearLayout>(R.id.linearL1)
        l0.addView(mGLView,0)
        //l1.addView(surfaceView,0)

        //fpsText = findViewById(R.id.FpsTextView)

    }

    override fun onPause() {
        super.onPause()
        stopBackgroundThread()
        //mGLView!!.onPause()
    }

    override fun onResume() {
        super.onResume()
        startBackgroundThread()
        //mGLView!!.onResume()
    }

    override fun onStop() {
        super.onStop()
        stopBackgroundThread()

    }



    fun currentImageToBitmap(): Bitmap {
        var mImage = cpuImageReader!!.acquireLatestImage()
        if(cpuImageReader!!.acquireLatestImage() == null)
            Log.e("Image","Image null")
        var buffer: ByteBuffer = mImage.getPlanes().get(0).getBuffer()
        var bytes = ByteArray(buffer.remaining())
        buffer.get(bytes)


        //Save pixel values by converting to Bitmap first
        var ba = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        mImage.close()
        return ba
    }

    //-----------------------Metodi per la configurazione della sessione ARCore-----------------------

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

    //Metodo che crea e configura la sessione
    private fun createSession() {

        // Create a new ARCore session.
        //session = Session(this)

        //Creazione e configurazione della sessione
        sharedSession = Session(this, EnumSet.of(Session.Feature.SHARED_CAMERA))

        val config = com.google.ar.core.Config(sharedSession)

        config.planeFindingMode = Config.PlaneFindingMode.HORIZONTAL_AND_VERTICAL

        //Aggiunta di specifiche per la configurazione

        sharedSession!!.configure(config)

        //Apertura della fotocamera e creazione di una sessione di acquiszione
        openCameraForSharing()
        waitUntilCameraCaptureSessionIsActive()
    }


    //-------------------------Metodi per la configurazione della fotocamera--------------------------

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

            @RequiresApi(Build.VERSION_CODES.P)
            override fun onOpened(cd: CameraDevice) {
                Log.i("CameraTag", "Camera device ID " + cd.id + " opened.")
                cameraDevice = cd
                cameraOpened = true
                captureSessionChangesPossible = true;

                createCameraCaptureSession()
                //TODO strana cosa -> viene scritto
                //UpdateBugFixes on CameraConfigManager is unimplemented!
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

    @RequiresApi(Build.VERSION_CODES.P)
    private fun createCameraCaptureSession(){

        try {
            if(cameraDevice == null){
                Log.e("Camera","cameraDevice is null")
            }

            cpuImageReader= ImageReader.newInstance(640,480,android.graphics.ImageFormat.YUV_420_888,16)


            cpuImageReader!!.setOnImageAvailableListener({
                //val previewSurface = surfaceView.holder.surface

                cpuImageReader!!.setOnImageAvailableListener({
                    Log.d("camera","setOnImageAvailableListener")



                }, backgroundHandler)

            }, backgroundHandler)

            //val p = previewView!!.surfaceProvider

            //Imposta le superfici create dall'app, per ricevere immagini aggiuntive quando ARCore è attivo.
            sharedCamera!!.setAppSurfaces(cameraDevice!!.id,
                listOf(cpuImageReader!!.surface)
            )

            //previewCaptureRequestBuilder = cameraDevice!!.createCaptureRequest(CameraDevice.TEMPLATE_RECORD)

            // Costruisce una serie di superfici, a partire da quelle fornite da ARCore.
            surfaceList = sharedCamera!!.arCoreSurfaces
            surfaceList!!.removeAt(1)
            surfaceList!!.add(cpuImageReader!!.surface)
            //surfaceList!!.add()

            //TODO Capire perchè throw questo errore
            //E/SharedConfiguredCameraControlContext(0): Saw invalid request: CameraControlConfigureRequest(outputs=Camera2Outputs{api21Outputs=Api21Outputs{surfaces=[Surface(name=android.graphics.SurfaceTexture@cdfc04f)/@0x582126b, Surface(name=null)/@0x53f7337]}}, captureSessionConfigs=[]) in state: SHARED_CONFIGURED
            //2022-05-12 19:40:38.650 14580-14641/com.example.esp22



            // (Optional) Add a CPU image reader surface.
            //surfaceList!!.add(cpuImageReader!!.getSurface())

            // Add ARCore surfaces and CPU image surface targets.
            /*for (surface in surfaceList) {
                previewCaptureRequestBuilder!!.addTarget(surface)
            }*/

            // Wrap the callback in a shared camera callback.
            val wrappedCallback = sharedCamera!!.createARSessionStateCallback(cameraSessionStateCallback, backgroundHandler)

            //Crea una sessione di acquisizione per l'anteprima della fotocamera usando wrappedCallback
            val c = listOf(OutputConfiguration(0, surfaceList!![0]),OutputConfiguration(0, surfaceList!![1])) as MutableList

            val s = SessionConfiguration(
                SessionConfiguration.SESSION_REGULAR,
                c,
                mainExecutor,
                wrappedCallback


            )
            cameraDevice!!.createCaptureSession(s)

            //cameraDevice!!.createCaptureSession(surfaceList!!, wrappedCallback, backgroundHandler)
            Log.i(TAG,"Camera Device created Capture Session")

        } catch (e: CameraAccessException) {
            Log.e(TAG, "CameraAccessException", e)
        }
    }

    //Callback che permette di ricevere gli aggiornamenti sullo stato di una sessione di acquisizione della telecamera.
    val cameraSessionStateCallback = object : CameraCaptureSession.StateCallback() {

        /*Chiamato quando ARCore configura per la prima volta la sessione do acquisizione, dopo
          l'inizializzazione dell'app, e ad ogni resume()*/
        override fun onConfigured(session: CameraCaptureSession) {
            captureSession = session
            Log.d(TAG, "Camera capture configured.")


        }

        override fun onActive(session: CameraCaptureSession) {
            //Se la sessione di ArCore non è attiva la riattivo
            if(!arcoreActive){
                resumeARCore()
            }
            Log.d(TAG, "Camera capture active.")

            isAllReady = true
        }

        override fun onSurfacePrepared(
            session: CameraCaptureSession, surface: Surface
        ) { Log.d(TAG, "Camera capture surface prepared.")
        }

        override fun onReady(session: CameraCaptureSession) {
            Log.d(TAG, "Camera capture session ready.")
            val requestTemplate = CameraDevice.TEMPLATE_PREVIEW
            combinedRequest = cameraDevice!!.createCaptureRequest(requestTemplate)

            // Link the Surface targets with the combined request
            combinedRequest!!.addTarget(sharedCamera!!.arCoreSurfaces[0])
            combinedRequest!!.addTarget(cpuImageReader!!.surface)
            setRepeatingCaptureRequest()

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

        Log.i("Capture","Surface -> "+captureSession!!.inputSurface.toString()) //null
        Log.i("Capture","Surface -> "+surfaceList!![0].toString())
        //Log.i("Capture","Surface -> "+surfaceList!![1].toString())//null
        //Log.i("Capture","Surface -> "+cpuImageReader!!.surface.toString())//null

        Log.i("Capture","Surface -> "+sharedCamera!!.arCoreSurfaces[0].toString())


        //combinedRequest.addTarget(sharedCamera!!.arCoreSurfaces[1])
        //combinedRequest.addTarget(cpuImageReader!!.surface)

        Log.i("Capture","CaptureSession -> "+captureSession.toString())
        Log.i("Capture","PreviewCaptureRequestBuilder -> "+combinedRequest.toString())
        Log.i("Capture","cameraCaptureCallback -> "+ cameraCaptureCallback.toString())
        Log.i("Capture","BackgroundHandler -> "+backgroundHandler.toString())

        captureSession!!.setRepeatingRequest(
            combinedRequest!!.build(), cameraCaptureCallback, backgroundHandler )


    }


    fun setOnImageAvailable(reader: ImageReader) : Bitmap? {
        val image = cpuImageReader!!.acquireLatestImage()
        val camera = sharedSession!!.sharedCamera
        val texture = camera.surfaceTexture

        val preview = Preview.Builder().build()
         preview.setSurfaceProvider(previewView!!.surfaceProvider)



        //texture.updateTexImage()

        //texture.attachToGLContext(0)
        //GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture.)
        //GLES20.glReadPixels(0, 0, image.width, image.height, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, ib);



        if(sharedCamera!!.arCoreSurfaces[0].isValid){
            Log.i("Image","Surface Ar0 is valid")
            //MediaPlayer

        }

        val planes: Array<Image.Plane> = image.getPlanes()
        val yRowStride: Int = planes.get(0).getRowStride()

        val yImage = ByteArray(yRowStride)
        planes.get(0).getBuffer().get(yImage)
        //TODO Api 31 maledetto

        //val cs = ImageDecoder.createSource(yImage)
        val data = yImage
        val offset = 0
        val lenght = data.size

        val value = TypedValue()
        value.density = Bitmap.DENSITY_NONE
        val rect = image.cropRect
        //val decoder: ImageDecoder = scr.createImageDecoder()
        val inputS = data.inputStream()

        //val bm= BitmapFactory.decodeResourceStream(null, value, inputS, rect, null)

        //val bm = ImageDecoder.decodeBitmap(cs)

        val bm = BitmapFactory.decodeByteArray(yImage,0,yImage.size)
        Log.i("Bitmap", bm.toString())
        return bm
    }



    //-----------------------Classe annidata ImagePreviewRender-----------------------
/*
    private class ImagePreviewRender(resources: Resources) : GLSurfaceView.Renderer {

        private lateinit var textures: IntArray
        private val resources: Resources

        override fun onSurfaceCreated(gl: GL10, config: EGLConfig) {
            textures = IntArray(1)
            gl.glEnable(GL10.GL_TEXTURE_2D)
            gl.glEnableClientState(GL10.GL_VERTEX_ARRAY)
            gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY)
            gl.glGenTextures(1, textures, 0)
            gl.glBindTexture(GL10.GL_TEXTURE_2D, textures[0])
            gl.glTexParameterf(
                GL10.GL_TEXTURE_2D,
                GL10.GL_TEXTURE_MAG_FILTER,
                GL10.GL_LINEAR.toFloat()
            )
            gl.glTexParameterf(
                GL10.GL_TEXTURE_2D,
                GL10.GL_TEXTURE_MIN_FILTER,
                GL10.GL_LINEAR.toFloat()
            )
            gl.glTexParameterf(
                GL10.GL_TEXTURE_2D,
                GL10.GL_TEXTURE_WRAP_S,
                GL10.GL_CLAMP_TO_EDGE.toFloat()
            )
            gl.glTexParameterf(
                GL10.GL_TEXTURE_2D,
                GL10.GL_TEXTURE_WRAP_T,
                GL10.GL_CLAMP_TO_EDGE.toFloat()
            )


        }


        override fun onSurfaceChanged(gl: GL10, width: Int, height: Int) {
            gl.glViewport(0, 0, width, height)
        }

        override fun onDrawFrame(gl: GL10) {
            gl.glClear(GL10.GL_COLOR_BUFFER_BIT or GL10.GL_DEPTH_BUFFER_BIT)

            if(isAllReady){
                Log.i("ImagePreview","yo")
                GLUtils.texImage2D(
                    GL10.GL_TEXTURE_2D,
                    0,
                    currentImageToBitmap(),
                    0
                )
                gl.glActiveTexture(GL10.GL_TEXTURE0)
                gl.glBindTexture(GL10.GL_TEXTURE_2D, textures[0])
                gl.glVertexPointer(3, GL10.GL_FLOAT, 0, VERTEX_BUFFER)
                gl.glTexCoordPointer(2, GL10.GL_FLOAT, 0, TEXCOORD_BUFFER)
                gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 0, 4)

            }

        }

        companion object {
            private val VERTEX_COORDINATES = floatArrayOf(
                -1.0f, +1.0f, 0.0f,
                +1.0f, +1.0f, 0.0f,
                -1.0f, -1.0f, 0.0f,
                +1.0f, -1.0f, 0.0f
            )
            private val TEXTURE_COORDINATES = floatArrayOf(
                0.0f, 0.0f,
                1.0f, 0.0f,
                0.0f, 1.0f,
                1.0f, 1.0f
            )
            private val TEXCOORD_BUFFER: Buffer =
                ByteBuffer.allocateDirect(TEXTURE_COORDINATES.size * 4)
                    .order(ByteOrder.nativeOrder()).asFloatBuffer().put(TEXTURE_COORDINATES)
                    .rewind()
            private val VERTEX_BUFFER: Buffer =
                ByteBuffer.allocateDirect(VERTEX_COORDINATES.size * 4)
                    .order(ByteOrder.nativeOrder()).asFloatBuffer().put(VERTEX_COORDINATES).rewind()
        }


        private fun currentImageToBitmap(): Bitmap? {
            var mImage = cpuImageReader!!.acquireLatestImage()

            var buffer: ByteBuffer = mImage.getPlanes().get(0).getBuffer()
            var bytes = ByteArray(buffer.remaining())
            buffer.get(bytes)
            mImage.close()

            //Save pixel values by converting to Bitmap first
            return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        }

        init {
            this.resources = resources
        }
    }

 */
    //-----------------------Fine Classe annidata ImagePreviewRender---------------------

}



