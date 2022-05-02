package com.example.memefinder

import android.Manifest
import android.annotation.SuppressLint
import android.content.*
import android.content.ContentValues.TAG
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
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.memefinder.adapter.Image
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizerOptions
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.Main
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class SplashScreenActivity : AppCompatActivity(), View.OnClickListener {

    lateinit var loadingTextView: TextView
    private lateinit var galleryButton: Button
    var imageNumber = 0
    private var isMainActivityOpen = 0
    private var isBroadcastSent = 0
    var isUIInitiated = 0

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)
        val ivLoup = findViewById<ImageView>(R.id.ivLoupe)
        ivLoup.alpha = 0f
        writeStringToPref(this@SplashScreenActivity, "0", "isGalleryOpen")
        writeStringToPref(this@SplashScreenActivity, "0", "isBroadcastSent")

        isMainActivityOpen = readStringFromPref(this, "isGalleryOpen")?.toInt() ?:0
        isBroadcastSent = readStringFromPref(this, "isBroadcastSent")?.toInt() ?:0

        // register broadcast manager
        val localBroadcastManager = LocalBroadcastManager.getInstance(this)
        localBroadcastManager.registerReceiver(receiver, IntentFilter("refresh memes"))

        // check if user has granted permission to access device external storage.
        // if not ask user for access to external storage.
        if (!checkSelfPermission()) {
            requestPermission()

        } else {
            // if permission granted, read images from storage.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ivLoup.animate().setDuration(1000).alpha(1f).withEndAction {

                    //Initiating UI of SplashScreen
                    initUI()

                    //launching coroutines for multi-thread loading.
                    CoroutineScope(Dispatchers.Default).launch {
                        val result = arrayListOf<Deferred<Boolean>>()
                        val cores = Runtime.getRuntime().availableProcessors()
                        //launch coroutines and wait for them to finish
                        for (i in 0..cores) {
                            val mResult: Deferred<Boolean> = async {queryImageStorage(cores+1, i)}
                            result.add(mResult)
                        }

                        if (result.all { it.await() }) {
                            //New meme loaded into memory
                            if(isMainActivityOpen == 1) {
                                isBroadcastSent = readStringFromPref(this@SplashScreenActivity, "isBroadcastSent")?.toInt() ?: 1
                                Log.d(TAG, "saveToMemoryAndOpenGallery: BROADCAST: $isBroadcastSent")
                                if (isBroadcastSent == 1) {
                                    sendBroadcast()
                                    isBroadcastSent = 1
                                    writeStringToPref(
                                        this@SplashScreenActivity,
                                        "1",
                                        "isBroadcastSent"
                                    )
                                }
                            }else{
                                saveToMemoryAndOpenGallery()
                            }
                            finish()
                        }
                    }
                }
            }
        }
    }

    private var receiver: BroadcastReceiver = object : BroadcastReceiver() {
        @RequiresApi(Build.VERSION_CODES.N)
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent != null) {
                Log.d(TAG, "onReceive: Broadcast received, intent != null")
                val str = intent.getStringExtra("EdRfTg123")

                if(str.equals("SAVE!")){
                    saveToMemoryAndOpenGallery()
                }
            }
            Log.d(TAG, "onReceive: Broadcast received, intent == null")
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


                                //initiate UI
                                initUI()

                                //launching coroutines for multi-thread loading.
                                CoroutineScope(Dispatchers.Default).launch {
                                    val result = arrayListOf<Deferred<Boolean>>()
                                    val cores = Runtime.getRuntime().availableProcessors()

                                    //launch coroutines and wait for them to finish
                                    for (i in 0..cores) {
                                        val mResult: Deferred<Boolean> = async {queryImageStorage(cores+1, i)}
                                        result.add(mResult)
                                    }

                                    if (result.all { it.await() }) {
                                        saveToMemoryAndOpenGallery()
                                        finish()
                                    }
                                }

                            }
                        }
                    } else {
                        Toast.makeText(this, "Permission Denied! Cannot load images.", Toast.LENGTH_SHORT).show()
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
        val list = readListOfImagesFromPref(this@SplashScreenActivity, "images $coroutineNumber").toList()
        Log.d(TAG, "queryImageStorage: ROZMIAR ${list.size}")
        val listOfId = arrayListOf<Long>()

        val newList = readListOfImagesFromPref(this@SplashScreenActivity, "images $coroutineNumber")

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
                val heightColumn = it.getColumnIndexOrThrow(MediaStore.Images.Media.HEIGHT)
                val widthColumn = it.getColumnIndexOrThrow(MediaStore.Images.Media.WIDTH)

                while (it.moveToNext()) {

                    val id = it.getLong(idColumn)
                    val name = it.getString(nameColumn)
                    val size = it.getString(sizeColumn)
                    val date = it.getString(dateColumn)
                    val height = it.getInt(heightColumn)
                    val width = it.getInt(widthColumn)
                    val contentUri = ContentUris.withAppendedId(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        id
                    ).toString()

                    listOfId.add(id)
                    Log.d(TAG, "queryImageStorage: SIZE OF ID LIST ${listOfId.size}")
                    //each coroutine takes care of different image, based on image id modulo coroutine amount.
                    if (id.toInt() % coroutineAmount == coroutineNumber) {
                        Log.d(
                            TAG,
                            "queryImageStorage: Hello xD $coroutineNumber, image: ${it.getLong(idColumn)}"
                        )
                        Log.d(
                            TAG,
                            "queryImageStorage: Hello, this is thread ${Thread.currentThread().name}"
                        )
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
                                val recognizer =
                                    TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
                                val result = recognizer.process(inputImage)

                                result.addOnSuccessListener { visionText ->
                                    // Task completed successfully
                                    val image = Image(
                                        id,
                                        name,
                                        size,
                                        date,
                                        contentUri,
                                        visionText.text.uppercase()
                                    )

                                    newList.add(0, image)
                                    Log.d(TAG, "queryImageStorage: SUCCESS ${visionText.text}")
                                    Log.d(TAG, "queryImageStorage: NEW LIST $newList")

                                    //deleteListFromSharedPref(this@SplashScreenActivity, "images $coroutineNumber")
                                    writeListOfImagesToPref(
                                        this@SplashScreenActivity,
                                        newList,
                                        "images $coroutineNumber"
                                    )

                                    writeListOfIDToPref(this@SplashScreenActivity, listOfId)

                                    imageNumber += 1
                                }

                                result.addOnFailureListener { e ->
                                    // Task failed with an exception
                                    Log.d(TAG, "queryImageStorage: FAILURE $e")
                                }

                                //Suspending coroutine prevents memory issues associated with queueing too much images to recognizer.process (it's an async call)
                                suspendCoroutine <Text> { continuation ->
                                    result.addOnCompleteListener { task ->
                                        if (task.isSuccessful) {
                                            continuation.resume(task.result)
                                        } else {
                                            continuation.resumeWithException(task.exception!!)
                                        }
                                    }
                                }
                            }

                            //New meme loaded into memory
                            if(isMainActivityOpen == 1) {
                                isBroadcastSent = readStringFromPref(this, "isBroadcastSent")?.toInt() ?: 0
                                Log.d(TAG, "saveToMemoryAndOpenGallery: BROADCAST: $isBroadcastSent")
                                if (isBroadcastSent == 0) {
                                    sendBroadcast()
                                    isBroadcastSent = 1
                                    writeStringToPref(this@SplashScreenActivity, "1", "isBroadcastSent")
                                }
                            }

                        } else {
                            imageNumber += 1
                            Log.d(TAG, "queryImageStorage: JUZ TAKI JEST")
                        }
                    }


                    //Updating textview with percentage
                    if (imagesAmount != null) {
                        percentageloaded = (imageNumber * 100 / imagesAmount)
                        //Log.d(TAG, "queryImageStorage: PROCENTY $imageNumber $imagesAmount")
                    }

                    // Update textview on main thread
                    if (isUIInitiated == 1){
                        updateUIOnMainThread(percentageloaded, imagesAmount)
                    }
                }

            }

        }

        //Functions below delete those memes from gallery, which were already removed from memory

        /*val keysOfB = listOfId.map { it } // or listOfB.map { it.key }.also { keysOfB ->
        newList.removeAll {
            it.id !in keysOfB
        }*/

        removeDeletedMemesFromMemory(this@SplashScreenActivity, newList, listOfId, coroutineNumber)

        return true
    }


    @RequiresApi(Build.VERSION_CODES.N)
    private fun initUI() {
        val progressBar: ProgressBar = findViewById(R.id.progressBar)
        loadingTextView = findViewById(R.id.loadingPercentage)
        galleryButton = findViewById(R.id.gallerybtn)
        progressBar.visibility = View.VISIBLE
        loadingTextView.visibility = View.VISIBLE
        galleryButton.visibility = View.VISIBLE
        galleryButton.setOnClickListener { onClick(galleryButton) }
        isUIInitiated = 1
        Log.d(TAG, "initUI: UI Initiated!")
    }

    @SuppressLint("SetTextI18n")
    private suspend fun updateUIOnMainThread(percentageloaded: Int, imagesAmount: Int?) {
        withContext(Main) {
            loadingTextView.text =
                "Loading images: $percentageloaded % \n($imageNumber / $imagesAmount)"
        }
    }

    //broadcast about new meme loaded
    private fun sendBroadcast() {
        isBroadcastSent = 1
        writeStringToPref(this@SplashScreenActivity, "1", "isBroadcastSent")
        val intent = Intent("new meme")
        intent.putExtra("EdRfTg1234", "Show Snackbar!")
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
        Log.d(TAG, "sendBroadcast: SENT!!!")
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onClick(p0: View?) {
        if (p0 == galleryButton){
            saveToMemoryAndOpenGallery()
            writeStringToPref(this@SplashScreenActivity, "1", "isGalleryOpen")
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun saveToMemoryAndOpenGallery(){
        val cores = Runtime.getRuntime().availableProcessors()
        val bigList = ArrayList<Image>()
        for (i in 1..cores+1) {
            val mList = readListOfImagesFromPref(this@SplashScreenActivity, "images ${i - 1}")
            bigList.addAll(mList)
        }

        //deleting images without any text on it - these are not memes for sure
        removeImagesWithoutText(bigList)
        /*val notMemes = Predicate { image: Image -> image.text==""}
        remove(bigList,notMemes)*/

        bigList.sortByDescending { Image -> Image.date }
        deleteListFromSharedPref(this, R.string.preference_file_key.toString())

        //checking if list is empty
        val list = readListOfImagesFromPref(this@SplashScreenActivity, R.string.preference_file_key.toString()).toList()
        Log.d(TAG, "saveToMemoryAndOpenGallery: ROZMIAR ${list.size} ")


        writeListOfImagesToPref(
            this@SplashScreenActivity,
            bigList,
            R.string.preference_file_key.toString()
        )

        //checking if list is written successfully
        val list2 = readListOfImagesFromPref(this@SplashScreenActivity, R.string.preference_file_key.toString()).toList()
        Log.d(TAG, "saveToMemoryAndOpenGallery: ROZMIAR 2 ${list2.size} ")

        isMainActivityOpen =
            readStringFromPref(this, "isGalleryOpen")?.toInt() ?:0
        if(isMainActivityOpen == 0){
            val intent = Intent(applicationContext, MainActivity::class.java)
            startActivity(intent)
            isMainActivityOpen = 1
            writeStringToPref(this@SplashScreenActivity, "1", "isGalleryOpen")
        }
    }
}