package com.example.memefinder

import android.Manifest
import android.annotation.SuppressLint
import android.content.ContentUris
import android.content.ContentValues.TAG
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import com.example.memefinder.adapter.Image
import com.google.android.gms.tasks.Tasks
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizerOptions
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.io.IOException
import java.util.concurrent.Executors

class GetImgActivity : AppCompatActivity() {
    internal var TIME_OUT = 800

    lateinit var loadingTextView: TextView

    // Create an executor that executes tasks in a background thread.
    val backgroundExecutor = Executors.newSingleThreadScheduledExecutor()

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_get_img)

        // check if user has granted permission to access device external storage.
        // if not ask user for access to external storage.
        if (!checkSelfPermission()) {
            requestPermission()

        } else {
            // if permission granted, read images from storage.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                initUI()
                backgroundExecutor.execute { queryImageStorage() }
            }
        }
    }

    private fun requestPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
            6036
        )
    }

    private fun checkSelfPermission(): Boolean {

        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            6036 -> {
                if (grantResults.isNotEmpty()) {
                    val permissionGranted = grantResults[0] == PackageManager.PERMISSION_GRANTED
                    if (permissionGranted) {
                        // Now we are ready to access device storage and read images stored on device.
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            initUI()
                            backgroundExecutor.execute { queryImageStorage() }
                        }
                    } else {
                        Toast.makeText(
                            this,
                            "Permission Denied! Cannot load images.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    //Function for fetching all the data and creating Image objects, which are returned in a list.
    //I will pass them to the main activity and show on the screen
    @SuppressLint("SetTextI18n")
    @RequiresApi(Build.VERSION_CODES.Q)
    private fun queryImageStorage() {
        var imageNumber = 0
        var percentageloaded = 0
        val list = readListFromPref(this, R.string.preference_file_key.toString()).toList()
        Log.d(TAG, "queryImageStorage: ROZMIAR ${list.size}")

        val newList = readListFromPref(this, R.string.preference_file_key.toString())

        val imageProjection = arrayOf(
            MediaStore.Images.Media.DISPLAY_NAME,
            MediaStore.Images.Media.SIZE,
            MediaStore.Images.Media.DATE_TAKEN,
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.HEIGHT,
            MediaStore.Images.Media.WIDTH
        )
        val imageSortOrder = "${MediaStore.Images.Media.DATE_TAKEN} ASC"

        val cursor = contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            imageProjection,
            null,
            null,
            imageSortOrder
        )
        cursor.use { it ->
            val imagesAmount = cursor?.count
            //Log.d(TAG, "queryImageStorage: $x")
            it?.let { it ->
                val idColumn = it.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
                val nameColumn = it.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)
                val sizeColumn = it.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE)
                val dateColumn = it.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_TAKEN)
                val height = it.getColumnIndexOrThrow(MediaStore.Images.Media.HEIGHT)
                val width = it.getColumnIndexOrThrow(MediaStore.Images.Media.WIDTH)

                while (it.moveToNext()) {
                    //TODO: Dodać worker threads (np. 3 lub 4) zeby wiele obrazow pzerabiac naraz.

                    val id = it.getLong(idColumn)
                    val name = it.getString(nameColumn)
                    val size = it.getString(sizeColumn)
                    val date = it.getString(dateColumn)
                    val height = it.getInt(height)
                    val width = it.getInt(width)
                    val contentUri = ContentUris.withAppendedId(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        id
                    ).toString()

                    // make image object and add to list if it's not there already.
                    //TODO: Dodac skanowanie obrazów z unused stuff
                    if (!list.any { Image -> Image.id == id } && height > 32 && width >32) {

                        //process the image
                        try {
                            val inputImage =
                                contentUri.let { it1 -> InputImage.fromFilePath(this, it1.toUri()) }
                            val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
                            val result = recognizer.process(inputImage)

                            result.addOnSuccessListener { visionText ->
                                // Task completed successfully
                                val image = Image(id, name, size, date, contentUri, visionText.text.uppercase())

                                newList.add(0, image)
                                Log.d(TAG, "queryImageStorage: SUCCESS ${visionText.text}")
                                Log.d(TAG, "queryImageStorage: NEW LIST $newList")
                                writeListToPref(this, newList, R.string.preference_file_key.toString())
                                imageNumber += 1
                            }

                            result.addOnFailureListener { e ->
                                // Task failed with an exception
                                Log.d(TAG, "queryImageStorage: FAILURE $e")
                            }

                            //Solves my bug!
                            Tasks.await(result)

                        } catch (e: IOException) {
                            Log.d(TAG, "queryImageStorage: DUPA")
                            e.printStackTrace()
                        }

                    } else {
                        imageNumber += 1
                        Log.d(TAG, "queryImageStorage: JUZ TAKI JEST")
                    }

                    //Updating textview with percentage
                    if (imagesAmount != null) {
                        percentageloaded = (imageNumber * 100 / imagesAmount)
                        //Log.d(TAG, "queryImageStorage: PROCENTY $imageNumber $imagesAmount")
                    }

                    // Create an executor that executes tasks in the main thread.
                    mainExecutor.execute {
                        loadingTextView.text = "Loading images: $percentageloaded % \n($imageNumber / $imagesAmount)"
                    }

                    //Log.d(TAG, "queryImageStorage: $imageNumber")
                    //Działa sharedpreferences. Przy 1 uruchomienu laduje wszystko, przy kolejnych tylko nowe zdjecia. Do zrobienia Listener zeby działało płynnie.
                }

            }
        } ?: kotlin.run {
            Log.e("TAG", "Cursor is null!")
        }
        val intent = Intent(applicationContext, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun initUI() {
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