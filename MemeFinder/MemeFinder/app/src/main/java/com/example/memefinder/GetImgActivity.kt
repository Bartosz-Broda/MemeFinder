package com.example.memefinder

import android.Manifest
import android.annotation.SuppressLint
import android.content.ContentUris
import android.content.ContentValues.TAG
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class GetImgActivity : AppCompatActivity() {
    internal var TIME_OUT = 800

    lateinit var loadingTextView: TextView

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_get_img)
        // check if user has granted permission to access device external storage.
        // if not ask user for access to external storage.
        if (!checkSelfPermission()) {
            requestPermission()
            val intent = intent
            finish()
            startActivity(intent)
        } else {
            // if permission granted, read images from storage.
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    initUI()
                    Thread{queryImageStorage()}.start()
                }
            }
    }

    //TODO: PROCENTY MAJA SIE LICZYC!


    private fun requestPermission() {
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 6036)
    }

    private fun checkSelfPermission(): Boolean {

        return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
    }

    //Function for fetching all the data and creating Image objects, which are returned in a list.
    //I will pass them to the main activity and show on the screen
    @SuppressLint("SetTextI18n")
    @RequiresApi(Build.VERSION_CODES.Q)
    private fun queryImageStorage() {
            var imageNumber = 0
            var percentageloaded = 0
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
                        if (!list.contains(image)) {
                            list.add(image)
                            Log.d("HEEEE", "KURDEE $list")
                            writeListToPref(this, list)
                        }
                        imageNumber += 1
                        //Updating textview with percentage
                        if (imagesAmount != null) {
                            percentageloaded = (imageNumber *100/ imagesAmount)
                            //Log.d(TAG, "queryImageStorage: PROCENTY $imageNumber $imagesAmount")
                        }
                        Handler(Looper.getMainLooper()).post(Runnable {
                            loadingTextView.text = "Loading images: $percentageloaded %"
                        })
                        try {
                            Thread.sleep(0,1)
                        } catch (e: InterruptedException) {
                            e.printStackTrace()
                        }

                        //Log.d(TAG, "queryImageStorage: $imageNumber")

                        //TODO: Dzia??a sharedpreferences. Przy 1 uruchomienu laduje wszystko, przy kolejnych tylko nowe zdjecia. Do zrobienia Listener zeby dzia??a??o p??ynnie.
                    }

                }
            } ?: kotlin.run {
                Log.e("TAG", "Cursor is null!")
            }
            val intent = Intent(applicationContext, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

    private fun initUI(){
        val progressBar: ProgressBar = findViewById(R.id.progressBar)
        loadingTextView = findViewById(R.id.loadingPercentage)
        progressBar.visibility = View.VISIBLE
        loadingTextView.visibility = View.VISIBLE
        //Toast.makeText(this, "KURWA", Toast.LENGTH_SHORT).show()
        Log.d(TAG, "initUI: SIEMA")
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    fun runCoroutines() = runBlocking { // this: CoroutineScope
        launch { // launch a new coroutine and continue
            //delay(5000L) // non-blocking delay for 1 second (default time unit is ms)
            println("World!") // print after delay
            Log.d(TAG, "runCoroutines: World")
            //initUI()
            queryImageStorage()
        }
        println("Hello") // main coroutine continues while a previous one is delayed
        Log.d(TAG, "runCoroutines: HELLO")
        initUI()

    }
}