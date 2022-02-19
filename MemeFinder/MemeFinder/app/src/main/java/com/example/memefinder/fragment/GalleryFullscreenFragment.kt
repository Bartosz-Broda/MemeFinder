package com.example.memefinder.fragment

import android.app.Activity.RESULT_OK
import android.app.RecoverableSecurityException
import android.content.ContentResolver
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.net.toUri
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.memefinder.MainActivity
import com.example.memefinder.R
import com.example.memefinder.adapter.Image
import com.example.memefinder.helper.ZoomOutPageTransformer
import com.example.memefinder.readListFromPref
import java.io.File
import androidx.lifecycle.ViewModelProviders
import com.example.memefinder.viewModel.MainActivityViewModel
import com.example.memefinder.writeListToPref


class GalleryFullscreenFragment : DialogFragment() {
    private var imageList = ArrayList<Image>()
    private val imageListKey: String = "listForFragment"
    private var selectedPosition: Int = 0
    lateinit var shareButton: ImageButton
    lateinit var deleteButton: ImageButton
    lateinit var viewPager: ViewPager
    lateinit var galleryPagerAdapter: GalleryPagerAdapter
    lateinit var mUri: String
    private lateinit var intentSenderLauncher: ActivityResultLauncher<IntentSenderRequest>
    private var viewModel: MainActivityViewModel? = null

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

        intentSenderLauncher = registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) {
            if(it.resultCode == RESULT_OK) {
                if(Build.VERSION.SDK_INT == Build.VERSION_CODES.Q) {
                    deletePhoto(mUri)
                }
                Toast.makeText(context, "Photo deleted successfully", Toast.LENGTH_SHORT).show()

            } else {
                Toast.makeText(context, "Photo couldn't be deleted", Toast.LENGTH_SHORT).show()
            }
        }

        return view
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, android.R.style.Theme_Black_NoTitleBar_Fullscreen)
        viewModel = ViewModelProvider(requireActivity()).get(MainActivityViewModel::class.java)
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
        return try{
            context?.contentResolver?.delete(uri.toUri(), null, null)
            Log.d(TAG, "deletePhoto: Success! Photo deleted!")
            true
        }
        catch(e: SecurityException){

            val intentSender = when{
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.R -> {
                    MediaStore.createDeleteRequest(context?.contentResolver!!, listOf(uri.toUri())).intentSender
                }
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> {
                    mUri = uri
                    val recoverableSecurityException = e as? RecoverableSecurityException
                    recoverableSecurityException?.userAction?.actionIntent?.intentSender
                }
                else -> null
            }
            intentSender.let { sender ->
                intentSenderLauncher.launch(
                    sender?.let { IntentSenderRequest.Builder(it).build() }
                )
            }

            e.printStackTrace()
            Log.d(TAG, "deletePhoto: Can't delete image!")
            true
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