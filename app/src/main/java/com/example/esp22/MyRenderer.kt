package com.example.esp22
import android.graphics.BitmapFactory
import android.graphics.Color
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import android.util.Log
import android.widget.TextView
import com.google.ar.core.*
import com.google.ar.core.Camera
import com.threed.jpct.*
import com.threed.jpct.util.MemoryHelper
import java.nio.ByteBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10
import com.threed.jpct.Camera as Cam


class MyRenderer : GLSurfaceView.Renderer {

    var sharedSession: Session? = null

    var fb: FrameBuffer? = null
    private var world: World? = null        //Mondo in cui sono istanziati gli oggetti
    private var back = RGBColor(50, 50, 100)    //Colore dello sfondo

    private var cube: Object3D? = null
    private var pyramid: Object3D? = null
    //Fps
    var fps = 0
    var FpsText : TextView? = null
    //Sole
    var sun: Light? = null
    private lateinit var cam: Cam

    private var sunIntensityMultiplier = 1f     //Multiplicatore dell'intensità del sole
    private var sunPositionMultiplier = 100f


    //Variabile del tempo per contare gli fps
    private var time = System.currentTimeMillis()
    //TODO quando ArCore sarà apposto metterlo true
    private val isEnabled = false


    //Da mettere per forza perchè GLSurfaceView.Renderer ha 3 funzioni da implementare
    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig) {

    }

    //Funzione in cui si preparano gli oggetti3d, il mondo e la luce...
    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {

        fb?.dispose()
        fb = FrameBuffer(gl,width,height)


        //Inizializzo il mondo(genera lo spazio in 3d)
        world = World()

        //Imposto la luce base del mondo(cioè una luce di base che non genera ombre)
        world!!.setAmbientLight(20, 20, 20)

        //Creo una nuova luce e la aggiungo nel mondo
        sun = Light(world)

        //Intensità e colore della luce
        sun!!.setIntensity(250f, 250f, 250f)



        // Creo una texture. Si può usare anche un immagine
        //La texture è l'immagine che viene appiccicata sopra il cubo dandogli colore
        //TODO usare come texture un immagine
        var texture: Texture?


        val color = RGBColor(255, 255, 255)
        texture = Texture(64,64,color)
        if(!TextureManager.getInstance().containsTexture("texture")){
            TextureManager.getInstance().addTexture("texture",texture)
        }

        //Creo un cubo ed una piramide e gli scalo di 10
        cube = Primitives.getCube(10f)
        pyramid = Primitives.getPyramide(10f,0.5f)

        //Genero il cubo aggiungendo la texture e faccio la build
        if(cube != null){
            cube!!.calcTextureWrapSpherical()
            cube!!.setTexture("texture")
            cube!!.strip()
            cube!!.build()
        }

        //Stessa cosa per la piramide
        if(pyramid != null){
            pyramid!!.calcTextureWrapSpherical()
            pyramid!!.setTexture("texture")
            pyramid!!.strip()
            pyramid!!.build()
            //La sposto in alto
            pyramid!!.translate(0f,-15f,0f)
            //Imposto come padre della piramide il cubo così saranno copiati tutti i movimenti
            pyramid!!.addParent(cube)

        }

        //Aggiungo i due oggetti al modo
        world!!.addObject(cube)
        world!!.addObject(pyramid)


        //Mi seleziono la telecamera
        cam = world!!.camera
        //La muovo fuori dal cubo ad una velocità 50
        cam.moveCamera(Cam.CAMERA_MOVEOUT, 50f)
        //Imposto la telecamera che guardi il cubo
        cam.lookAt(cube!!.transformedCenter)

        //Qui si ricava un vettore di dove è posizionato il cubo...
        val sv = SimpleVector()
        sv.set(cube!!.transformedCenter)
        //e si muove 100 unità lungo l'asse y e z
        sv.y -= 100f
        sv.z -= 100f
        //e posiziona il sole in quel punto
        sun!!.position = sv

        //Comando utile per risparmiare la memoria
        MemoryHelper.compact()

    }


    //Funzione che gestisce i cambiamenti degli oggetti come rotazione e traslazione ecc.
    override fun onDrawFrame(gl: GL10?) {

        //Pulisco il buffer con il colore dello sfondo
        fb!!.clear(back)

        //TODO tirare fuori le variabili
        sharedSession = MainActivity.sharedSession

        if(sharedSession != null && isEnabled){

            val frame: Frame = sharedSession!!.update()
            val camera: Camera = frame.camera

            try{
                var mImage = MainActivity.cpuImageReader!!.acquireLatestImage()

                var buffer: ByteBuffer = mImage.getPlanes().get(0).getBuffer()
                var bytes = ByteArray(buffer.remaining())
                buffer.get(bytes)
                mImage.close()

                //Save pixel values by converting to Bitmap first
                var image = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                var x = image.width
                var y = image.height
                var intArray = IntArray(x * y)
                image.getPixels(intArray, 0, x, 0, 0, x, y)
                for (i in intArray.indices) {
                    intArray[i] = Color.red(intArray[i]) //Any colour will do
                }

                var fa = FloatArray(intArray.size)

                for (i in intArray){
                    fa[i] = intArray[i].toFloat()
                }

                var m = com.threed.jpct.Matrix()
                m.dump = fa

                cam!!.back = m

                //TextureManager.getInstance().
                //fb!!.blit()
            }catch (e: Exception){
                Log.w("Frame","Frame not aquired")
            }
            //frame.acquireCameraImage()
            if (camera.trackingState === TrackingState.TRACKING) {
                val cameraPose: Pose = camera.displayOrientedPose
                //Ottengo la direzione della jpct cam
                var direction = cam.direction.toArray()
                //Ruoto il vettore con la nuova direzione della camera di ArCore
                var newDir = cameraPose.rotateVector(direction)
                //Setto la nuova posizione
                cam.moveCamera(SimpleVector(newDir),50f)

                //Cambio la posizione della camera di jpct con la camera di arCore
                cam.position = SimpleVector(cameraPose.translation)

            }

            val light: LightEstimate =  frame.lightEstimate
            if(light.state == LightEstimate.State.VALID){
                var lightDirection = light.environmentalHdrMainLightDirection
                var lightIntensity = light.environmentalHdrMainLightIntensity
                //Intensità luce
                var intensity = SimpleVector(lightIntensity)
                //Lo scalo per un coefficente
                intensity.scalarMul(sunIntensityMultiplier)
                sun!!.intensity = SimpleVector(lightIntensity)

                //TODO sistemare la posizione in base alla direzione
                var sunPosition = SimpleVector(lightDirection)
                //Viene scalato di un valore
                sunPosition.scalarMul(sunPositionMultiplier)
                sun!!.position = sunPosition

            }
        }




        //Aggiorno il mondo con i nuovi cambiamenti
        world!!.renderScene(fb)
        world!!.draw(fb)
        fb!!.display()

        //Calcolo gli Fps
        if (System.currentTimeMillis() - time >= 1000) {
            Logger.log(fps.toString() + "fps")
            FpsText?.text = fps.toString()

            fps = 0
            time = System.currentTimeMillis()
        }
        //Boh...
        fps++
    }



}