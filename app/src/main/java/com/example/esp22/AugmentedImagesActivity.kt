package com.example.esp22

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.ar.core.AugmentedImage
import com.google.ar.core.AugmentedImageDatabase
import com.google.ar.core.Config
import com.google.ar.core.TrackingState
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.rendering.CameraStream
import com.google.ar.sceneform.ux.ArFragment
import com.google.ar.sceneform.ux.BaseArFragment
import com.gorisse.thomas.sceneform.light.LightEstimationConfig
import com.gorisse.thomas.sceneform.lightEstimationConfig

class AugmentedImagesActivity: AppCompatActivity() {

    lateinit var arFragment: ArFragment

    var tv1:TextView?=null

    private var database: AugmentedImageDatabase? = null

    private var imageDetected=false

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_augmented_images)

        tv1= findViewById<TextView>(R.id.tx1)

        //Riferimento al ArFragment
        arFragment = (supportFragmentManager.findFragmentById(R.id.arFragment) as ArFragment)

        //Configurazione sessione ArCore
        arFragment.apply {
            setOnSessionConfigurationListener { session, config ->

                // Disable plane detection
                config.setPlaneFindingMode(Config.PlaneFindingMode.DISABLED)

                database = AugmentedImageDatabase(session)

                val cuboimg: Bitmap= BitmapFactory.decodeResource(resources, R.drawable.ric_cuborosso)

                database!!.addImage("cuboRosso", cuboimg)

                config.setAugmentedImageDatabase(database)

                // Check for image detection
                arFragment.setOnAugmentedImageUpdateListener(onAugmentedImageTrackingUpdate)

            }

        }
    }

    private val onAugmentedImageTrackingUpdate=
        BaseArFragment.OnAugmentedImageUpdateListener{ augmentedImage ->

            if(augmentedImage.getTrackingState()==TrackingState.TRACKING
                && augmentedImage.getTrackingMethod() == AugmentedImage.TrackingMethod.FULL_TRACKING){

                if(!imageDetected && augmentedImage.name.equals("cuboRosso")){

                    imageDetected=true
                    tv1!!.text="cubo rosso riconosciuto"

                }
            }
    }

}