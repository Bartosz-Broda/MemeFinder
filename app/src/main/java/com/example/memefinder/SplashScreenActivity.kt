package com.example.memefinder

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.ContentUris
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import com.example.memefinder.adapter.Image
import com.google.android.gms.tasks.Tasks
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizerOptions
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main

class SplashScreenActivity : AppCompatActivity() {

    lateinit var loadingTextView: TextView
    var imageNumber = 0

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)

        lateinit var notificationManager: NotificationManager
        createNotificationChannel()

        val ivLoup = findViewById<ImageView>(R.id.ivLoupe)
        ivLoup.alpha = 0f

        // check if user has granted permission to access device external storage.
        // if not ask user for access to external storage.
        if (!checkSelfPermission()) {
            requestPermission()

        } else {
            // if permission granted, read images from storage.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ivLoup.animate().setDuration(1000).alpha(1f).withEndAction {
                    initUI()

                    //launching coroutines for multi-thread loading.
                    CoroutineScope(IO).launch {
                        val result = arrayListOf<Deferred<Boolean>>()
                        //launch 3 coroutines and wait for them to finish
                        for (i in 0..4) {
                            val mResult: Deferred<Boolean> = async { queryImageStorage(5, i) }
                            result.add(mResult)
                        }

                        if (result.all { it.await() }) {
                            Log.d(TAG, "onRequestPermissionsResult: HEHEHEHEHEH")
                            val bigList = ArrayList<Image>()
                            for (i in 1..5) {
                                val mList = readListFromPref(this@SplashScreenActivity, "images ${i-1}")
                                bigList.addAll(mList)
                                bigList.sortByDescending { Image -> Image.date }
                                writeListToPref(this@SplashScreenActivity, bigList, R.string.preference_file_key.toString())
                            }

                            val intent = Intent(applicationContext, MainActivity::class.java)
                            startActivity(intent)
                            finish()
                        }

                    }

                }
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
                            val ivLoup = findViewById<ImageView>(R.id.ivLoupe)
                            ivLoup.alpha = 0f
                            ivLoup.animate().setDuration(1000).alpha(1f).withEndAction {
                                initUI()

                                //launching coroutines for multi-thread loading.
                                CoroutineScope(IO).launch {
                                    val result = arrayListOf<Deferred<Boolean>>()
                                    //launch 5 coroutines and wait for them to finish
                                    for (i in 0..4) {
                                        val mResult: Deferred<Boolean> = async { queryImageStorage(5, i) }
                                        result.add(mResult)
                                    }

                                    if (result.all { it.await() }) {
                                        Log.d(TAG, "onRequestPermissionsResult: HEHEHEHEHEH")
                                        val bigList = ArrayList<Image>()
                                        for (i in 1..5) {
                                            val mList = readListFromPref(this@SplashScreenActivity, "images ${i-1}")
                                            bigList.addAll(mList)
                                            writeListToPref(this@SplashScreenActivity, bigList, R.string.preference_file_key.toString())
                                        }

                                        val intent = Intent(applicationContext, MainActivity::class.java)
                                        startActivity(intent)
                                        finish()
                                    }
                                }
                            }
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
    private suspend fun queryImageStorage(coroutineAmount: Int, coroutineNumber: Int): Boolean {
        var percentageloaded = 0
        val list = readListFromPref(this@SplashScreenActivity, "images $coroutineNumber").toList()
        Log.d(TAG, "queryImageStorage: ROZMIAR ${list.size}")

        val newList = readListFromPref(this@SplashScreenActivity, "images $coroutineNumber")

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

                //CODE FOR NOTIFICATION
                val builder = NotificationCompat.Builder(this, "1").apply {
                    setContentTitle("Loading memes...")
                    setContentText("Download in progress")
                    setSmallIcon(R.drawable.ic_launcher_foreground)
                    setPriority(NotificationCompat.PRIORITY_LOW)
                    setOnlyAlertOnce(true)
                }
                val PROGRESS_MAX = 100
                var PROGRESS_CURRENT = 0
                NotificationManagerCompat.from(this).apply {
                    // Issue the initial notification with zero progress
                    builder.setProgress(PROGRESS_MAX, PROGRESS_CURRENT, false)
                    val notificationId = 1
                    notify(notificationId, builder.build())

                while (it.moveToNext()) {
                    Log.d(
                        TAG,
                        "queryImageStorage: Hello xD $coroutineNumber, image: ${it.getLong(idColumn)}"
                    )
                    Log.d(
                        TAG,
                        "queryImageStorage: Hello, this is thread ${Thread.currentThread().name}"
                    )

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

                    //each coroutine takes care of different image, based on image id modulo coroutine amount.
                    if (id.toInt() % coroutineAmount == coroutineNumber) {

                        //make image object and add to list if it's not there already.
                        if (!list.any { Image -> Image.id == id } && height > 32 && width > 32) {

                            //process the image
                            kotlin.runCatching {
                                val inputImage = contentUri.let { it1 ->
                                    InputImage.fromFilePath(
                                        this@SplashScreenActivity,
                                        it1.toUri()
                                    )
                                }
                                val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
                                val result = recognizer.process(inputImage)

                                result.addOnSuccessListener { visionText ->
                                    // Task completed successfully
                                    if(visionText.text.isNotBlank()) {
                                        val image = Image(id, name, size, date, contentUri, visionText.text.uppercase())
                                        newList.add(0, image)
                                        Log.d(TAG, "queryImageStorage: SUCCESS ${visionText.text}")
                                        Log.d(TAG, "queryImageStorage: NEW LIST $newList")
                                        writeListToPref(
                                            this@SplashScreenActivity,
                                            newList,
                                            "images $coroutineNumber"
                                        )
                                        imageNumber += 1
                                    }
                                }

                                result.addOnFailureListener { e ->
                                    // Task failed with an exception
                                    Log.d(TAG, "queryImageStorage: FAILURE $e")
                                }

                                //Solves my bug!
                                Tasks.await(result)

                            }

                        } else {
                            imageNumber += 1
                            Log.d(TAG, "queryImageStorage: JUZ TAKI JEST")
                        }
                    }


                    //Updating textview with percentage
                    if (imagesAmount != null) {
                        percentageloaded = (imageNumber * 100 / imagesAmount)

                        // Update textview on main thread
                        updateUIOnMainThread(percentageloaded, imagesAmount)
                        //Log.d(TAG, "queryImageStorage: PROCENTY $imageNumber $imagesAmount")

                        //Code for updating notification
                        builder.setContentText("${percentageloaded} %" )
                        PROGRESS_CURRENT = percentageloaded
                        builder.setProgress(PROGRESS_MAX, PROGRESS_CURRENT, false)
                        notify(notificationId, builder.build())

                    }

                    }
                    // When done, update the notification one more time to remove the progress bar
                    builder.setContentText("Download complete")
                        .setProgress(0, 0, false)
                    notify(notificationId, builder.build())

                    //Log.d(TAG, "queryImageStorage: $imageNumber")
                }

            }

        }

        return true
    }


    private fun initUI() {
        val progressBar: ProgressBar = findViewById(R.id.progressBar)
        loadingTextView = findViewById(R.id.loadingPercentage)
        progressBar.visibility = View.VISIBLE
        loadingTextView.visibility = View.VISIBLE
        //Toast.makeText(this, "KURWA", Toast.LENGTH_SHORT).show()
        Log.d(TAG, "initUI: SIEMA")
    }

    @SuppressLint("SetTextI18n")
    private suspend fun updateUIOnMainThread(percentageloaded: Int, imagesAmount: Int?) {
        withContext(Main) {
            loadingTextView.text =
                "Processing images: $percentageloaded % \n($imageNumber / $imagesAmount)"
        }
    }

    private fun createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.channel_name)
            val descriptionText = getString(R.string.channel_description)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel("1", name, importance).apply {
                description = descriptionText
            }
            // Register the channel with the system
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    override fun onDestroy() {
        val mNotificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        mNotificationManager.cancel(1)
        super.onDestroy()
    }
}