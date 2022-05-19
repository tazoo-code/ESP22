package com.example.esp22


import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.ar.core.Config
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.Node
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.CameraStream
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.rendering.Renderable
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

    private var model: Renderable? = null
    var objRenderable: ModelRenderable? = null
    var positionClicked :Int = -1





    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_session)

        obj = intent.getStringExtra("nameObject").toString()

        //savedInstanceState!!.putString("nameObject",obj)

        Log.d("obj",obj)

        arFragment = (supportFragmentManager.findFragmentById(R.id.arFragment) as ArFragment)

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

            arFragment.setOnTapArPlaneListener { hitResult, plane, motionEvent ->


                arFragment.arSceneView.scene.addChild(AnchorNode(hitResult.createAnchor()).apply {

                    setModel()

                    // Create the transformable model and add it to the anchor.
                    addChild(TransformableNode(arFragment.transformationSystem).apply {

                        renderable = objRenderable
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
        }

        //RecyclerView dello slider
        val recyclerView: RecyclerView = findViewById(R.id.slider_recycler_view)
        // TODO prendere/salvare le preview
        //val list = arrayOf("1", "2", "3", "4", "5")

        //Applica l'adapter alla recyclerView
        recyclerView.adapter = SliderAdapter(this.resources.getStringArray(R.array.object_array))

        //Creo un listener per vedere che oggetto premo
        recyclerView.addOnItemTouchListener( RecyclerItemClickListener(
            applicationContext,
            object : RecyclerItemClickListener.OnItemClickListener {

                override fun onItemClick(view: View?, position: Int) {

                    //In base alla posizione degli oggetti
                    when(position){
                        0->obj = "lamp"
                        1->obj ="spada"
                        2->obj ="cuboRosso"
                        3->obj ="cuboWireframe"
                    }

                }
            }))



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

        obj = intent.getStringExtra("nameObject").toString()

        arFragment.setOnTapArPlaneListener { hitResult, plane, motionEvent ->

            /*
            setModel()

            val anchor = hitResult.createAnchor()
            val anchorNode = AnchorNode(anchor)
            val tn = TransformableNode(arFragment.transformationSystem)
            tn.parent = anchorNode
            tn.renderable = cubeRenderable
            tn.select()

            */
            arFragment.arSceneView.scene.addChild(AnchorNode(hitResult.createAnchor()).apply {

                setModel()

                // Create the transformable model and add it to the anchor.
                addChild(TransformableNode(arFragment.transformationSystem).apply {

                    renderable = objRenderable
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
    }


    private fun setModel() {

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