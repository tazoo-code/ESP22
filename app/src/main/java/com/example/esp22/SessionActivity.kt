package com.example.esp22


import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.ar.core.Config
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.HitTestResult
import com.google.ar.sceneform.Node
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.CameraStream
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.ux.ArFragment
import com.google.ar.sceneform.ux.TransformableNode
import com.gorisse.thomas.sceneform.light.LightEstimationConfig
import com.gorisse.thomas.sceneform.lightEstimationConfig


class SessionActivity : AppCompatActivity() {
    //lateinit var object

    lateinit var arFragment: ArFragment

    lateinit var obj: String

    //TODO ricordarsi di cambiarlo se cambia il nome del pacchetto
    val PACKAGE_NAME = "com.example.esp22"

    var objRenderable: ModelRenderable? = null
    var isTouched : Boolean = false

    var switchButton :SwitchCompat?=null


    var anchorNodeList: MutableList<AnchorNode>?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_session)
        //Toglie la barra sopra
        supportActionBar?.hide();


        obj = intent.getStringExtra("nameObject").toString()

        switchButton = findViewById<SwitchCompat>(R.id.switch1)


        switchButton!!.setOnTouchListener(View.OnTouchListener { view, motionEvent ->

            if(!isTouched){
                isTouched=true
            }
            else{
                isTouched=false
            }

            false
        })

        switchButton!!.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->

            if (isChecked) {
                switchButton!!.text = getString(R.string.delMode)
                isTouched=true
            } else {
                switchButton!!.text = getString(R.string.placeMode)
                isTouched=false
            }
        })


        arFragment = (supportFragmentManager.findFragmentById(R.id.arFragment) as ArFragment)

        //RecyclerView dello slider
        val recyclerView: RecyclerView = findViewById(R.id.slider_recycler_view)

        //Applica l'adapter alla recyclerView
        recyclerView.adapter = SliderAdapter(this.resources.getStringArray(R.array.object_array))


        //Creo un listener per vedere che oggetto premo
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
            }

                Log.i("isTouched", obj +"and " + isTouched)


                    arFragment.setOnTapArPlaneListener { hitResult, plane, motionEvent ->

                        if (!isTouched) {

                            arFragment.arSceneView.scene.addChild(AnchorNode(hitResult.createAnchor()).apply {

                                // Create the transformable model and add it to the anchor.
                                addChild(TransformableNode(arFragment.transformationSystem).apply {
                                    setModel()

                                    renderable = objRenderable
                                    select()
                                    //RenderableInstance(transform provider,renderable)
                                    // Add child model relative the a parent model
                                    addChild(Node().apply {
                                        // Define the relative position
                                        localPosition = Vector3(0.0f, 1f, 0.0f)
                                        // Define the relative scale
                                        localScale = Vector3(0.7f, 0.7f, 0.7f)
                                        //renderable = modelView
                                    })
                                    //renderableInstance.animate(true).start()
                                })
                            })
                        }
                        else{

                            /*PROBLEMI: Bisogna ricavare il nodo con hitTestResult....hitResult crea il nodo associato al anchor...
                              ma non permette di ricavare un nodo già creato...con l'evento setOnTouchListener è possibile
                              ricavare un hitTestResult ma crasha l'applicazione quando si è nella modalità delete e si tocca
                              un piano...
                             */
                            arFragment.arSceneView.scene.removeChild(AnchorNode(hitResult.createAnchor()).apply{

                                this.anchor = null

                                this.renderable==null

                                removeChild(TransformableNode(arFragment.transformationSystem).apply {

                                    removeChild(Node().apply {

                                        this.setParent(null)
                                        this.setEnabled(false)
                                    })

                                })

                            })

                            /*arFragment.arSceneView.scene.setOnTouchListener { hitTestResult, motionEvent ->


                                arFragment.onPeekTouch(hitTestResult, motionEvent)

                                if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                                    val hitNode = hitTestResult.node as AnchorNode

                                    hitNode.anchor=null

                                    hitNode.setParent(null)

                                    hitNode.setEnabled(false)
                                }



                                false

                            }*/

                    }

            }


            val bottomSheet: LinearLayout = findViewById(R.id.bottom_sheet_layout)
            val bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet)

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

    private fun handleOnTouch(hitTestResult:HitTestResult, motionEvent:MotionEvent){

        arFragment.onPeekTouch(hitTestResult, motionEvent)

        val hitNode = hitTestResult.node as AnchorNode

        hitNode.anchor=null

        hitNode.setParent(null)

        hitNode.setEnabled(false)

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

        /*when (obj) {
            "cubo" -> ModelRenderable.builder()
                .setSource(this, Uri.parse("models/cube3.glb"))
                .setIsFilamentGltf(true)
                .build()
                .thenAccept { model: ModelRenderable -> objRenderable = model }
                .exceptionally {
                    val t = Toast.makeText(this, "Unable to load Cube model", Toast.LENGTH_SHORT)
                    t.show()
                    null
                }

            "spada" -> ModelRenderable.builder()
                .setSource(this, Uri.parse("models/spada.glb"))
                .setIsFilamentGltf(true)
                .build()
                .thenAccept { model: ModelRenderable -> objRenderable = model }
                .exceptionally {
                    val t = Toast.makeText(this, "Unable to load Cube model", Toast.LENGTH_SHORT)
                    t.show()
                    null
                }

            "cuboRosso" -> ModelRenderable.builder()
                .setSource(this, Uri.parse("models/cuboRosso.glb"))
                .setIsFilamentGltf(true)
                .build()
                .thenAccept { model: ModelRenderable -> objRenderable = model }
                .exceptionally {
                    val t = Toast.makeText(this, "Unable to load Cube model", Toast.LENGTH_SHORT)
                    t.show()
                    null
                }
            "vinitalyGLB" -> ModelRenderable.builder()
                .setSource(this, Uri.parse("models/vinitalyGLB.glb"))
                .setIsFilamentGltf(true)
                .build()
                .thenAccept { model: ModelRenderable -> objRenderable = model }
                .exceptionally {
                    val t = Toast.makeText(this, "Unable to load Cube model", Toast.LENGTH_SHORT)
                    t.show()
                    null
                }
            "omino" -> ModelRenderable.builder()
                .setSource(this, Uri.parse("models/omino.glb"))
                .setIsFilamentGltf(true)
                .build()
                .thenAccept { model: ModelRenderable -> objRenderable = model }
                .exceptionally {
                    val t = Toast.makeText(this, "Unable to load Cube model", Toast.LENGTH_SHORT)
                    t.show()
                    null
                }
        }*/
    }



}