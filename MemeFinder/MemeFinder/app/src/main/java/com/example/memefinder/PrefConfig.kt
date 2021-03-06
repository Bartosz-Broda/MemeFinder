package com.example.memefinder

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

fun writeListToPref(context: Context, list: ArrayList<Image>){
    val sharedPref = context.getSharedPreferences(
        R.string.preference_file_key.toString(), Context.MODE_PRIVATE)
    val listInString = Gson().toJson(list)
    sharedPref.edit().putString(R.string.preference_file_key.toString(), listInString).apply()
}

fun readListFromPref(context: Context): ArrayList<Image>{
    val sharedPref = context.getSharedPreferences(
        R.string.preference_file_key.toString(), Context.MODE_PRIVATE)
    val jsonString = sharedPref.getString(R.string.preference_file_key.toString(), "")
    val type = object: TypeToken<ArrayList<Image>>() {}.type

    var listFromString = Gson().fromJson<ArrayList<Image>>(jsonString, type)
    if (listFromString == null){
        listFromString = arrayListOf()
    }
    return listFromString
}

fun registerSharedPref(context: Context, listener: SharedPreferences.OnSharedPreferenceChangeListener){
    val pref = context.getSharedPreferences(R.string.preference_file_key.toString(), Context.MODE_PRIVATE)
    pref.registerOnSharedPreferenceChangeListener(listener)
}

fun unregisterSharedPref(context: Context, listener: SharedPreferences.OnSharedPreferenceChangeListener){
    val pref = context.getSharedPreferences(R.string.preference_file_key.toString(), Context.MODE_PRIVATE)
    pref.unregisterOnSharedPreferenceChangeListener(listener)
}