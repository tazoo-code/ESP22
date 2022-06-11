package com.unipd.dei.esp22.appName

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView

class ImagesGalleryActivity : AppCompatActivity() {


    private lateinit var planets : Array<String>
    private lateinit var models: Array<Model>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_gallery)
        val recyclerView: RecyclerView = findViewById(R.id.gallery_recycler_view)

        planets=this.resources.getStringArray(R.array.planet_array)
        models=Array(planets.size) {
            Model(planets[it])
        }

        //Applica l'adapter alla recyclerView
        recyclerView.adapter = ImagesGalleryAdapter(models)

        val shareButton = findViewById<ImageView>(R.id.share_button)
        shareButton.setOnClickListener {

            val uriList = ImagesGalleryAdapter.uriList
            if(uriList.isNotEmpty()){
                val shareIntent = Intent().apply {
                    action = Intent.ACTION_SEND_MULTIPLE
                    putParcelableArrayListExtra(Intent.EXTRA_STREAM, uriList)
                    type = "image/*"
                }
                startActivity(Intent.createChooser(shareIntent, null))
            } else {
                Toast.makeText(this,"Seleziona almeno un elemento", Toast.LENGTH_SHORT).show()
            }

        }

        val backButton = findViewById<ImageView>(R.id.gallery_back_button)
        backButton.setOnClickListener{
            finish()
        }
    }
}