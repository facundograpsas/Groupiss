package com.example.groupis

import com.google.firebase.database.FirebaseDatabase

class GroupisApp : android.app.Application(){


    override fun onCreate() {
        super.onCreate()

        FirebaseDatabase.getInstance().setPersistenceEnabled(true)

    }

}