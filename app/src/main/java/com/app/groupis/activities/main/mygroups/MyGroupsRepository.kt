package com.app.groupis.activities.main.mygroups

import com.app.groupis.activities.main.lobby.Callback
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class MyGroupsRepository {

    private val groupsRef = FirebaseDatabase.getInstance().reference.child("Groups")


    fun getLastMessage(groupTitle: String, callback: Callback) {
        groupsRef.child(groupTitle).child("messages")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        callback.onSuccess(snapshot.children.last())
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    callback.onFailure(error.message)
                }
            })
    }

    fun getAllGroups(callback: Callback) {
        groupsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                callback.onSuccess(snapshot)
            }

            override fun onCancelled(error: DatabaseError) {
                callback.onFailure(error.message)
            }
        })
    }

    fun getGroup(groupTitle: String, callback: Callback) {
        groupsRef.child(groupTitle).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                callback.onSuccess(snapshot)
            }

            override fun onCancelled(error: DatabaseError) {
                callback.onFailure(error.message)
            }
        })
    }
}