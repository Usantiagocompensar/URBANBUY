package com.example.urbanbuy
import android.app.Application
import com.google.firebase.FirebaseApp
class UrbanBuyApp: Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
    }
}
