package com.example.memefinder

import android.R
import android.content.ContentValues.TAG
import android.content.Context
import android.os.Build
import android.util.Log
import android.util.Size
import android.view.View
import android.view.ViewGroup
import android.widget.AbsListView
import android.widget.BaseAdapter
import android.widget.ImageView
import androidx.annotation.RequiresApi
import androidx.core.net.toUri
import com.bumptech.glide.Glide


class ImageAdapter(private val context: Context, private val imageList: ArrayList<Image> = readListFromPref(context, com.example.memefinder.R.string.preference_file_key.toString())): BaseAdapter() {
    override fun getCount(): Int {
        return imageList.count()
    }

    override fun getItem(position: Int): Any {
        return position
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun getView(position: Int, view: View?, viewGroup: ViewGroup?): View {
        val picturesView: ImageView
        if (view == null) {
            picturesView = ImageView(context)
            picturesView.scaleType = ImageView.ScaleType.FIT_CENTER
            picturesView.layoutParams = AbsListView.LayoutParams(220, 220)
        } else {
            picturesView = view as ImageView
        }
        Log.d(TAG, "getView: $position")

        val thumbnail = imageList[position].uri?.let { (context).contentResolver.loadThumbnail(it.toUri(), Size(480, 480), null) }
        picturesView.setImageBitmap(thumbnail)

        return picturesView
    }

}