package com.example.memefinder

import android.R
import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.AbsListView
import android.widget.BaseAdapter
import android.widget.ImageView
import com.bumptech.glide.Glide


class ImageAdapter(val context: Context, val imageList: ArrayList<Image> = readListFromPref(context)): BaseAdapter() {
    override fun getCount(): Int {
        return imageList.count()
    }

    override fun getItem(position: Int): Any {
        return position
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, view: View?, viewGroup: ViewGroup?): View {
        val picturesView: ImageView
        if (view == null) {
            picturesView = ImageView(context)
            picturesView.scaleType = ImageView.ScaleType.FIT_CENTER
            picturesView.layoutParams = AbsListView.LayoutParams(270, 270)
        } else {
            picturesView = view as ImageView
        }

        Glide.with(context).load(imageList[position])
            .placeholder(R.drawable.ic_menu_report_image).centerCrop()
            .into(picturesView)

        return picturesView
    }

}