package com.unipd.dei.esp22.appName

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView

class ImagesGalleryActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_gallery)
        val recyclerView: RecyclerView = findViewById(R.id.gallery_recycler_view)

        val list = arrayOf("1", "2", "3", "4", "5","1", "2", "3", "4", "5","1", "2", "3", "4", "5","1", "2", "3", "4", "5")
        //Applica l'adapter alla recyclerView
        recyclerView.adapter = ImagesGalleryAdapter(list)

    }
}