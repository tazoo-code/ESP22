package com.example.esp22

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView


class InitialActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_layout)

        val recyclerView: RecyclerView = findViewById(R.id.recycler_view)
        // TODO prendere/salvare le preview
        val list = arrayOf("1", "2", "3", "4", "5","1", "2", "3", "4", "5","1", "2", "3", "4", "5","1", "2", "3", "4", "5")
        //Applica l'adapter alla recyclerView
        recyclerView.adapter = GalleryAdapter(list)

    }
}