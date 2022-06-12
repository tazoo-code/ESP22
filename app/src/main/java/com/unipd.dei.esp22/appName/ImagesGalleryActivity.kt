package com.unipd.dei.esp22.appName

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.RecyclerView
import java.io.File
import java.io.FileOutputStream


class ImagesGalleryActivity : AppCompatActivity() {


    private lateinit var planets : Array<String>
    private lateinit var models: Array<Model>

    private lateinit var privateRootDir: File

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_gallery)
        val recyclerView: RecyclerView = findViewById(R.id.gallery_recycler_view)

        planets=this.resources.getStringArray(R.array.planet_array)
        models=Array(planets.size) {
            Model(planets[it])
        }

        //Applica l'adapter alla recyclerView
        recyclerView.adapter = ImagesGalleryAdapter(models)

        val shareButton = findViewById<ImageView>(R.id.share_button)
        shareButton.setOnClickListener {

            val  imageList = ImagesGalleryAdapter.imageList
            val  images=ImagesGalleryAdapter.images

            if(imageList.isNotEmpty()){

                val uriArray = ArrayList<Uri>()

                //val fileArray= ArrayList<File>()

                for (i in imageList.indices){
                    //val id = resources.getIdentifier(imageList.get(i),"drawable", packageName)

                    val img=images.get(i)
                    val bitmapDrawable: BitmapDrawable= img.drawable as BitmapDrawable
                    val bitmap :Bitmap=bitmapDrawable.bitmap
                    val uri: Uri= getImageToShare(bitmap,imageList.get(i)) as Uri
                    uriArray.add(uri)
                }

                val shareIntent = Intent().apply {
                    action = Intent.ACTION_SEND_MULTIPLE
                    putParcelableArrayListExtra(Intent.EXTRA_STREAM, uriArray )
                    type = "image/png"
                }

                startActivity(Intent.createChooser(shareIntent, "SharePlanets"))
                /*val shareIntent = Intent().apply {
                    val uriArray = ArrayList<Uri>()

                    val fileArray= ArrayList<File>()
                    for (name in imageList){
                        val id = resources.getIdentifier(name,"drawable", packageName)
                        //val file : File = File(filesDir, i)

                        val uri = Uri.parse("android.resource://com.unipd.dei.esp22.appName/$id")
                        //val f= File("drawable/"+name+".png")
                        uriArray.add(uri)
                    }
                    action = Intent.ACTION_SEND_MULTIPLE
                    putParcelableArrayListExtra(Intent.EXTRA_STREAM, uriArray )
                    type = "image/png"
                    flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                }
                startActivity(Intent.createChooser(shareIntent, null))*/
            }else {
                Toast.makeText(this,"Seleziona almeno un elemento", Toast.LENGTH_SHORT).show()
            }

        }

        val backButton = findViewById<ImageView>(R.id.gallery_back_button)
        backButton.setOnClickListener{
            finish()
        }
    }

    private fun getImageToShare(bitmap: Bitmap, name:String): Uri? {

        val imagefolder: File = File(filesDir, "images")
        var uri: Uri? = null
        try {
            imagefolder.mkdirs()
            val file = File(imagefolder, name+".png")
            val outputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.PNG, 90, outputStream)
            outputStream.flush()
            outputStream.close()
            uri = FileProvider.getUriForFile(this, packageName, file)
        } catch (e: Exception) {
            Toast.makeText(this, "" + e.message, Toast.LENGTH_LONG).show()
        }
        return uri
    }
}


