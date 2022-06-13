package com.unipd.dei.esp22.appName

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.RecyclerView
import java.io.File
import java.io.FileOutputStream

class ImagesGalleryActivity : AppCompatActivity() {

    //Array di stringhe che contiene i nomi delle immagini dei pianeti
    private lateinit var planets : Array<String>

    //Array che contiene oggetti ModelPlanet che verrà passato come parametro ad ImagesGalleryAdapter
    private lateinit var modelPlanets: Array<ModelPlanet>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gallery)

        //RecyclerView della galleria
        val recyclerView: RecyclerView = findViewById(R.id.gallery_recycler_view)

        //Riferimento all'immagine che permette di condividere le immagini dei pianeti
        val shareButton = findViewById<ImageView>(R.id.share_button)

        //Recupero i nomi dei pianeti dal file arrays.xml
        planets=this.resources.getStringArray(R.array.planet_array)

        //Per ciascun pianeta creo l'oggetto ModelPlanet corrispondente e lo aggiungo all'array
        modelPlanets=Array(planets.size) {
            ModelPlanet(planets[it])
        }

        //Definisce l'adapter per il recyclerView
        recyclerView.adapter = ImagesGalleryAdapter(modelPlanets, this)

        //Listener per gestire l'evento click su shareButton
        shareButton.setOnClickListener {

            //Lista di nomi delle immagini che sono state selezionate dalla galleria
            val  namesImg = ImagesGalleryAdapter.names

            //Lista delle immagini che sono state selezionate dalla galleria
            val  imgs=ImagesGalleryAdapter.images

            //Se sono state selezionate delle immagini
            if(namesImg.isNotEmpty()){

                //Definizione di una lista di Uri (verrà definito un Uri per ciascuna immagine che è stata selezionata)
                val uriArray = ArrayList<Uri>()

                for (i in namesImg.indices){

                    //Per ciascuna immagine selezionata
                    val img= imgs[i]

                    //Recupero drawable corrente che avvolge una bitmap
                    val bitmapDrawable: BitmapDrawable= img.drawable as BitmapDrawable

                    //Recupero bitmap
                    val bitmap :Bitmap=bitmapDrawable.bitmap

                    //Creo l'uri per la bitmap corrispondente


                    /* Creo un file nella sottodirectory file/images (definita nel file xml paths)
                       con filesDir viene indicato il percorso assoluto nel filesystem dell'applicazione
                       in cui è presente la sottodirectory images*/
                    val imgfolder = File(filesDir, "images")

                    try{
                        //Creazione della directory in files/
                        imgfolder.mkdirs()
                    }catch(e: Exception){
                        Toast.makeText(this, "" + e.message, Toast.LENGTH_LONG).show()
                    }
                    //Creazione dell'uri di contenuto per ciascuna immagine salvata all'interno di files/images
                    val uri: Uri= getImageToShare(bitmap, namesImg[i],imgfolder) as Uri

                    //Aggiungo l'uri alla lista di Uri
                    uriArray.add(uri)
                }

                //Creo un intent per la condivisione delle immagini
                val shareIntent = Intent().apply {
                    action = Intent.ACTION_SEND_MULTIPLE
                    putParcelableArrayListExtra(Intent.EXTRA_STREAM, uriArray )
                    type = "image/png"
                }

                /*Avvia l'intent per la condivisione creando un selettore di attività
                  in cui l'utente può scegliere con quale applicazione può condividere le
                  immagini. E' stato impostato anche il titolo del selettore.
                 */
                startActivity(Intent.createChooser(shareIntent, "SharePlanets"))

            }else {
                Toast.makeText(this,"Seleziona almeno un elemento", Toast.LENGTH_SHORT).show()
            }

        }

        val backButton = findViewById<ImageView>(R.id.gallery_back_button)
        backButton.setOnClickListener{
            finish()
        }
    }

    private fun getImageToShare(bitmap: Bitmap, name:String, imagefolder:File): Uri? {

        var uri: Uri? = null
        try {
            //Creazione il file dell'immagine nella directory files/images
            val file = File(imagefolder, "$name.png")

            //Creazione di uno stream per la scrittura di dati su un file
            val outputStream = FileOutputStream(file)

            //Scrive una versione compressa della bitmap nello specifico stream
            bitmap.compress(Bitmap.CompressFormat.PNG, 90, outputStream)
            outputStream.flush()
            outputStream.close()

            //Recupera l'uri del file salvato in files/images
            uri = FileProvider.getUriForFile(this, packageName, file)
        } catch (e: Exception) {
            Toast.makeText(this, "" + e.message, Toast.LENGTH_LONG).show()
        }
        return uri
    }
}


