package com.unipd.dei.esp22.appName


import android.content.Context
import android.content.Intent
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
import java.util.*

class AugmentedImagesActivity: AppCompatActivity() {

    //Fragment che gestisce le interazioni AR dell'utente
    private lateinit var arFragment: ArFragment

    //Database che contiene le immagini aumentate
    private lateinit var database: AugmentedImageDatabase

    /*Lista di TransformableNode che sono presenti nella scena,
      ciascuno è associato ad un preciso pianeta che ruota su se stesso.
      Quando vengono creati */
    private val listnode: MutableList<TransformableNode> = arrayListOf()

    /*Lista di Boolean che specifica se è stato renderizzato il modello 3d
      del pianeta di una determinata immagine */
    private var renderobj: MutableList<Boolean> = arrayListOf()

    //Array di stringhe dei nomi delle immagini aumentate
    private lateinit var namesobj : Array<String>

    //Nodo sul quale verrà renderizzato il modello 3d del pianeta
    private var node : TransformableNode? = null

    //Variabile che serve per far ruotare i pianeti su se stessi
    private var rot = 0f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_augmented_images)

        //Recupero i nomi delle immagini aumentate dal file arrays.xml
        namesobj= this.resources.getStringArray(R.array.planet_array)

        //Inizialmente nessun modello del pianeta è renderizzato
        for( i in namesobj.indices){
            renderobj.add(false)
        }

        //Riferimento all'immagine che permette di tornare alla home
        val homeButton : ImageView = findViewById(R.id.home_button_augm)

        //Riferimento all'immagine che permette di ricevere dello info sul funzionamento dell'applicazione
        val infoButton :ImageView = findViewById(R.id.info_button_augmented)

        /*Button che permette di riavviare l'activity eliminando
          tutti i pianeti renderizzati. */
        val clearButton = findViewById<Button>(R.id.clear)

        val galleryButton=findViewById<Button>(R.id.gallery)

        homeButton.setOnClickListener {
            finish()
        }

        infoButton.setOnClickListener{
            InfoDialogFragment().show(supportFragmentManager,"AugmentedImagesActivity")
        }

        //Listener per il riavvio dell'activity
        clearButton!!.setOnClickListener(ClearsetOnClickListener)


        galleryButton!!.setOnClickListener(GallerysetOnClickListener)

        //Riferimento al ArFragment
        arFragment = (supportFragmentManager.findFragmentById(R.id.arFragment) as ArFragment)

        //Configurazione sessione ArCore
        arFragment.apply {
            setOnSessionConfigurationListener { session, config ->

                // Modalita plane detection disabilitata
                config.planeFindingMode = Config.PlaneFindingMode.DISABLED

                /*Riferimento al database: il file "myimages.imgdb" è stato creato grazie allo
                  strumento a riga di comando "arcoreimg"*/
                database = assets.open("myimages.imgdb").use {
                    AugmentedImageDatabase.deserialize(session, it)
                }

                //Configurazione del database di immagini aumentate
                config.augmentedImageDatabase = database

                //Stima della luce disabilitata
                config.lightEstimationMode = Config.LightEstimationMode.DISABLED

                //Configurazione della sessione con le impostazioni definite precedentemente
                session.configure(config)

                //Listener che verrà chiamato per ciascun fotogramma prima che la scena si aggiorni
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
    private val ClearsetOnClickListener = View.OnClickListener {
        //Restart Activity
        val intent = intent
        finish()
        startActivity(intent)
    }


    private val GallerysetOnClickListener= View.OnClickListener {

        val intent = Intent(this, ImagesGalleryActivity::class.java)
        startActivity(intent)
    }

    //Listener che viene invocato ad ogni aggiornamento della scena di ARCore
    private val onUpdateFrame = Scene.OnUpdateListener {
        val frame = arFragment.arSceneView.arFrame

        //Ruoto di 1 grado ogni nodo per far ruotare i pianeti su se stessi
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

        //Ciclo le immagini individuate
        for (augmentedImage in augmentedImages) {

            //Controlla se l'immagine è stata tracciata
            if (augmentedImage.trackingState == TrackingState.TRACKING) {

                //Controlla a quale immagine corrisponde
                for (i in namesobj.indices) {

                    /* Se vil modello 3d del pianeta corrispondente all'immagine
                       non è stato renderizzato, allora lo crea
                     */
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
            .setSource(this, Uri.parse("models/augmentedImage/$name.glb"))
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
}
