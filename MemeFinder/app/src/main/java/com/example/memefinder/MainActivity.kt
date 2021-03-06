package com.example.memefinder

import android.annotation.SuppressLint
import android.content.*
import android.content.ContentValues.TAG
import android.os.Build
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.Gravity
import android.view.Menu
import android.widget.AdapterView.OnItemClickListener
import android.widget.FrameLayout
import android.widget.GridView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModelProvider
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.memefinder.adapter.Image
import com.example.memefinder.adapter.ImageAdapter
import com.example.memefinder.fragment.GalleryFullscreenFragment
import com.example.memefinder.viewModel.MainActivityViewModel
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.BaseTransientBottomBar.LENGTH_LONG
import com.google.android.material.snackbar.Snackbar

// ODPALIć SNACKBAR PO ZALADOWANIU OBRAZKA

class MainActivity : AppCompatActivity(), SharedPreferences.OnSharedPreferenceChangeListener {
    private var viewModel: MainActivityViewModel? = null

    @SuppressLint("ShowToast")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        writeStringToPref(this, "0", "isBroadcastSent")
        /*val list = readListFromPref(this, R.string.preference_file_key.toString())
        Log.d(TAG, "onCreate: JESSS $list")
        Log.d(TAG, "onCreate: JESE ${list.size}" )*/

        // Create a viewModel
        viewModel = ViewModelProvider(this).get(MainActivityViewModel::class.java)
        viewModel!!.init(this, SavedStateHandle())
        viewModel!!.getListOfImages()?.observe(this, {
            showGallery(it as ArrayList<Image>)
            Log.d(TAG, "onCreate: it size: ${it.size}")
        })
        refreshApp()


        val localBroadcastManager = LocalBroadcastManager.getInstance(this)
        localBroadcastManager.registerReceiver(receiver, IntentFilter("new meme"))

        Log.d(TAG, "onClick: String read from SharedPref:" + readStringFromPref(this, "isGalleryOpen"))
    }


    private fun scanForMemes(text: String, list: ArrayList<Image> = readListOfImagesFromPref(this, R.string.preference_file_key.toString())): ArrayList<Image> {
        viewModel?.setQuery(text)

        val filteredList: ArrayList<Image> = ArrayList()
        for (image in list){
            if (image.text?.contains(text) == true){
                filteredList.add(image)
            }
        }
        return filteredList
    }

    private fun showGallery(list: ArrayList<Image>){
        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        val width = displayMetrics.widthPixels

            val gallery: GridView = findViewById(R.id.galleryGridView)
            gallery.adapter = ImageAdapter(this, list, width)

            gallery.onItemClickListener =
                OnItemClickListener { arg0, arg1, position, arg3 ->
                    if (list.isNotEmpty()) {
                        writeListOfImagesToPref(this, list, "listForFragment")
                        val bundle = Bundle()
                        bundle.putInt("position", position)
                        val fragmentTransaction = supportFragmentManager.beginTransaction()
                        val galleryFragment = GalleryFullscreenFragment()
                        galleryFragment.arguments = bundle
                        galleryFragment.show(fragmentTransaction, "gallery")
                    }
                }
    }

    //searching menu on toolbar
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.searchbar_menu, menu)
        val item = menu.findItem(R.id.action_search)
        val searchView = item.actionView as SearchView
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                showGallery(scanForMemes(query.uppercase()))
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                //filter as you type
                showGallery(scanForMemes(newText.uppercase()))
                return false
            }
        })
        return super.onCreateOptionsMenu(menu)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        Log.d(TAG, "onSharedPreferencesChanged: CHANGE!")
        val list = readListOfImagesFromPref(this, R.string.preference_file_key.toString())
        showGallery(list)
        when (key){
            getString(R.string.preference_file_key) -> {
                val list = readListOfImagesFromPref(this, R.string.preference_file_key.toString())
                showGallery(list)
                Log.d(TAG, "onSharedPreferencesChanged: CHANGE!!!")
            }
        }
    }

    private fun refreshApp(){
        val swipeToRefresh = findViewById<SwipeRefreshLayout>(R.id.swipeToRefresh)
        swipeToRefresh.setOnRefreshListener {
            sendBroadcast()
            writeStringToPref(this, "0", "isBroadcastSent")
            //Toast.makeText(this, "Memes refreshed", Toast.LENGTH_SHORT).show()
            swipeToRefresh.isRefreshing = false
        }
    }

    //send info about refreshing memes
    private fun sendBroadcast() {
        val intent = Intent("refresh memes")
        intent.putExtra("EdRfTg123", "SAVE!")
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
        Log.d(TAG, "sendBroadcast: SENT")
    }

    //receiver for displaying info(snackbar) about new memes
    private var receiver: BroadcastReceiver = object : BroadcastReceiver() {
        @RequiresApi(Build.VERSION_CODES.N)
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent != null) {
                Log.d(TAG, "onReceive: Broadcast received, intent != null")
                val str = intent.getStringExtra("EdRfTg1234")

                if(str.equals("Show Snackbar!")){
                    /*val mySnackbar = Snackbar.make(findViewById(R.id.myCoordinatorLayout), "snackbar_message", LENGTH_LONG)
                    mySnackbar.show()*/
                    showSnackbar()
                }
            }
            Log.d(TAG, "onReceive: Broadcast received, intent == null")
        }
    }

    private fun showSnackbar(){
        val snackBarView = Snackbar.make(findViewById(R.id.myCoordinatorLayout), "I found new memes! Swipe down to refresh" , 8000)
        val view = snackBarView.view
        val params = view.layoutParams as CoordinatorLayout.LayoutParams
        params.gravity = Gravity.TOP
        view.layoutParams = params
        //view.background = ContextCompat.getDrawable(this,R.drawable.custom_drawable) // for custom background
        snackBarView.animationMode = BaseTransientBottomBar.ANIMATION_MODE_FADE
        snackBarView.show()
    }

    override fun onPause() {
        Log.d(TAG, "onPause: xDD")
        super.onPause()
    }

    override fun onStart() {
        super.onStart()
        registerSharedPref(this, this)
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterSharedPref(this, this)
    }

    override fun onBackPressed() {
        writeStringToPref(this, "0", "isGalleryOpen")
        super.onBackPressed()
        finish()
    }
}