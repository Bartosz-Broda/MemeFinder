package com.example.memefinder.fragment

import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.memefinder.R
import com.example.memefinder.adapter.Image
import com.example.memefinder.helper.ZoomOutPageTransformer

import android.util.Log
import android.widget.ImageButton
import androidx.core.net.toUri
import com.example.memefinder.readListFromPref
import java.io.File
import java.lang.Exception
import android.os.Environment





class GalleryFullscreenFragment : DialogFragment() {
    private var imageList = ArrayList<Image>()
    private val imageListKey: String = "listForFragment"
    private var selectedPosition: Int = 0
    lateinit var shareButton: ImageButton
    lateinit var deleteButton: ImageButton
    lateinit var viewPager: ViewPager
    lateinit var galleryPagerAdapter: GalleryPagerAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_gallery_fullscreen, container, false)
        viewPager = view.findViewById(R.id.viewPager)
        shareButton = view.findViewById(R.id.share_img_btn)
        deleteButton = view.findViewById(R.id.delete_img_btn)
        galleryPagerAdapter = GalleryPagerAdapter()
        imageList = activity?.let { readListFromPref(it, imageListKey) }!!
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
                //tvGalleryTitle.text = imageList[position].name
                shareButton.setOnClickListener {
                    Log.d(TAG, "onPageSelected: Share!")
                    shareThroughShareSheet(imageList[position])
                }
                deleteButton.setOnClickListener {
                    Log.d(TAG, "onPageSelected: Delete! :0")
                    try {
                        imageList[position].uri?.let { it1 -> deletePhoto(it1) }
                        imageList.drop(position)
                        Log.d(TAG, "onPageSelected: Image deleted!")
                    }catch (e:Exception){
                        e.printStackTrace()
                    }
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
    private fun shareThroughShareSheet(image: Image){
        val shareIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_STREAM, image.uri?.toUri())
            type = "image/*"
        }

        startActivity(Intent.createChooser(shareIntent, resources.getText(R.string.send_to)))
    }

    private fun deletePhoto(uri: String): Boolean{
        val fdelete = File(uri.toUri().path!!)
        return if (fdelete.exists()) {
            if (fdelete.delete()) {
                println("file Deleted :" + uri.toUri().path)
                true
            } else {
                println("file not Deleted :" + uri.toUri().path)
                false
            }
        } else{
            //Doesnt work!
            Log.d(TAG, "deletePhoto: No such file! URI: $uri")
            false
        }
    }


    override fun onPause() {
        //imageList.clear()
        super.onPause()
        Log.d(TAG, "onPause: xD")
    }

    override fun onResume() {
        //it works quite fast - must be used instead of bundle because list is too big for bundle
        //imageList = activity?.let { readListFromPref(it, "listForFragment") }!!
        Log.d(TAG, "onResume: ${imageList.size}")
        super.onResume()

        Log.d(TAG, "onResume: xD")
    }

}