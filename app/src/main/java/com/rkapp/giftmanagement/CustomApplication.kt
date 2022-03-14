package com.rkapp.giftmanagement

import android.app.Application
import android.content.Context

class CustomApplication : Application() {
    companion object {
        lateinit var appContext: Context
    }

    override fun onCreate() {
        super.onCreate()
        appContext = this
    }
}