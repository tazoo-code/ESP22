package com.example.esp22

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.ar.core.*
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.Node
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.ux.ArFragment
import com.google.ar.sceneform.ux.BaseArFragment
import java.util.concurrent.CompletableFuture


class AugmentedImagesActivity: AppCompatActivity() {

    private lateinit var arFragment: ArFragment

    private var tv1: TextView? = null
    private var model: CompletableFuture<ModelRenderable>? = null
    private var centerNode: Node? = null
    var modelSelected = false

    private lateinit var database: AugmentedImageDatabase

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_augmented_images)

        tv1 = findViewById<TextView>(R.id.tx1)


        //Riferimento al ArFragment
        arFragment = (supportFragmentManager.findFragmentById(R.id.arFragment) as ArFragment)

        //Configurazione sessione ArCore
        arFragment.apply {
            setOnSessionConfigurationListener { session, config ->

                // Disable plane detection
                config.planeFindingMode = Config.PlaneFindingMode.DISABLED

                //database = AugmentedImageDatabase(session)

                database = assets.open("myimages.imgdb").use {
                    AugmentedImageDatabase.deserialize(session, it)
                }

                config.augmentedImageDatabase = database
                config.focusMode = Config.FocusMode.AUTO
                session.configure(config)

                // Check for image detection
                arFragment.setOnAugmentedImageUpdateListener(onAugmentedImageTrackingUpdate)

            }

        }
        setModel()
    }

    private val onAugmentedImageTrackingUpdate =
        BaseArFragment.OnAugmentedImageUpdateListener { augmentedImage ->
            //Log.i("AugmentedImage", "Image Found")
            if (augmentedImage.trackingState == TrackingState.TRACKING
                && augmentedImage.trackingMethod == AugmentedImage.TrackingMethod.FULL_TRACKING
            ) {
                Log.i("AugmentedImage", "Image ${augmentedImage.name} tracked")
                when (augmentedImage.name) {

                    "terra.jpeg" -> tv1!!.text = "terra visibile"

                    "marte.jpeg" -> tv1!!.text = "Marte visibile"

                    "mercurio.jpeg" -> tv1!!.text = "Mercurio visibile"

                    "rabbit.jpeg" -> tv1!!.text = "Rabbit visibile"

                }
                if(!modelSelected){
                    Log.i("AugmentedImage", "Image ${augmentedImage.name} tracked")
                    augmentedImage.createAnchor(augmentedImage.getCenterPose())


                    val localPosition = Vector3()
                    val centerNode = Node()
                    val an = AnchorNode(augmentedImage.anchors.first())
                    Log.i("AugmentedImage", augmentedImage.anchors.first().toString())
                    Log.i("AugmentedImage", augmentedImage.anchors.size.toString())
                    centerNode.parent = an
                    localPosition.set(augmentedImage.getExtentX(),0.5f,augmentedImage.extentZ)
                    //localPosition[ augmentedImage.getExtentX(), 0.5f] =  augmentedImage.getExtentZ()
                    centerNode.localPosition = localPosition
                    centerNode.renderable = model!!.getNow(null)
                    arFragment.getArSceneView().getScene().addChild(centerNode)
                    modelSelected = true
                }
            }

        }

    fun setModel() {
        if (model == null) {
            model =
                ModelRenderable.builder()
                    .setSource(this, Uri.parse("models/cuboRosso.glb"))
                    .setIsFilamentGltf(true)
                    //.setSource(context, Uri.parse("models/borderfence-small2.sfb"))
                    .build();
        }
    }
}