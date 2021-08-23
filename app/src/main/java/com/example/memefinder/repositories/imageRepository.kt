package com.example.memefinder.repositories

import android.content.Context
import androidx.lifecycle.MutableLiveData
import com.example.memefinder.R
import com.example.memefinder.adapter.Image
import com.example.memefinder.readListFromPref

class ImageRepository {
    var data: MutableLiveData<List<Image>> = MutableLiveData()

    //Kotlin singleton
    companion object {
        val instance = ImageRepository()
    }

    fun getImageList(context: Context):MutableLiveData<List<Image>> {
        val list = readListFromPref(context, R.string.preference_file_key.toString())
        data.value = list
        return data
    }

}