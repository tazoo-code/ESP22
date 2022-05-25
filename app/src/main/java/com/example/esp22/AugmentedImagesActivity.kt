package com.example.esp22


import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.ar.core.*
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.Node
import com.google.ar.sceneform.Scene
import com.google.ar.sceneform.math.Quaternion
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.rendering.Renderable
import com.google.ar.sceneform.ux.ArFragment
import com.google.ar.sceneform.ux.TransformableNode
import java.util.concurrent.CompletableFuture


class AugmentedImagesActivity: AppCompatActivity() {

    private lateinit var arFragment: ArFragment

    private var tv1: TextView? = null
    private var model: CompletableFuture<ModelRenderable>? = null
    private var centerNode: Node? = null
    var modelSelected = false

    private lateinit var database: AugmentedImageDatabase
    private var isRendered = false
    private var node : TransformableNode? = null
    private var rot = 0f

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_augmented_images)

        tv1 = findViewById<TextView>(R.id.tx1)
        //setModel()



    }

    override fun onResume() {
        super.onResume()

        //Riferimento al ArFragment
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

                // Check for image detection
                //arFragment.setOnAugmentedImageUpdateListener(onAugmentedImageTrackingUpdate)
                arFragment.getArSceneView().getScene().addOnUpdateListener(onUpdateFrame);

            }

        }

    }








    private val onUpdateFrame = Scene.OnUpdateListener {
        val frame = arFragment.arSceneView.arFrame

        //Rotazione del nodo e quindi del modello TODO aggiungere la possibilità di ruotare piu nodi(con una lista di nodi?)
        if(node != null) {
            if (rot >= 360) {
                rot = 0f
            }
            rot++
            node!!.localRotation = Quaternion(Vector3(0f,rot,0f))
        }

        val augmentedImages = frame!!.getUpdatedTrackables(
            AugmentedImage::class.java
        )
        for (augmentedImage in augmentedImages) {
            if (augmentedImage.trackingState == TrackingState.TRACKING) {
                if (augmentedImage.name.contains("terra") && !isRendered) {
                    // here we got that image has been detected
                    // we will render our 3D asset in center of detected image
                    renderObject(
                        arFragment,
                        augmentedImage.createAnchor(augmentedImage.centerPose),
                        "terra"
                    )
                    isRendered = true
                }

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
        val anchorNode = AnchorNode(anchor)
        anchorNode.localScale = Vector3(0.1f,0.1f,0.1f)

        node = TransformableNode(fragment.transformationSystem)
        node!!.renderable = renderable
        node!!.parent = anchorNode
        //node.localScale = Vector3(0.05f,0.05f,0.05f)
        fragment.arSceneView.scene.addChild(anchorNode)
        //Posizione in riferimento alla foto
        node!!.localPosition = Vector3(0f,0.5f,0f)
        node!!.select()

    }
}

/* Credit per i modelli
"Earth" (https://skfb.ly/6TwGG) by Akshat is licensed under Creative Commons Attribution (http://creativecommons.org/licenses/by/4.0/).

 */