package com.example.memefinder.helper

/*import android.content.ContentValues
import android.util.Log
import androidx.core.net.toUri
import com.example.memefinder.Image
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