package com.example.groupis

import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ProcessLifecycleOwner
import com.example.groupis.Checking.isRunning
import com.google.firebase.database.FirebaseDatabase


class GroupisApp : android.app.Application(), LifecycleObserver{

    val TAG = "GROUPAPP"

    override fun onCreate() {
        super.onCreate()

        FirebaseDatabase.getInstance().setPersistenceEnabled(true)
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onAppForegrounded() {

        isRunning = true
        Log.e(TAG, "************* foregrounded")
        Log.e(TAG, "************* ${"isInForeground()"}")
        // App in foreground
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onAppBackgrounded() {
        //App in background

        isRunning = false
        Log.e(TAG, "************* backgrounded")
        Log.e(TAG, "************* asdasdasddsasad")
    }

}

object Checking{
    var isRunning = false
}
