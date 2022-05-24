package com.example.esp22

import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.ar.core.*
import com.google.ar.sceneform.ux.ArFragment
import com.google.ar.sceneform.ux.BaseArFragment


class AugmentedImagesActivity: AppCompatActivity() {

    private lateinit var arFragment: ArFragment

    private var tv1:TextView?=null

    private lateinit var database:  AugmentedImageDatabase

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
    }

    private val onAugmentedImageTrackingUpdate=
        BaseArFragment.OnAugmentedImageUpdateListener{ augmentedImage ->
            Log.i("AugmentedImage","Image Found")
            if(augmentedImage.trackingState == TrackingState.TRACKING
                && augmentedImage.trackingMethod == AugmentedImage.TrackingMethod.FULL_TRACKING){
                Log.i("AugmentedImage","Image ${augmentedImage.name} tracked")
                    when(augmentedImage.name){

                        "terra.jpeg"->  tv1!!.text="terra visibile"

                        "marte.jpeg"-> tv1!!.text="Marte visibile"

                        "mercurio.jpeg"->tv1!!.text="Mercurio visibile"

                        "rabbit.jpeg"->tv1!!.text="Rabbit visibile"

                    }
            }
    }

}