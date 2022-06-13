package com.unipd.dei.esp22.appName

/*Gli oggetti ModelAnimal vengono associati alle immagini degli animali.
  La definizione di questa classe ci ha permesso di gestire la selezione
  di un animale nello slider e di colorare il background dell'immagine
  corrispondente. Solo l'elemento selezionato avrà il background colorato*/
class ModelAnimal(private var name:String?) {

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