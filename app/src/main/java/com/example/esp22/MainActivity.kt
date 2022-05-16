package com.example.esp22

import android.content.ContentResolver
import android.content.res.Resources
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.Node
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.rendering.Renderable
import com.google.ar.sceneform.ux.ArFragment
import com.google.ar.sceneform.ux.TransformableNode


class MainActivity : AppCompatActivity() {

    lateinit var arFragment: ArFragment

    //TODO ricordarsi di cambiarlo se cambia il nome del pacchetto
    val PACKAGE_NAME = "com.example.esp22"

    private var model: Renderable? = null
    var cubeRenderable: ModelRenderable? = null

    /*private val onTapPlane = arFragment.setOnTapArPlaneListener { hitResult,plane, motionEvent ->
        arFragment.arSceneView.scene.addChild(AnchorNode(hitResult.createAnchor()).apply {
            // Create the transformable model and add it to the anchor.
            addChild(TransformableNode(arFragment.transformationSystem).apply {
                val model = createModel(1)

                renderable = cubeRenderable
                //renderableInstance.animate(true).start()
                // Add child model relative the a parent model
                addChild(Node().apply {
                    // Define the relative position
                    localPosition = Vector3(0.0f, 1f, 0.0f)
                    // Define the relative scale
                    localScale = Vector3(0.7f, 0.7f, 0.7f)
                    //renderable = modelView
                })
            })
        })
    }*/

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        arFragment = (supportFragmentManager.findFragmentById(R.id.arFragment) as ArFragment)


        /*arFragment.setOnTapArPlaneListener { hitResult, plane, motionEvent ->

            val anchor = hitResult.createAnchor()
            val anchorNode = AnchorNode(anchor)
            anchorNode.setParent(arFragment.arSceneView.scene)
            createModel(anchorNode, 1)*/

            arFragment.setOnTapArPlaneListener { hitResult,plane, motionEvent ->

            arFragment.arSceneView.scene.addChild(AnchorNode(hitResult.createAnchor()).apply {

                setModel()

                // Create the transformable model and add it to the anchor.
                addChild(TransformableNode(arFragment.transformationSystem).apply {
                    //val model = createModel(1)

                    renderable = cubeRenderable
                    //renderableInstance.setCulling(false)
                    //renderableInstance.animate(true).start()
                    // Add child model relative the a parent model
                    addChild(Node().apply {
                        // Define the relative position
                        localPosition = Vector3(0.0f, 1f, 0.0f)
                        // Define the relative scale
                        localScale = Vector3(0.7f, 0.7f, 0.7f)
                        //renderable = modelView
                    })
                })
            })
        }

        }

    private fun setModel() {
        //Modo1
        //UriParse("/assets/models/cub.gltf")

        //Modo2
        //R.raw.cub

        //Modo3
        //UriParse("android.resource://"+PACKAGE_NAME+"/" + R.raw.cub")

        //Modo4
        val resources: Resources = this.getResources()
        val uri = Uri.Builder()
            .scheme(ContentResolver.SCHEME_ANDROID_RESOURCE)
            .authority(resources.getResourcePackageName(R.raw.cub))
            .appendPath(resources.getResourceTypeName(R.raw.cub))
            .appendPath(resources.getResourceEntryName(R.raw.cub))
            .build()

        ModelRenderable.builder()
            .setSource(this, uri)
            .setIsFilamentGltf(true)
            .build()
            .thenAccept { model: ModelRenderable -> cubeRenderable = model }
            .exceptionally {
                Toast.makeText(this, "Unable to load Cube model", Toast.LENGTH_SHORT)
                null
            }

    }

}

