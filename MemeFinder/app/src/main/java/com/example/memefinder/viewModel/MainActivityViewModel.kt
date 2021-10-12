package com.example.memefinder.viewModel

import android.content.ContentValues.TAG
import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.example.memefinder.adapter.Image
import com.example.memefinder.repositories.ImageRepository

class MainActivityViewModel(val savedStateHandle: SavedStateHandle) : ViewModel() {

    private var imageList: MutableLiveData<List<Image>>? = null
    private var mRepo: ImageRepository? = null
    private val KEY = "Saved_Image_List"
    private var items : MutableLiveData<String>? = null

    fun init(context: Context, savedStateHandle: SavedStateHandle) {
        if (imageList != null) {
            return
        }
        mRepo = ImageRepository.instance
        items = savedStateHandle.getLiveData(KEY)
        //Produces null......
        Log.d(TAG, "init: items: ${(items as MutableLiveData<String>).value}")
        imageList = mRepo!!.getImageList(context)
    }

    fun getListOfImages(): MutableLiveData<List<Image>>? {
        return imageList
    }

    fun overwriteListOfImages(context: Context, list: ArrayList<Image>) {
        mRepo?.overwriteListOfImages(context, list)
    }

    fun setQuery(query: String) {
        Log.d(TAG, "setQuery: query: $query")
        savedStateHandle[KEY] = query
    }

}