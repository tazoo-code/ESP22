package com.example.esp22

import android.content.ContentValues.TAG
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

class SessionActivity : AppCompatActivity() {

    lateinit var arFragment: ArFragment

    var objRenderable: ModelRenderable? = null

    //Nome dell'oggetto dal quale sarà costruito il modello 3d
    lateinit var obj: String

    var isTouched : Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_session)

        //Toglie la barra sopra
        supportActionBar?.hide()

        //Riferimento a switchbutton per il cambio di modalità (Place model-->Delete model)
        val switchButton = findViewById<SwitchCompat>(R.id.switch1)

        val bottomSheet: LinearLayout = findViewById(R.id.bottom_sheet_layout)

        val bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet)

        //Viene passato il nome dell'oggetto da renderizzare la prima volta che viene eseguita questa Activity
        obj = intent.getStringExtra("nameObject").toString()

        //Evento per cambio modalità
        switchButton!!.setOnCheckedChangeListener { buttonView, isChecked ->

            if (isChecked) {
                switchButton!!.text = getString(R.string.delMode)
                isTouched = true
            } else {
                switchButton!!.text = getString(R.string.placeMode)
                isTouched = false
            }
        }

        //Riferimento al ArFragment
        arFragment = (supportFragmentManager.findFragmentById(R.id.arFragment) as ArFragment)

        //RecyclerView dello slider
        val recyclerView: RecyclerView = findViewById(R.id.slider_recycler_view)

        //Applica l'adapter alla recyclerView
        recyclerView.adapter = SliderAdapter(this.resources.getStringArray(R.array.object_array))

        //Listener per vedere quale oggetto da posizionare seleziono
        recyclerView.addOnItemTouchListener(
            RecyclerItemClickListener(
                applicationContext,
                object : RecyclerItemClickListener.OnItemClickListener {

                    override fun onItemClick(view: View?, position: Int) {

                        //In base alla posizione degli oggetti
                        when (position) {
                            0 -> obj = "lamp"
                            1 -> obj = "spada"
                            2 -> obj = "cuboRosso"
                            3 -> obj = "cuboWireframe"
                        }
                        setModel()
                    }
                })
        )

        //Listener per eliminare i nodi
        val delNode =
            Node.OnTouchListener { hitTestResult, motionEvent ->

                Log.d(TAG, "handleOnTouch");

                // Prima chiamata a ArFragment per gestire TrasformableNode
                arFragment.onPeekTouch(hitTestResult, motionEvent);

                //La rimozione si verifica con un evento ACTION_UP
                if (motionEvent.action == MotionEvent.ACTION_UP) {

                    if (hitTestResult.node != null && switchButton!!.isChecked) {

                        Log.d(TAG, "handleOnTouch hitTestResult.getNode() != null")

                        val hitNode: Node? = hitTestResult.node

                        arFragment.arSceneView.scene.removeChild(hitNode)

                        hitNode!!.parent = null
                        hitNode!!.renderable = null
                    }
                }
                true
            }

        //Configurazione sessione ArCore
        arFragment.apply {
            setOnSessionConfigurationListener { session, config ->
                if (session.isDepthModeSupported(Config.DepthMode.AUTOMATIC)) {
                    config.depthMode = Config.DepthMode.AUTOMATIC
                }
            }

            setOnViewCreatedListener { arSceneView ->
                // Available modes: DEPTH_OCCLUSION_DISABLED, DEPTH_OCCLUSION_ENABLED
                arSceneView.cameraStream.depthOcclusionMode =
                    CameraStream.DepthOcclusionMode.DEPTH_OCCLUSION_ENABLED

                // Use this mode if you want your objects to be more like if they where real
                arSceneView.lightEstimationConfig = LightEstimationConfig.REALISTIC

                // Use this mode if you want your objects to be more spectacular
                //arSceneView.lightEstimationConfig = LightEstimationConfig.SPECTACULAR
            }

            //Evento che si verifica quando viene toccato un piano
            arFragment.setOnTapArPlaneListener { hitResult, plane, motionEvent ->

                //Se siamo nella modalità place model
                if (!switchButton!!.isChecked) {


                    arFragment.arSceneView.scene.addChild(AnchorNode(hitResult.createAnchor()).apply {

                        // Crea il transformable model e lo aggiunge all'anchor
                        addChild(TransformableNode(arFragment.transformationSystem).apply {
                            setModel()

                            renderable = objRenderable

                            /*Associo al nodo il listener che elimina il nodo quando
                              viene cliccato nella modalita delete model
                            */
                            setOnTouchListener(delNode)

                            select()

                            //RenderableInstance(transform provider,renderable)
                            // Add child model relative the a parent model
                            addChild(Node().apply {

                                // Definizione posizione relativa
                                localPosition = Vector3(0.0f, 1f, 0.0f)

                                // Definizione scala relativa
                                localScale = Vector3(0.7f, 0.7f, 0.7f)
                                //renderable = modelView
                            })
                            //renderableInstance.animate(true).start()
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
                    //Nothing
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
                    BottomSheetBehavior.STATE_DRAGGING -> {
                        //
                    }
                    BottomSheetBehavior.STATE_HALF_EXPANDED -> {
                        //
                    }
                    BottomSheetBehavior.STATE_HIDDEN -> {
                        //
                    }
                    BottomSheetBehavior.STATE_SETTLING -> {
                        //
                    }
                }
            }
        }
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
        //obj = intent.getStringExtra("nameObject").toString()
    }

    //Crea il modello 3d che sarà renderizzato nello spazio 3D
    private fun setModel() {

        Log.i("OBJ",obj)

        ModelRenderable.builder()
            .setSource(this, Uri.parse("models/"+obj+".glb"))
            .setIsFilamentGltf(true)
            .build()
            .thenAccept { model: ModelRenderable -> objRenderable = model }
            .exceptionally {
                val t = Toast.makeText(this, "Unable to load Cube model", Toast.LENGTH_SHORT)
                t.show()
                null
            }
        Log.i("OBJ","fine build")
    }
}
