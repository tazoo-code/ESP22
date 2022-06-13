package com.unipd.dei.esp22.appName

import android.content.Context
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import androidx.recyclerview.widget.RecyclerView

// Classe per gestire l'evento click su un elemento dello slider
class RecyclerItemClickListener(context: Context, listener: OnItemClickListener) : RecyclerView.OnItemTouchListener {

    private var mListener :OnItemClickListener?
    private var mGestureDetector: GestureDetector? = null

    init {
        mListener = listener
        mGestureDetector = GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
            override fun onSingleTapUp(e: MotionEvent?): Boolean {
                return true
            }
        })
    }

    interface OnItemClickListener {
        fun onItemClick(view: View?, position: Int)
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

    override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {
        //intenzionalmente vuoto
    }
}