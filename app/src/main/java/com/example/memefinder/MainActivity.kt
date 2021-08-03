package com.example.memefinder

import android.content.ContentValues.TAG
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.widget.AdapterView.OnItemClickListener
import android.widget.GridView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.view.MenuItemCompat
import com.example.memefinder.adapter.Image
import com.example.memefinder.adapter.ImageAdapter
import com.example.memefinder.fragment.GalleryFullscreenFragment
import com.google.android.material.floatingactionbutton.FloatingActionButton


class MainActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val list = readListFromPref(this, R.string.preference_file_key.toString())
        Log.d(TAG, "onCreate: JESSS $list")
        Log.d(TAG, "onCreate: JESE ${list.size}" )

        showGallery(list)

    }


    private fun scanForMemes(text: String, list: ArrayList<Image> = readListFromPref(this, R.string.preference_file_key.toString())): ArrayList<Image> {
        val filteredList: ArrayList<Image> = ArrayList()
        for (image in list){
            if (image.text?.contains(text) == true){
                filteredList.add(image)
            }
        }
        return filteredList
    }

    private fun showGallery(list: ArrayList<Image>){
        val gallery: GridView = findViewById(R.id.galleryGridView)
        gallery.adapter = ImageAdapter(this, list)

        gallery.onItemClickListener =
            OnItemClickListener { arg0, arg1, position, arg3 ->
                if (list.isNotEmpty()) {
                    //Toast.makeText(applicationContext, "position " + position + " " + list[position], Toast.LENGTH_SHORT).show()
                    val bundle = Bundle()
                    bundle.putSerializable("images", list)
                    bundle.putInt("position", position)
                    val fragmentTransaction = supportFragmentManager.beginTransaction()
                    val galleryFragment = GalleryFullscreenFragment()
                    galleryFragment.arguments = bundle
                    galleryFragment.show(fragmentTransaction, "gallery")
                }
            }
    }

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

}