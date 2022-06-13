package com.unipd.dei.esp22.appName

import android.content.ContentValues.TAG
import android.content.Context
import android.content.SharedPreferences
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.core.content.res.ResourcesCompat
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.ar.core.Config
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.Node
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.CameraStream
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.ux.ArFragment
import com.google.ar.sceneform.ux.TransformableNode
import com.gorisse.thomas.sceneform.light.LightEstimationConfig
import com.gorisse.thomas.sceneform.lightEstimationConfig
import java.util.*

class PlaneDetectionActivity : AppCompatActivity() {

    //Fragment che gestisce le interazioni AR dell'utente
    private lateinit var arFragment: ArFragment

    //Oggetto sul quale verrà renderizzato il modello 3d
    private var objRenderable: ModelRenderable? = null

    //Array contenente il nome dei modelli
    private lateinit var stringArray : Array<String>

    //Nome dell'oggetto dal quale sarà costruito il modello 3d
    lateinit var obj: String

    private lateinit var modelAnimals: Array<ModelAnimal>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_plane_detection)

        //Recupero i nomi dei modelli dal file arrays.xml
        stringArray = this.resources.getStringArray(R.array.object_array)

        //Riferimento al ArFragment
        arFragment = (supportFragmentManager.findFragmentById(R.id.arFragment) as ArFragment)

        //RecyclerView dello slider
        val recyclerView: RecyclerView = findViewById(R.id.slider_recycler_view)

        //Riferimento a switchbutton per il cambio di modalità (Place model-->Delete model)
        val switchButton = findViewById<SwitchCompat>(R.id.switch1)

        //Riferimento al layout che contiene le immagini dei modelli da posizionare
        val bottomSheet: LinearLayout = findViewById(R.id.bottom_sheet_layout)

        //Riferimento all'immagine che permette di tornare alla home
        val homeButton: ImageView = findViewById(R.id.home_button_session)

        //Riferimento all'immagine che permette di ricevere dello info sul funzionamento dell'applicazione
        val infoButton : ImageView = findViewById(R.id.info_button_session)

        //Per rilevare il comportamento dello slider (quando è in evidenza oppure nascosto)
        val bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet)

        modelAnimals=Array(stringArray.size) {
            ModelAnimal(stringArray[it])
        }


        //Se il pulsante home viene premuto ritorna alla home
        homeButton.setOnClickListener {
            finish()
        }

        // Shared preferences
        val preferences : SharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)

        // Salva la preferenza per mostrare o meno l'info dialog
        if(!preferences.contains(InfoDialogFragment.DONT_SHOW_PLANES)){
            with (preferences.edit()) {
                putBoolean(InfoDialogFragment.DONT_SHOW_PLANES, false)
                commit()
            }
        }

        // Se è stato selezionato il checkbox "don't show again", non viene mostrato l'info
        if(!preferences.getBoolean(InfoDialogFragment.DONT_SHOW_PLANES, false))
            InfoDialogFragment().show(supportFragmentManager,InfoDialogFragment.PLANES_ACTIVITY)

        //Se il pulsante info viene premuto allora invoca il fragment con il messaggio di info
        infoButton.setOnClickListener{
            InfoDialogFragment().show(supportFragmentManager,InfoDialogFragment.PLANES_ACTIVITY)
        }

        //Evento per cambio modalità
        switchButton!!.setOnCheckedChangeListener { _, isChecked ->

            if (isChecked) {
                switchButton.text = getString(R.string.delMode)

            } else {
                switchButton.text = getString(R.string.placeMode)
            }
        }

        //Applica l'adapter alla recyclerView
        recyclerView.adapter = SliderAdapter(modelAnimals,this)

        //Listener per vedere quale oggetto da posizionare seleziono
        recyclerView.addOnItemTouchListener(
            RecyclerItemClickListener(
                applicationContext,
                object : RecyclerItemClickListener.OnItemClickListener {

                    override fun onItemClick(view: View?, position: Int) {

                        /* Le immagini dei modelli sullo slider sono disposti nello
                           stesso ordine nell'array. Dunque un click un item alla
                           posizione (p) corrisponde al nome del modello alla posizione
                           (p) nel stringArray.*/
                        obj = stringArray[position]
                        Log.i("Modello",obj)
                        setModel()

                    }
                })
        )

        //Listener per eliminare i nodi
        val delNode =
            Node.OnTouchListener { hitTestResult, motionEvent ->
                if(switchButton.isChecked){
                    Log.d(TAG, "handleOnTouch")

                    // Prima chiamata ad ArFragment per gestire TrasformableNode
                    arFragment.onPeekTouch(hitTestResult, motionEvent)

                    //La rimozione si verifica con un evento ACTION_UP
                    if (motionEvent.action == MotionEvent.ACTION_UP) {

                        if (hitTestResult.node != null && switchButton.isChecked) {

                            Log.d(TAG, "handleOnTouch hitTestResult.getNode() != null")

                            //Restituisce il nodo che è stato colpito dal hitTest
                            val hitNode: Node? = hitTestResult.node

                            hitNode!!.renderable = null

                            hitNode.parent = null

                            //Eliminazione di tutti i figli del nodo
                            val children =hitNode.children
                            if(children.isNotEmpty() && children != null){
                                for (i in 0 until children.size){
                                    children[i].renderable = null
                                }
                            }

                            arFragment.arSceneView.scene.removeChild(hitNode)
                        }
                    }
                }
                true
            }

        //Configurazione sessione ArCore
        arFragment.apply {
            setOnSessionConfigurationListener { session, config ->
                /*Se il dispositivo supporta depth API viene configurata
                  sessione ARCore che è in grado di rilevare la profondità */
                if (session.isDepthModeSupported(Config.DepthMode.AUTOMATIC)) {
                    config.depthMode = Config.DepthMode.AUTOMATIC
                }
            }
            setOnViewCreatedListener { arSceneView ->
                /*Se il dispositivo supporta la profondità viene attivata l'occlusione
                  degli oggetti virtuali (nascosti da oggetti reali)
                  Modalita disponibili: DEPTH_OCCLUSION_DISABLED, DEPTH_OCCLUSION_ENABLED */
                arSceneView.cameraStream.depthOcclusionMode =
                    CameraStream.DepthOcclusionMode.DEPTH_OCCLUSION_ENABLED

                // Modalità che configura la luce in modo che gli oggetti virtuali siano più reali
                arSceneView.lightEstimationConfig = LightEstimationConfig.REALISTIC
            }

            //Evento che si verifica quando viene toccato un piano
            arFragment.setOnTapArPlaneListener { hitResult, _, _ ->

                //Se siamo nella modalità place model
                if (!switchButton.isChecked) {

                    arFragment.arSceneView.scene.addChild(AnchorNode(hitResult.createAnchor()).apply {

                        // Crea il transformable model e lo aggiunge all'anchor
                        addChild(TransformableNode(arFragment.transformationSystem).apply {

                            setModel()

                            renderable = objRenderable

                            /*Associo al nodo il listener che elimina il nodo quando
                              viene cliccato nella modalita delete model*/
                            setOnTouchListener(delNode)
                            select()

                            if (renderableInstance!=null && renderableInstance.hasAnimations()){
                                renderableInstance.animate(true).start()
                            }

                            // Add child model relative the a parent model
                            addChild(Node().apply {

                                // Definizione posizione relativa
                                localPosition = Vector3(0.0f, 1f, 0.0f)

                                // Definizione scala relativa
                                localScale = Vector3(0.7f, 0.7f, 0.7f)
                                //renderable = modelView

                                localScale= Vector3(0.03f,0.03f,0.03f)

                            })
                        })
                    })
                }
            }

            //Rileva quando lo slider cambia di stato
            bottomSheetBehavior.addBottomSheetCallback(object :
                BottomSheetBehavior.BottomSheetCallback() {

                override fun onStateChanged(bottomSheet: View, i: Int) {
                    //cambia immagine alla freccia
                    changeArrow(i)
                }

                override fun onSlide(bottomSheet: View, slideOffset: Float) {
                    //Niente
                }
            })

            //Rileva il tocco della freccia, e cambia lo stato dello slider
            val arrow: ImageView = findViewById(R.id.gallery_arrow)
            arrow.setOnClickListener {
                when (bottomSheetBehavior.state) {
                    BottomSheetBehavior.STATE_EXPANDED -> bottomSheetBehavior.state =
                        BottomSheetBehavior.STATE_COLLAPSED
                    BottomSheetBehavior.STATE_COLLAPSED -> bottomSheetBehavior.state =
                        BottomSheetBehavior.STATE_EXPANDED
                    BottomSheetBehavior.STATE_DRAGGING -> {}

                    BottomSheetBehavior.STATE_HALF_EXPANDED -> {}

                    BottomSheetBehavior.STATE_HIDDEN -> {}

                    BottomSheetBehavior.STATE_SETTLING -> {}
                }
            }
        }
        obj = "cubo_rosso"
        setModel()
    }

    //Cambia la freccia del bottom sheet verso l'alto o verso il basso quando lo stato cambia
    fun changeArrow(state: Int) {
        val iv: ImageView = findViewById(R.id.gallery_arrow)
        var myDrawable: Drawable? = iv.drawable

        when (state) {
            //Se lo stato è EXPANDED, la freccia diventa verso il basso
            BottomSheetBehavior.STATE_EXPANDED -> {
                myDrawable = ResourcesCompat.getDrawable(
                    resources,
                    R.drawable.ic_baseline_keyboard_arrow_down_40,
                    null
                )
            }

            //Se lo stato è COLLAPSED, la freccia diventa verso l'alto
            BottomSheetBehavior.STATE_COLLAPSED -> {
                myDrawable = ResourcesCompat.getDrawable(
                    resources,
                    R.drawable.ic_baseline_keyboard_arrow_up_40,
                    null
                )
            }
        }
        iv.setImageDrawable(myDrawable)
    }

    override fun onResume() {
        super.onResume()
        arFragment.onResume()
    }

    override fun onPause() {
        super.onPause()
        arFragment.onPause()
    }

    // Prima della creazione dell'activity viene scelta la lingua
    // in base alle impostazioni scelte
    override fun attachBaseContext(newBase: Context) {
        // Metodo per la scelta della lingua, passando il contesto attuale
        val lang = LocaleHelper.chooseLanguage(newBase)
        // Imposta la lingua al contesto
        newBase.resources.configuration.setLocale(Locale(lang))
        applyOverrideConfiguration(newBase.resources.configuration)

        super.attachBaseContext(newBase)
    }

    //Crea il modello 3d che sarà renderizzato nello spazio 3D
    private fun setModel() {
        Log.i("OBJ",obj)
        val uri = Uri.parse("models/planeDetect/$obj.glb")

        ModelRenderable.builder()
            //.setAsyncLoadEnabled(true)
            .setSource(this, uri)
            .setIsFilamentGltf(true)
            .build()
            .thenAccept { model: ModelRenderable -> objRenderable = model }
            .exceptionally {
                val t = Toast.makeText(this, "Unable to load model", Toast.LENGTH_SHORT)
                t.show()
                null
            }
        Log.i("OBJ", "fine build")
    }
}

