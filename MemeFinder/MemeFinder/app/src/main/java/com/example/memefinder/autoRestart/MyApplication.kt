package com.example.memefinder.autoRestart

import android.app.Application
import android.content.Context


class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    override fun getApplicationContext(): Context {
        return super.getApplicationContext()
    }

    companion object {
        var instance: MyApplication? = null
    }
}