package com.example.memefinder

import android.net.Uri
import android.os.Parcel
import android.os.Parcelable

data class Image(val id: Long, val name: String?, val size: String?, val date: String?, val uri: String?) {

}