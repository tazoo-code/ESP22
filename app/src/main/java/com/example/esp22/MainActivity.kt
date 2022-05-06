package com.example.esp22

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.ar.core.ArCoreApk
import com.threed.jpct.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //Check della disponibilità
        val availability = ArCoreApk.getInstance().checkAvailability(this)
        if(!availability.isSupported){
            //TODO agire in qualche modo quando l'applicazione non supporta ArCore
        }

        setContentView(R.layout.activity_main)
    }
}