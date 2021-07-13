package com.example.memefinder

import android.Manifest
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.nfc.Tag
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity(), SharedPreferences.OnSharedPreferenceChangeListener {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val list = readListFromPref(this)
        Log.d(TAG, "onCreate: JESSS $list")
        Log.d(TAG, "onCreate: JESE ${list.size}" )
    }


    //Doesn't work, don't know why. Perhaps i have to run both activities at once, until all data will be loaded
    //As for now, i don't use listener, i just load everything at the first start and then only new pictures are loaded.
    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        when (key){
            getString(R.string.preference_file_key) -> {
                val list = readListFromPref(this)
                Log.d(TAG, "onCreate: JESTT $list")
            }
        }
    }

    override fun onStart() {
        super.onStart()
        registerSharedPref(this, this)
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterSharedPref(this, this)
    }

}