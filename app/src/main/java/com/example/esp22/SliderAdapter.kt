package com.example.esp22

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView

class SliderAdapter(private val list: Array<String>) :
    RecyclerView.Adapter<SliderAdapter.ItemViewHolder>() {

    private val onClickListener = View.OnClickListener { v ->
        //TODO cambio oggetto 3D
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
            itemImageView.setImageResource(R.drawable.no_image)
        }
    }

}