package com.unipd.dei.esp22.appName

import android.graphics.Color
import android.view.*
import android.widget.ImageView
import androidx.appcompat.view.menu.MenuView
import androidx.recyclerview.widget.RecyclerView

class SliderAdapter(private val list: Array<ModelAnimal>) :
    RecyclerView.Adapter<SliderAdapter.ItemViewHolder>() {

    private val elements : MutableList<View> = arrayListOf()

    private val indices : MutableList<Int> = arrayListOf()

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

        holder.bind(list[position].getText() as String)

        holder.itemView.setBackgroundColor(if (list[position].isSelected()) Color.CYAN else Color.WHITE)

        holder.itemImageView.setOnClickListener {

            if(!list[position].isSelected()) {
                list[position].setSelected(true)
                holder.itemView.setBackgroundColor(Color.CYAN)

                if(indices.isNotEmpty()) {
                    for (i in 0 until elements.size) {
                        elements.get(i).setBackgroundColor(Color.WHITE)
                    }
                    for (j in indices) {
                        list[j].setSelected(false)
                        indices.remove(j)
                    }
                }
                indices.add(position)
                elements.add(holder.itemView)
            }

        }
    }

    // Descrive un elemento e la sua posizione
    inner class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        public val itemImageView: ImageView = itemView.findViewById(R.id.slider_img_preview)

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