package com.blessall05.translator

import android.app.Application
import com.blessall05.translator.model.DatabaseHelper

class App : Application() {
    companion object {
        lateinit var app: App
        lateinit var database: DatabaseHelper
    }

    override fun onCreate() {
        super.onCreate()
        app = this
        database = DatabaseHelper(this)
    }
}
