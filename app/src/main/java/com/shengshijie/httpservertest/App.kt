package com.shengshijie.httpservertest

import android.app.Application
import androidx.multidex.MultiDex

class App : Application() {

    override fun onCreate() {
        super.onCreate()
        MultiDex.install(this)
    }

}