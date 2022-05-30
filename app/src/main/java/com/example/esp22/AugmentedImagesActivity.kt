package com.example.esp22


import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.ar.core.*
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.Scene
import com.google.ar.sceneform.math.Quaternion
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.rendering.Renderable
import com.google.ar.sceneform.ux.ArFragment
import com.google.ar.sceneform.ux.TransformableNode


class AugmentedImagesActivity: AppCompatActivity() {

    private lateinit var arFragment: ArFragment

    private lateinit var database: AugmentedImageDatabase

    private var node : TransformableNode? = null

    private val listnode: MutableList<TransformableNode> = arrayListOf()


    private lateinit var namesobj : Array<String>

    private var renderobj: MutableList<Boolean> = arrayListOf()
    private var augimages: MutableList<AugmentedImage> = arrayListOf()
    var clearPressed = false

    private var rot = 0f
    private var count=0

    private var s: Session?=null


    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_augmented_images)

        //tv1 = findViewById<TextView>(R.id.tx1)

        namesobj= this.resources.getStringArray(R.array.planet_array)

        for( i in 0 until namesobj.size){
            renderobj.add(false)
        }

        val homeButton : ImageView = findViewById(R.id.home_button_augm)

        val infoButton :ImageView = findViewById(R.id.info_button_augmented)

        val clearButton = findViewById<Button>(R.id.clear)

        homeButton.setOnClickListener {
            finish()
        }

        infoButton.setOnClickListener{
            InfoDialogFragment().show(supportFragmentManager,"AugmentedImagesActivity")
        }



        clearButton!!.setOnClickListener(setOnClickListener)


        arFragment = (supportFragmentManager.findFragmentById(R.id.arFragment) as ArFragment)

        //Configurazione sessione ArCore
        arFragment.apply {
            setOnSessionConfigurationListener { session, config ->

                //TODO Risolvere se possibile il problema dell'auto facus
                //config.focusMode = Config.FocusMode.AUTO
                // Disable plane detection

                config.planeFindingMode = Config.PlaneFindingMode.DISABLED

                //database = AugmentedImageDatabase(session)

                database = assets.open("myimages.imgdb").use {
                    AugmentedImageDatabase.deserialize(session, it)
                }

                config.augmentedImageDatabase = database
                config.lightEstimationMode = Config.LightEstimationMode.DISABLED
                session.configure(config)

                //s=session
                // Check for image detection
                //arFragment.setOnAugmentedImageUpdateListener(onAugmentedImageTrackingUpdate)
                arFragment.getArSceneView().getScene().addOnUpdateListener(onUpdateFrame)

                val a = session.config
                val b = a.focusMode.name
                Log.i("Camera","Camerafocus -> $b")

            }
        }
    }

    private val setOnClickListener = View.OnClickListener { v->

        //Restart Activity
        val intent = intent
        finish()
        startActivity(intent)

    }

    override fun onPause() {
        super.onPause()
        arFragment.onPause()

    }
    override fun onResume() {
        super.onResume()
        arFragment.onResume()


    }


    private val onUpdateFrame = Scene.OnUpdateListener {
        val frame = arFragment.arSceneView.arFrame

        if (!listnode.isEmpty()) {
            for (n in listnode) {
                //Rotazione del nodo e quindi del modello
                if (rot >= 360) {
                    rot = 0f
                }
                rot++
                n.localRotation = Quaternion(Vector3(0f, rot, 0f))
            }
        }


        val augmentedImages = frame!!.getUpdatedTrackables(
            AugmentedImage::class.java
        )
        for (augmentedImage in augmentedImages) {

            if (augmentedImage.trackingState == TrackingState.TRACKING) {

                for (i in 0 until namesobj.size) {

                    if (augmentedImage.name.contains(namesobj[i]) && !renderobj[i]) {

                        Toast.makeText(this,""+namesobj[i]+" rilevato",Toast.LENGTH_SHORT).show()
                        if(namesobj[i]=="systemsolar"){
                            Toast.makeText(this,""+namesobj[i]+" rilevato",Toast.LENGTH_SHORT).show()
                            // here we got that image has been detected
                            // we will render our 3D asset in center of detected image
                            renderObject(
                                arFragment,
                                augmentedImage.createAnchor(augmentedImage.centerPose),
                                "orrery"
                            )

                            renderobj[i] = true
                        }else {
                            // here we got that image has been detected
                            // we will render our 3D asset in center of detected image
                            renderObject(
                                arFragment,
                                augmentedImage.createAnchor(augmentedImage.centerPose),
                                namesobj[i]
                            )
                            renderobj[i] = true
                        }
                    }
                }
            }
            if (augmentedImage.trackingState == TrackingState.PAUSED) {
                //--
            }
        }
    }
    private fun renderObject(fragment: ArFragment, anchor: Anchor, name: String) {
        ModelRenderable.builder()
            .setSource(this, Uri.parse("models/$name.glb"))
            .setIsFilamentGltf(true)
            .build()
            .thenAccept { renderable: ModelRenderable ->
                addNodeToScene(
                    fragment,
                    anchor,
                    renderable
                )
            }
            .exceptionally { throwable: Throwable ->
                val builder: android.app.AlertDialog.Builder = android.app.AlertDialog.Builder(this)
                builder.setMessage(throwable.message)
                    .setTitle("Error!")
                builder.create()?.show()
                null
            }
    }

    private fun addNodeToScene(fragment: ArFragment, anchor: Anchor, renderable: Renderable) {

        /*val anchorNode = AnchorNode(anchor)
        anchorNode.localScale = Vector3(0.1f,0.1f,0.1f)

        listnode!!.add(TransformableNode(fragment.transformationSystem))

        listnode!!.elementAt(count).renderable = renderable
        listnode!!.elementAt(count).parent = anchorNode
        //node.localScale = Vector3(0.05f,0.05f,0.05f)
        fragment.arSceneView.scene.addChild(anchorNode)
        //Posizione in riferimento alla foto
        listnode!!.elementAt(count).localPosition = Vector3(0f,0.5f,0f)
        listnode!!.elementAt(count).select()
        count++*/

        //listnode!!.add(node!!)

        val anchorNode = AnchorNode(anchor)
        anchorNode.localScale = Vector3(0.1f,0.1f,0.1f)

        node = TransformableNode(fragment.transformationSystem)


        node!!.renderable = renderable
        node!!.parent = anchorNode

        if(node!!.renderableInstance.hasAnimations()){
            node!!.renderableInstance.animate(true).start()
        }
        //node.localScale = Vector3(0.05f,0.05f,0.05f)
        fragment.arSceneView.scene.addChild(anchorNode)
        //Posizione in riferimento alla foto
        node!!.localPosition = Vector3(0f,0.8f,0f)
        node!!.select()

        listnode.add(node!!)
        clearPressed=false
    }
}

/* Credit per i modelli
"Earth" (https://skfb.ly/6TwGG) by Akshat is licensed under Creative Commons Attribution (http://creativecommons.org/licenses/by/4.0/).

 */