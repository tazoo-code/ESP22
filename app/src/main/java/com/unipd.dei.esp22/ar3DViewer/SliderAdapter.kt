package com.unipd.dei.esp22.ar3DViewer

import android.content.Context
import android.graphics.Color
import android.util.Log
import android.util.TypedValue
import android.view.*
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView


class SliderAdapter(private val list: Array<String>, context: Context) :
    RecyclerView.Adapter<SliderAdapter.ItemViewHolder>() {

    //Serve per tenere traccia delle view selezionate
    private val elements : MutableList<View> = arrayListOf()

    //Serve per identificare l'immagine selezionata
    private val isSelected : MutableList<Boolean> = arrayListOf()

    //Colore per l'immagine selezionata
    private val cyan : Int

    init {
        val typedValue = TypedValue()
        // Prende il colore colorSecondary dal tema
        context.theme.resolveAttribute(com.google.android.material.R.attr.colorSecondary, typedValue, true)
        cyan = typedValue.data
    }

    // Ritorna un nuovo ViewHolder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.slider_item, parent, false)
            for (i in 0 until itemCount){
                isSelected.add(false)
            }

        return ItemViewHolder(view)
    }

    // Ritorna la dimensione dell'array
    override fun getItemCount(): Int {
        return list.size
    }

    // Mostra l'immagine in una certa posizione
    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {

        //Android ricostruisce gli elementi fuori dallo schermo quindi dobbiamo tenere conto degli oggetti selezionati
        if(isSelected[position]){
            holder.itemView.setBackgroundColor(cyan)
            Log.d("Slider", "$position Cyan")
        }else{
            holder.itemView.setBackgroundColor(Color.TRANSPARENT)
            Log.d("Slider", "$position Trasparente")
        }

        holder.bind(list[position])

        /*Ogni volta che viene scelto un animale, vengono impostati i background
          trasparenti per tutti gli animali che erano stati selezionati in precedenza.
         */
        holder.itemImageView.setOnClickListener {

            Log.i("Slider",position.toString())

                //disattivare gli altri
                for(e in elements){
                    e.setBackgroundColor(Color.TRANSPARENT)
                }

                for (i in 0 until itemCount){
                    isSelected[i] = false
                }

                //attiviamo quello selezionato
                holder.itemView.setBackgroundColor(cyan)
                elements.add(holder.itemView)
                isSelected[position] = true

        }
    }

    // Descrive un elemento e la sua posizione
    inner class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val itemImageView: ImageView = itemView.findViewById(R.id.slider_img_preview)

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