package com.example.memefinder

import android.content.Context
import android.media.MediaScannerConnection
import android.os.Build
import android.os.Environment
import android.util.Log
import androidx.annotation.RequiresApi
import com.example.memefinder.adapter.Image
import java.io.File
import java.util.function.Predicate
import android.content.Intent
import android.net.Uri


@RequiresApi(Build.VERSION_CODES.N)
    fun updateListOfID(ID: Long, context: Context){
        var listOfID = readListofIDFromPref(context)
        val idToDelete = Predicate { id: Long -> id == ID }
        remove(listOfID, idToDelete)
        writeListOfIDToPref(context, listOfID)
    }

    @RequiresApi(Build.VERSION_CODES.N)
    fun removeDeletedMemesFromMemory(context: Context, listOfMemes: ArrayList<Image>, listOfId: ArrayList<Long> = readListofIDFromPref(context), coroutineNumber: Int = 0) {
        if(coroutineNumber == 0){
            val notDeletedMeme = Predicate { image: Image -> image.id in listOfId }
            removeIfNot(listOfMemes, notDeletedMeme)

            deleteListFromSharedPref(context, "images")

            writeListOfImagesToPref(
                context,
                listOfMemes,
                "images"
            )
        }else {
            val notDeletedMeme = Predicate { image: Image -> image.id in listOfId }
            removeIfNot(listOfMemes, notDeletedMeme)

            deleteListFromSharedPref(context, "images")

            writeListOfImagesToPref(
                context,
                listOfMemes,
                "images $coroutineNumber"
            )
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    fun removeImagesWithoutText(listOfImages: ArrayList<Image>){
        val notMemes = Predicate { image: Image -> image.text==""}
        remove(listOfImages,notMemes)
    }

    @RequiresApi(Build.VERSION_CODES.N)
    fun <T> remove(list: MutableList<T>, predicate: Predicate<T>) {
        list.removeIf { x: T -> predicate.test(x) }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    fun <T> removeIfNot(list: MutableList<T>, predicate: Predicate<T>) {
        list.removeIf { x: T -> !predicate.test(x) }
    }

    fun deleteImage(path: String, context: Context) {
        val fDelete = File(path)
        if (fDelete.exists()) {
            if (fDelete.delete()) {
                MediaScannerConnection.scanFile(context, arrayOf(Environment.getExternalStorageDirectory().toString()), null) { path, uri ->
                    Log.d("debug", "DONE")
                }
                // request scan
                val scanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
                scanIntent.data = Uri.fromFile(fDelete)
                context.sendBroadcast(scanIntent)
            }
        }
    }
