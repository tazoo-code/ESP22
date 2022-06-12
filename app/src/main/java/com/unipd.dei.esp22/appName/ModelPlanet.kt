package com.unipd.dei.esp22.appName

import android.widget.ImageView

class ModelPlanet(private var name:String?) {

    private var isSelected = false

    fun getText(): String? {
        return name
    }

    fun setSelected(selected: Boolean) {
        isSelected = selected
    }

    fun isSelected(): Boolean {
        return isSelected
    }
}