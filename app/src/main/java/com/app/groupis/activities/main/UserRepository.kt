package com.app.groupis.activities.main

import com.app.groupis.activities.main.lobby.Callback
import com.app.groupis.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class UserRepository {

    private val currentUserUid = FirebaseAuth.getInstance().currentUser!!.uid

    fun getUsername(callback: Callback) {
        FirebaseDatabase.getInstance().reference.child("Users").child(currentUserUid)
            .child("nameId")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        callback.onSuccess(snapshot)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    callback.onFailure(error.message)
                }

            })
    }
}