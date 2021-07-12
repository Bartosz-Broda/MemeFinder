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

    private fun requestPermission() {
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 6036)
    }

    private fun checkSelfPermission(): Boolean {

        return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
    }

    //assigns loaded data to imagesList and puts it in the parcelable for Main Activity
    @RequiresApi(Build.VERSION_CODES.Q)
    fun loadAllImages(){
        var imagesList = queryImageStorage()
        var intent = Intent(applicationContext, MainActivity::class.java)
        intent.putParcelableArrayListExtra("images", imagesList as java.util.ArrayList<out Parcelable>)
        startActivity(intent)
        //Log.d(TAG, "loadAllImages: $imagesList")
    }

    //Function for fetching all the data and creating Image objects, which are returned in a list.
    //I will pass them to the main activity and show on the screen
    @RequiresApi(Build.VERSION_CODES.Q)
    private fun queryImageStorage(): MutableList<Image> {
        val sharedPref = this.getSharedPreferences(
            getString(R.string.preference_file_key), Context.MODE_PRIVATE)
        val list: MutableList<Image> = ArrayList()

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
                    // make image object and add to list
                    var image = Image(id, name, size, date, contentUri)
                    list.add(image)
                    //Log.d("HEEEE", "KURDEE $list")

                    //TODO: Wykorzystać sharedPreferences zamiast tego. Mozna zrobic listener i bedzie ladnie plynnie ogarniac
                    writeListToPref(this, list as java.util.ArrayList<Image>)
                    //var listInString = Gson().toJson(list)
                    //sharedPref.edit().putString(R.string.preference_file_key.toString(), listInString).apply()
                    //if(list.size > 3000){
                     //   var intent = Intent(applicationContext, MainActivity::class.java)
                      //  intent.putParcelableArrayListExtra("images", list as java.util.ArrayList<out Parcelable>)
                     //   startActivity(intent)
                   // }
                    // generate the thumbnail -> will be moved somewhere else
                    //val thumbnail = (this as Context).contentResolver.loadThumbnail(contentUri, Size(480, 480), null)

                }
            } ?: kotlin.run {
                Log.e("TAG", "Cursor is null!")
            }
            var intent = Intent(applicationContext, MainActivity::class.java)
            startActivity(intent)
        }
        return list
    }
}