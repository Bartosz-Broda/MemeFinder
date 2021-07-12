package com.example.memefinder

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

fun writeListToPref(context: Context, list: ArrayList<Image>){
    val sharedPref = context.getSharedPreferences(
        R.string.preference_file_key.toString(), Context.MODE_PRIVATE)
    var listInString = Gson().toJson(list)
    sharedPref.edit().putString(R.string.preference_file_key.toString(), listInString).apply()
}

fun readListFromPref(context: Context): ArrayList<Image>{
    val sharedPref = context.getSharedPreferences(
        R.string.preference_file_key.toString(), Context.MODE_PRIVATE)
    var jsonString = sharedPref.getString(R.string.preference_file_key.toString(), "")
    var type = object: TypeToken<ArrayList<Image>>() {}.type

    var listFromString = Gson().fromJson<ArrayList<Image>>(jsonString, type)
    return listFromString
}