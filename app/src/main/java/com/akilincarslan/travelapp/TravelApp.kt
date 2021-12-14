package com.akilincarslan.travelapp

import android.app.Application
import timber.log.Timber

class TravelApp :Application() {
    override fun onCreate() {
        super.onCreate()
        Timber.plant(Timber.DebugTree())
    }
}