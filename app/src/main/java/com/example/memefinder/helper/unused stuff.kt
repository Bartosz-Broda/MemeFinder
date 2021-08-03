package com.example.memefinder.helper

/*import android.content.ContentValues
import android.util.Log
import androidx.core.net.toUri
import com.example.memefinder.adapter.Image
import com.example.memefinder.R
import com.example.memefinder.writeListToPref
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizerOptions

//Scanning image for text
val inputImage: InputImage
try {
    inputImage =
        contentUri.let { InputImage.fromFilePath(this, it.toUri()) }
    val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    val result = recognizer.process(inputImage)
        .addOnSuccessListener { visionText ->
            // Task completed successfully
            // if (visionText.toString().contains(text)){
            // }
            val image = Image(id, name, size, date, contentUri, visionText.text)
            newList.add(image)
            Log.d(ContentValues.TAG, "queryImageStorage: SUCCESS ${visionText.text}")
            Log.d("HEEEE", "KURDEE $newList")
            writeListToPref(this, newList, R.string.preference_newList_key.toString())
        }
        .addOnFailureListener { e ->
            val image = Image(id, name, size, date, contentUri)
            newList.add(image)
            Log.d("HEEEE", "KURDEE $newList")
            writeListToPref(this, newList, R.string.preference_newList_key.toString())
            Log.d(ContentValues.TAG, "queryImageStorage: FAILURE $e")
            // Task failed with an exception
        }
    Thread.sleep(3)

} catch (e: IOException) {
    e.printStackTrace()
}*/


/*f (imageAdded > 100){
    imageAdded = 0
    list = readListFromPref(this, R.string.preference_file_key.toString()).toList()
    newList = readListFromPref(this, R.string.preference_file_key.toString())
    Log.d(TAG, "queryImageStorage: xDDDDDDDDDD")
}*/


/*
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
 */