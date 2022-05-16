package com.example.esp22

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.Node
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.rendering.Renderable
import com.google.ar.sceneform.ux.ArFragment
import com.google.ar.sceneform.ux.TransformableNode

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val button1 = findViewById<Button>(R.id.bu1)
        val button2 = findViewById<Button>(R.id.bu2)
        val button3 = findViewById<Button>(R.id.bu3)


        button1.setOnClickListener {
            val n1=getString(R.string.cubo)
            val myIntent= Intent(it.context,SessionActivity::class.java)
            myIntent.putExtra("nameObject", n1 )
            it.context.startActivity(myIntent)
        }

        button2.setOnClickListener {
            val n1=getString(R.string.spada)
            val myIntent= Intent(it.context,SessionActivity::class.java)
            myIntent.putExtra("nameObject", n1 )
            it.context.startActivity(myIntent)
        }

        button3.setOnClickListener {
            val n1=getString(R.string.vinitalyGLB)
            val myIntent= Intent(it.context,SessionActivity::class.java)
            myIntent.putExtra("nameObject", n1 )
            it.context.startActivity(myIntent)
        }
    }
}




