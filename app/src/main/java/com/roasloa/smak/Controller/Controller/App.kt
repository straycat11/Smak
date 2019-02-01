package com.roasloa.smak.Controller.Controller

import android.app.Application
import com.roasloa.smak.Controller.Utilities.SharedPrefs

class App : Application() {

    companion object {
        lateinit var sharedPrefs: SharedPrefs
    }
    override fun onCreate() {
        sharedPrefs = SharedPrefs(applicationContext )
        super.onCreate()

    }
}