package com.unipd.dei.esp22.appName

import android.content.Context
import android.graphics.Color
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView

//Classe adapter per il recyclerview della Galleria
class ImagesGalleryAdapter(private val list: Array<ModelPlanet>, context: Context) :
RecyclerView.Adapter<ImagesGalleryAdapter.ItemViewHolder>() {

    private val cyan : Int

    companion object {
        //Nomi dell immagini selezionate
        val names : MutableList<String> = arrayListOf()

        //Immagini selezionate
        val images : MutableList<ImageView> = arrayListOf()
    }

    init {
        val typedValue = TypedValue()
        context.theme.resolveAttribute(com.google.android.material.R.attr.colorSecondary, typedValue, true)
        cyan = typedValue.data
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

        //Imposta il colore di background alla selezione
        holder.itemView.setBackgroundColor(if (m.isSelected()) cyan else Color.WHITE)

        //Listener per l'immagine del pianeta
        holder.itemImageView.setOnClickListener {

            //Imposta la selezione e il colore corrispondente
            m.setSelected(!m.isSelected())
            holder.itemView.setBackgroundColor(if (m.isSelected()) cyan else Color.WHITE)

            /*Se l'immagine è selezionata allora viene aggiunta alla lista images
              e il suo nome alla lista names */
            if(m.isSelected()){
                names.add(m.getText() as String)
                images.add(holder.itemImageView)
            } else if (names.contains(m.getText())) {
                names.remove(m.getText() as String)
                images.remove(holder.itemImageView)
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