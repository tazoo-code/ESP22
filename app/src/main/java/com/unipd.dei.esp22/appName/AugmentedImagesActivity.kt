package com.unipd.dei.esp22.appName


import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.unipd.dei.esp22.appName.InfoDialogFragment
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

    private val listnode: MutableList<TransformableNode> = arrayListOf()

    private var renderobj: MutableList<Boolean> = arrayListOf()

    private lateinit var namesobj : Array<String>

    private var node : TransformableNode? = null

    private var rot = 0f

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_augmented_images)

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

                // Disable plane detection

                config.planeFindingMode = Config.PlaneFindingMode.DISABLED

                database = assets.open("myimages.imgdb").use {
                    AugmentedImageDatabase.deserialize(session, it)
                }

                config.augmentedImageDatabase = database
                config.lightEstimationMode = Config.LightEstimationMode.DISABLED
                session.configure(config)

                arFragment.arSceneView.scene.addOnUpdateListener(onUpdateFrame)

                val a = session.config
                val b = a.focusMode.name
                Log.i("Camera","Camerafocus -> $b")
                //Necessario per abilitare le modifiche
                session.resume()
            }
        }
    }

    override fun onPause() {
        super.onPause()
        arFragment.onPause()
    }
    override fun onResume() {
        super.onResume()
        arFragment.onResume()
    }

    //Listener per resettare il tracking delle immagini
    private val setOnClickListener = View.OnClickListener {
        //Restart Activity
        val intent = intent
        finish()
        startActivity(intent)
    }

    //listener che viene invocato ad ogni aggiornamento della scena di ARCore
    private val onUpdateFrame = Scene.OnUpdateListener {
        val frame = arFragment.arSceneView.arFrame

        //Ruoto di 1 grado ogni frame ogni nodo per far ruotare i pianeti su se stessi
        if (listnode.isNotEmpty()) {
            for (n in listnode) {

                if (rot >= 360) {
                    rot = 0f
                }
                rot++
                n.localRotation = Quaternion(Vector3(0f, rot, 0f))
            }
        }

        //Update di tutte le immagini individuate
        val augmentedImages = frame!!.getUpdatedTrackables(
            AugmentedImage::class.java
        )

        //Per ogni immagine tracciata  se non è presente il modello allora viene immediatamente costruito e instanziato
        for (augmentedImage in augmentedImages) {

            if (augmentedImage.trackingState == TrackingState.TRACKING) {

                for (i in 0 until namesobj.size) {

                    if (augmentedImage.name.contains(namesobj[i]) && !renderobj[i]) {

                        Toast.makeText(this,""+namesobj[i]+" rilevato",Toast.LENGTH_SHORT).show()

                        if(namesobj[i]=="systemsolar"){
                            renderObject(
                                arFragment,
                                augmentedImage.createAnchor(augmentedImage.centerPose),
                                "solar_system"
                            )
                        }else {
                            renderObject(
                                arFragment,
                                augmentedImage.createAnchor(augmentedImage.centerPose),
                                namesobj[i]
                            )
                        }
                        renderobj[i] = true
                    }
                }
            }
        }
    }

    //Funzione chiamata per costruire i modelli 3d dei pianeti
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

    //Funzione usata per piazzare i nodi nella scena di ARCore
    private fun addNodeToScene(fragment: ArFragment, anchor: Anchor, renderable: Renderable) {

        val anchorNode = AnchorNode(anchor)
        anchorNode.localScale = Vector3(0.1f,0.1f,0.1f)

        node = TransformableNode(fragment.transformationSystem)
        node!!.renderable = renderable
        node!!.parent = anchorNode

        if(node!!.renderableInstance.hasAnimations()){
            node!!.renderableInstance.animate(true).start()
        }

        fragment.arSceneView.scene.addChild(anchorNode)
        //Posizione in riferimento alla foto
        node!!.localPosition = Vector3(0f,0.8f,0f)
        node!!.select()

        listnode.add(node!!)
    }
}
