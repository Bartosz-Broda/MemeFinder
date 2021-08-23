package com.example.memefinder.fragment

import android.content.ContentResolver
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.memefinder.R
import com.example.memefinder.adapter.Image
import com.example.memefinder.helper.ZoomOutPageTransformer

import android.provider.MediaStore
import android.util.Log
import androidx.core.net.toUri
import com.example.memefinder.MainActivity


class GalleryFullscreenFragment : DialogFragment() {
    private var imageList = ArrayList<Image>()
    private var selectedPosition: Int = 0
    lateinit var tvGalleryTitle: TextView
    lateinit var viewPager: ViewPager
    lateinit var galleryPagerAdapter: GalleryPagerAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_gallery_fullscreen, container, false)
        viewPager = view.findViewById(R.id.viewPager)
        tvGalleryTitle = view.findViewById(R.id.tvGalleryTitle)
        galleryPagerAdapter = GalleryPagerAdapter()
        imageList = arguments?.getSerializable("images") as ArrayList<Image>
        selectedPosition = requireArguments().getInt("position")
        viewPager.adapter = galleryPagerAdapter
        viewPager.addOnPageChangeListener(viewPagerPageChangeListener)
        viewPager.setPageTransformer(true, ZoomOutPageTransformer())
        setCurrentItem(selectedPosition)
        return view
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, android.R.style.Theme_Black_NoTitleBar_Fullscreen)
    }
    private fun setCurrentItem(position: Int) {
        viewPager.setCurrentItem(position, false)
    }

    // viewpager page change listener
    private var viewPagerPageChangeListener: ViewPager.OnPageChangeListener =
        object : ViewPager.OnPageChangeListener {
            override fun onPageSelected(position: Int) {
                // set gallery title
                tvGalleryTitle.text = imageList[position].name
                tvGalleryTitle.setOnClickListener {
                    Log.d(TAG, "onPageSelected: click!")
                    shareOnMessenger(imageList[position])
                }
            }
            override fun onPageScrolled(arg0: Int, arg1: Float, arg2: Int) {
            }
            override fun onPageScrollStateChanged(arg0: Int) {
            }
        }

    // gallery adapter
    inner class GalleryPagerAdapter : PagerAdapter() {
        override fun instantiateItem(container: ViewGroup, position: Int): Any {
            val layoutInflater = activity?.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val view = layoutInflater.inflate(R.layout.image_fullscreen, container, false)
            val image = imageList[position]
            // load image
            Glide.with(context!!)
                .load(image.uri)
                .fitCenter()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(view.findViewById(R.id.ivFullscreenImage))
            container.addView(view)
            return view
        }
        override fun getCount(): Int {
            return imageList.size
        }
        override fun isViewFromObject(view: View, obj: Any): Boolean {
            return view === obj as View
        }
        override fun destroyItem(container: ViewGroup, position: Int, obj: Any) {
            container.removeView(obj as View)
        }
    }

    //using share sheet for sharing image
    private fun shareOnMessenger(image: Image){
        val shareIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_STREAM, image.uri?.toUri())
            type = "image/*"
        }
        startActivity(Intent.createChooser(shareIntent, resources.getText(R.string.send_to)))
    }

}