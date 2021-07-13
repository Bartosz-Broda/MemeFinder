package com.example.memefinder

import android.Manifest
import android.content.ContentUris
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Parcelable
import android.provider.MediaStore
import android.util.Log
import android.util.Size
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.gson.Gson

class GetImgActivity : AppCompatActivity() {
    internal var SPLASH_TIME_OUT = 800


    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_get_img)

        Handler().postDelayed(
            {
                // check if user has granted permission to access device external storage.
                // if not ask user for access to external storage.
                if (!checkSelfPermission()) {
                    requestPermission()
                } else {
                    // if permission granted read images from storage.
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        Toast.makeText(this, "KURWA", Toast.LENGTH_SHORT).show()
                        queryImageStorage()
                    }
                }
            }, SPLASH_TIME_OUT.toLong()
        )
    }

    //TODO: PROCENTY MAJA SIE LICZYC!

    //val progressBar = findViewById<ProgressBar>(R.id.progressBar)!!
    //val loadingTextView = findViewById<TextView>(R.id.loadingPercentage)!!


    private fun requestPermission() {
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 6036)
    }

    private fun checkSelfPermission(): Boolean {

        return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
    }

    //Function for fetching all the data and creating Image objects, which are returned in a list.
    //I will pass them to the main activity and show on the screen
    @RequiresApi(Build.VERSION_CODES.Q)
    private fun queryImageStorage() {
        //progressBar.visibility = View.VISIBLE
        //progressBar2.visibility = View.VISIBLE
        var imageNumber = 0
        val list = readListFromPref(this)

        val imageProjection = arrayOf(
            MediaStore.Images.Media.DISPLAY_NAME,
            MediaStore.Images.Media.SIZE,
            MediaStore.Images.Media.DATE_TAKEN,
            MediaStore.Images.Media._ID
        )

        val imageSortOrder = "${MediaStore.Images.Media.DATE_TAKEN} DESC"

        val cursor = contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            imageProjection,
            null,
            null,
            imageSortOrder
        )

        cursor.use {
            val imagesAmount = cursor?.count
            //Log.d(TAG, "queryImageStorage: $x")
            it?.let {
                val idColumn = it.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
                val nameColumn = it.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)
                val sizeColumn = it.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE)
                val dateColumn = it.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_TAKEN)

                while (it.moveToNext()) {
                    val id = it.getLong(idColumn)
                    val name = it.getString(nameColumn)
                    val size = it.getString(sizeColumn)
                    val date = it.getString(dateColumn)

                    val contentUri = ContentUris.withAppendedId(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        id
                    ).toString()
                    // make image object and add to list if it's not there yet.
                    val image = Image(id, name, size, date, contentUri)
                    if (!list.contains(image)){
                        list.add(image)
                        Log.d("HEEEE", "KURDEE $list")
                        writeListToPref(this, list as java.util.ArrayList<Image>)
                    }
                    imageNumber += 1
                    Log.d(TAG, "queryImageStorage: $imageNumber")

                    //TODO: Działa sharedpreferences. Przy 1 uruchomienu laduje wszystko, przy kolejnych tylko nowe zdjecia. Do zrobienia Listener zeby działało płynnie.

                }
            } ?: kotlin.run {
                Log.e("TAG", "Cursor is null!")
            }
            val intent = Intent(applicationContext, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}