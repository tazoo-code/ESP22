package com.example.esp22

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior


class CameraActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_camera)

        //RecyclerView dello slider
        val recyclerView : RecyclerView = findViewById(R.id.slider_recycler_view)
        // TODO prendere/salvare le preview
        val list = arrayOf("1","2","3","4","5")
        //Applica l'adapter alla recyclerView
        recyclerView.adapter = SliderAdapter(list)

        val bottomSheet : LinearLayout = findViewById(R.id.bottom_sheet_layout)
        val bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet)
        //Rileva quando lo slider cambia di stato
        bottomSheetBehavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, i: Int) {
                //cambia immagine alla freccia
                changeArrow(i)
            }
            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                //Nothing
            }
        })

        //Rileva il tocco della freccia, e cambia lo stato dello slider
        val arrow : ImageView = findViewById(R.id.gallery_arrow)
        arrow.setOnClickListener {
            when (bottomSheetBehavior.state) {
                BottomSheetBehavior.STATE_EXPANDED -> bottomSheetBehavior.state =
                    BottomSheetBehavior.STATE_COLLAPSED
                BottomSheetBehavior.STATE_COLLAPSED -> bottomSheetBehavior.state =
                    BottomSheetBehavior.STATE_EXPANDED
            }
        }

    }
    //Cambia la freccia del bottom sheet verso l'alto o verso il basso quando lo stato cambia
    fun changeArrow(state: Int){
        val iv: ImageView = findViewById(R.id.gallery_arrow)
        var myDrawable : Drawable?
        when(state) {
            //Se lo stato è EXPANDED, la freccia diventa verso il basso
            BottomSheetBehavior.STATE_EXPANDED -> {
                myDrawable = ResourcesCompat.getDrawable(
                    resources,
                    R.drawable.ic_baseline_keyboard_arrow_down_40,
                    null
                )
            }
            //Se lo stato è COLLAPSED, la freccia diventa verso l'alto
            BottomSheetBehavior.STATE_COLLAPSED -> {
                myDrawable = ResourcesCompat.getDrawable(
                    resources,
                    R.drawable.ic_baseline_keyboard_arrow_up_40,
                    null
                )
            }
            else -> myDrawable = ResourcesCompat.getDrawable(
                resources,
                R.drawable.ic_baseline_keyboard_arrow_down_40,
                null
            )
        }
        iv.setImageDrawable(myDrawable)
    }
}