package com.example.memefinder

import android.content.ContentValues.TAG
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.AdapterView.OnItemClickListener
import android.widget.GridView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizerOptions
import java.io.IOException


class MainActivity : AppCompatActivity(), SharedPreferences.OnSharedPreferenceChangeListener {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val list = readListFromPref(this, R.string.preference_file_key.toString())
        Log.d(TAG, "onCreate: JESSS $list")
        Log.d(TAG, "onCreate: JESE ${list.size}" )

        var gallery: GridView = findViewById(R.id.galleryGridView)
        gallery.adapter = ImageAdapter(this, list)

        gallery.onItemClickListener =
            OnItemClickListener { arg0, arg1, position, arg3 ->
                if (list.isNotEmpty()) Toast.makeText(
                    applicationContext,
                    "position " + position + " " + list[position],
                    Toast.LENGTH_SHORT
                ).show()
            }

        //scanForMemes("PTASZNIK")

    }


    fun scanForMemes(text: String){
        val list = readListFromPref(this, R.string.preference_file_key.toString())
        //creating input image from uri
        for (item in list) {
            val inputImage: InputImage
            try {
                inputImage = item.uri?.let { InputImage.fromFilePath(this, it.toUri()) }!!
                val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
                val result = recognizer.process(inputImage)
                    .addOnSuccessListener { visionText ->
                        // Task completed successfully
                        if (visionText.toString().contains(text)){
                            Log.d(TAG, "scanForMemes: BEKA $visionText")
                        }
                    }
                    .addOnFailureListener { e ->
                        // Task failed with an exception
                        Log.d(TAG, "scanForMemes: $e")
                    }
            } catch (e: IOException) {
                e.printStackTrace()
            }


        }
    }









    //Doesn't work, don't know why. Perhaps i have to run both activities at once, until all data will be loaded
    //As for now, i don't use listener, i just load everything at the first start and then only new pictures are loaded.
    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        when (key){
            getString(R.string.preference_file_key) -> {
                val list = readListFromPref(this, R.string.preference_file_key.toString())
                Log.d(TAG, "onCreate: JESTT $list")
            }
        }
    }

    override fun onStart() {
        super.onStart()
        registerSharedPref(this, this)
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterSharedPref(this, this)
    }

}