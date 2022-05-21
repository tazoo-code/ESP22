package com.example.esp22

import android.content.Context
import android.content.Intent.*
import android.view.*
import android.widget.AdapterView
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

            when(str){

                "cuboRosso"->itemImageView.setImageResource(R.drawable.cuborosso)
                "spada"->itemImageView.setImageResource(R.drawable.spada)
                "cuboWireframe"->itemImageView.setImageResource(R.drawable.cubowireframe)
                "lamp"->itemImageView.setImageResource(R.drawable.lamp)

            }
        }
    }
}

 //Classe per gestire l'evento click su un elemento dello slider
 class RecyclerItemClickListener(context:Context, listener: OnItemClickListener) : RecyclerView.OnItemTouchListener {

     private var  mListener :OnItemClickListener?

     interface OnItemClickListener {
         fun onItemClick(view: View?, position: Int)
     }

     var mGestureDetector: GestureDetector? = null

     init {
         mListener = listener
         mGestureDetector = GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
             override fun onSingleTapUp(e: MotionEvent?): Boolean {
                 return true
             }
         })
     }

     override fun onInterceptTouchEvent(view: RecyclerView, e: MotionEvent): Boolean {
         val childView = view.findChildViewUnder(e.x, e.y)
         if (childView != null && mListener != null && mGestureDetector!!.onTouchEvent(e)) {
             mListener!!.onItemClick(childView, view.getChildAdapterPosition(childView))
         }
         return false
     }

     override fun onTouchEvent(view: RecyclerView, e: MotionEvent) {
         //intenzionalmente vuoto
     }

     override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {}
}