package com.example.memefinder

import android.content.Context
import android.content.SharedPreferences
import com.example.memefinder.adapter.Image
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

fun writeListOfImagesToPref(context: Context, list: ArrayList<Image>, key: String){
    val sharedPref = context.getSharedPreferences(
        key, Context.MODE_PRIVATE)
    val listInString = Gson().toJson(list)
    sharedPref.edit().putString(key, listInString).apply()
}

fun writeListOfIDToPref(context: Context, list: ArrayList<Long>, key: String = "IDList"){
    val sharedPref = context.getSharedPreferences(
        key, Context.MODE_PRIVATE)
    val listInString = Gson().toJson(list)
    sharedPref.edit().putString(key, listInString).apply()
}

fun writeStringToPref(context: Context, string: String, key: String){
    val sharedPref = context.getSharedPreferences(
        key, Context.MODE_PRIVATE)
    //val listInString = Gson().toJson(string)
    sharedPref.edit().putString(key, string).apply()
}

fun readListOfImagesFromPref(context: Context, key: String): ArrayList<Image>{
    val sharedPref = context.getSharedPreferences(
        key, Context.MODE_PRIVATE)
    val jsonString = sharedPref.getString(key, "")
    val type = object: TypeToken<ArrayList<Image>>() {}.type

    var listFromString = Gson().fromJson<ArrayList<Image>>(jsonString, type)
    if (listFromString == null){
        listFromString = arrayListOf()
    }
    return listFromString
}

fun readListofIDFromPref(context: Context, key: String = "IDList"): ArrayList<Long>{
    val sharedPref = context.getSharedPreferences(
        key, Context.MODE_PRIVATE)
    val jsonString = sharedPref.getString(key, "")
    val type = object: TypeToken<ArrayList<Long>>() {}.type

    var listFromString = Gson().fromJson<ArrayList<Long>>(jsonString, type)
    if (listFromString == null){
        listFromString = arrayListOf()
    }
    return listFromString
}

fun readStringFromPref(context: Context, key: String): String? {
    val sharedPref = context.getSharedPreferences(
        key, Context.MODE_PRIVATE
    )

    return sharedPref.getString(key, "")
}

fun registerSharedPref(context: Context, listener: SharedPreferences.OnSharedPreferenceChangeListener){
    val pref = context.getSharedPreferences(R.string.preference_file_key.toString(), Context.MODE_PRIVATE)
    pref.registerOnSharedPreferenceChangeListener(listener)
}

fun unregisterSharedPref(context: Context, listener: SharedPreferences.OnSharedPreferenceChangeListener){
    val pref = context.getSharedPreferences(R.string.preference_file_key.toString(), Context.MODE_PRIVATE)
    pref.unregisterOnSharedPreferenceChangeListener(listener)
}

fun deleteListFromSharedPref(context: Context, key: String){
    val sharedPref = context.getSharedPreferences(
        key, Context.MODE_PRIVATE)
    sharedPref.edit().clear().apply()
}