package com.unipd.dei.esp22.appName

import android.view.*
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView

class SliderAdapter(private val list: Array<String>) :
    RecyclerView.Adapter<SliderAdapter.ItemViewHolder>() {

    // Ritorna un nuovo ViewHolder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.slider_item, parent, false)

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
    inner class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val itemImageView: ImageView = itemView.findViewById(R.id.slider_img_preview)

        fun bind(str : String) {

            itemImageView.contentDescription=str

            var id = itemImageView.context.resources.getIdentifier(str,"drawable",itemImageView.context.packageName)
            if(id == 0) {
                id = itemImageView.context.resources.getIdentifier(
                    "no_image",
                    "drawable",
                    itemImageView.context.packageName
                )
            }

            itemImageView.setImageResource(id)

        }
    }
}