package com.unipd.dei.esp22.ar3DViewer


/*Gli oggetti ModelPlanet vengono associati alle immagini dei pianeti.
  La definizione di questa classe ci ha permesso di gestire la selezione
  di un pianeta nella galleria e di colorare il background dell'immagine
  corrispondente. Più elementi possono essere selezionati ed avere il background
   colorato.*/
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