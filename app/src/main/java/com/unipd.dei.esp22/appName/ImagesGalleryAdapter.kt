package com.unipd.dei.esp22.appName

import android.graphics.Color
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView

class ImagesGalleryAdapter(private val list: Array<Model>) :
RecyclerView.Adapter<ImagesGalleryAdapter.ItemViewHolder>() {

    companion object {
        val uriList : ArrayList<Uri> = arrayListOf()
    }

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

        holder.itemImageView.setOnClickListener {
            m.setSelected(!m.isSelected())
            holder.itemView.setBackgroundColor(if (m.isSelected()) Color.CYAN else Color.WHITE)

            val uri = Uri.parse("android.resource://com.unipd.dei.esp22.appName/drawable/"+ m.getText())
            if(m.isSelected()){
                uriList.add(uri)
            } else if (uriList.contains(uri)) {
                uriList.remove(uri)
            }
        }
    }

    // Descrive un elemento e la sua posizione
    class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val itemImageView: ImageView = itemView.findViewById(R.id.gallery_img_preview)

        fun bind(str : String) {
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