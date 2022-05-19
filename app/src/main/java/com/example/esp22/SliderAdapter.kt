package com.example.esp22

import android.app.Activity
import android.content.Intent
import android.content.Intent.*
import android.content.res.Resources
import android.net.Uri
import android.util.Log.v
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView

import androidx.recyclerview.widget.RecyclerView

class SliderAdapter(private val list: Array<String>) :
    RecyclerView.Adapter<SliderAdapter.ItemViewHolder>() {

    private val onClickListener = View.OnClickListener { v ->
        //TODO cambio oggetto 3D

        val nameObj= v.findViewById<ImageView>(R.id.slider_img_preview)
        val myIntent= Intent(v.context,SessionActivity::class.java)

        myIntent.setFlags(FLAG_ACTIVITY_REORDER_TO_FRONT)
        myIntent.putExtra("nameObject", nameObj.contentDescription )


        v.context.startActivity(myIntent)

    }

    // Ritorna un nuovo ViewHolder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.slider_item, parent, false)
        view.setOnClickListener(onClickListener)
        return ItemViewHolder(view)
    }

    // Ritorna la dimensione dell'array
    override fun getItemCount(): Int {
        return list.size
    }

    // Mostra l'immagine in una certa posizione
    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.bind(list[position])
    }

    // Descrive un elemento e la sua posizione
    class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val itemImageView: ImageView = itemView.findViewById(R.id.slider_img_preview)


        //TODO Serve str?
        fun bind(str : String) {

            itemImageView.contentDescription=str

            when(str){

                "cuboRosso"->itemImageView.setImageResource(R.drawable.cuborosso)
                "spada"->itemImageView.setImageResource(R.drawable.spada)
                "cuboWireframe"->itemImageView.setImageResource(R.drawable.cubowireframe)
                "lamp"->itemImageView.setImageResource(R.drawable.lamp)

            }
        }
    }

}