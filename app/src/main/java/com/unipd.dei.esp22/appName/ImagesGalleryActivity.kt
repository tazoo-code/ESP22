package com.unipd.dei.esp22.appName

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.google.ar.sceneform.ux.TransformableNode

class ImagesGalleryActivity : AppCompatActivity() {


    private lateinit var planets : Array<String>
    private lateinit var models: Array<Model>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_gallery)
        val recyclerView: RecyclerView = findViewById(R.id.gallery_recycler_view)

        planets=this.resources.getStringArray(R.array.planet_array)
        models=Array<Model>(planets.size) {
            Model(planets[it])
        }

        //Applica l'adapter alla recyclerView
        recyclerView.adapter = ImagesGalleryAdapter(models)

    }
}