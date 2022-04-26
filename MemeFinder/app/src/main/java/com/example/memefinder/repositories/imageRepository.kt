package com.example.memefinder.repositories

import android.content.ContentValues.TAG
import android.content.Context
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.example.memefinder.R
import com.example.memefinder.adapter.Image
import com.example.memefinder.readListOfImagesFromPref
import com.example.memefinder.writeListOfImagesToPref

class ImageRepository {
    var data: MutableLiveData<List<Image>> = MutableLiveData()

    //Kotlin singleton
    companion object {
        val instance = ImageRepository()
    }

    fun getImageList(context: Context):MutableLiveData<List<Image>> {
        val list = readListOfImagesFromPref(context, R.string.preference_file_key.toString())
        /*val result = CoroutineScope(IO).async {
            for(item in list){
                if (item.text == ""){
                    list.drop(list.indexOf(item))
                }
                Log.d(TAG, "getImageList: Dropping...")
            }
            return@async true
        }

        while (!result.await()){

        }*/

        Log.d(TAG, "getImageList: after dropping")
        data.value = list
        return data
    }

    fun overwriteListOfImages(context: Context, list: ArrayList<Image>) {
        writeListOfImagesToPref(context, list, R.string.preference_file_key.toString())
    }

}