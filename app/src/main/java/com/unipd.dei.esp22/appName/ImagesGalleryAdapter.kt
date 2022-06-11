package com.unipd.dei.esp22.appName

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView

class ImagesGalleryAdapter(private val list: Array<Model>) :
RecyclerView.Adapter<ImagesGalleryAdapter.ItemViewHolder>() {

    // Ritorna un nuovo ViewHolder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.gallery_item, parent, false)

        return ItemViewHolder(view)
    }

    // Ritorna la dimensione dell'array
    override fun getItemCount(): Int {
        return list.size
    }

    // Mostra l'immagine in una certa posizione
    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {

        val m= list[position]
        holder.bind(m.getText()!!)

        holder.itemView.setBackgroundColor(if (m.isSelected()) Color.CYAN else Color.WHITE)

        holder.itemImageView.setOnClickListener(View.OnClickListener {
            m.setSelected(!m.isSelected())
            holder.itemView.setBackgroundColor(if (m.isSelected()) Color.CYAN else Color.WHITE)
        })
    }

    // Descrive un elemento e la sua posizione
    class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        public val itemImageView: ImageView = itemView.findViewById(R.id.gallery_img_preview)

        fun bind(str : String) {
            itemImageView.setImageResource(R.drawable.no_image)
        }
    }


}