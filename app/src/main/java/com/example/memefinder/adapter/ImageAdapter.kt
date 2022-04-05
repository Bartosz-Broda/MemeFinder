package com.example.memefinder.adapter

import android.content.ContentValues.TAG
import android.content.Context
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.AbsListView
import android.widget.BaseAdapter
import android.widget.ImageView
import androidx.annotation.RequiresApi
import androidx.core.net.toUri
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.memefinder.R
import com.example.memefinder.readListFromPref
import java.io.IOException

class ImageAdapter(
    private val context: Context,
    private val imageList: ArrayList<Image> = readListFromPref(
        context,
        R.string.preference_file_key.toString()
    ),
    private val width: Int
): BaseAdapter() {
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

        val requestOptions = RequestOptions()

        val picturesView: ImageView
        if (view == null) {
            picturesView = ImageView(context)
            picturesView.scaleType = ImageView.ScaleType.CENTER_CROP
            val columnWidth = width/3 - 4
            picturesView.layoutParams = AbsListView.LayoutParams(columnWidth, columnWidth)

        /* val metrics = DisplayMetrics()

            when (metrics.densityDpi) {
                DisplayMetrics.DENSITY_LOW -> imageView.setLayoutParams(
                    AbsListView.LayoutParams(
                        lowVal,
                        lowVal
                    )
                )
                DisplayMetrics.DENSITY_MEDIUM -> imageView.setLayoutParams(
                    AbsListView.LayoutParams(
                        medVal,
                        medVal
                    )
                )
                DisplayMetrics.DENSITY_HIGH -> imageView.setLayoutParams(
                    AbsListView.LayoutParams(
                        highVal,
                        highVal
                    )
                )
            }*/
        } else {
            picturesView = view as ImageView
        }
        Log.d(TAG, "getView: $position")

        //val thumbnail = imageList[position].uri?.let { (context).contentResolver.loadThumbnail(it.toUri(), Size(330, 330), null) }
        //picturesView.setImageBitmap(thumbnail)

        //checks if the file still exists. Makes the whole app shattering and lagging but works
        /*val isExist = try {
            context.contentResolver.openInputStream(imageList[position].uri?.toUri()!!)?.use {
            }
            true
        }
        catch (e: IOException) {
            false
        }

        if(isExist) {
            Glide.with(context)
                .load(imageList[position].uri?.toUri())
                .placeholder(R.drawable.ic_baseline_image_24)
                .thumbnail(
                    Glide.with(context).load(imageList[position].uri?.toUri())
                        .apply(RequestOptions().override(1))
                )
                .apply(requestOptions).into(picturesView)
        }*/
        Glide.with(context)
            .load(imageList[position].uri?.toUri())
            .placeholder(R.drawable.ic_baseline_image_24)
            .thumbnail(
                Glide.with(context).load(imageList[position].uri?.toUri())
                    .apply(RequestOptions().override(1))
            )
            .apply(requestOptions).into(picturesView)

            return picturesView
    }

}