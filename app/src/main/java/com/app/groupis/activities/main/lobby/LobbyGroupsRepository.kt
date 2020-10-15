package com.app.groupis.activities.main.lobby

import android.util.Log
import com.app.groupis.models.Group
import com.app.groupis.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage

class GroupRepository {

    private val groupsRef = FirebaseDatabase.getInstance().reference.child("Groups")
    private val storageRef = FirebaseStorage.getInstance().reference.child("/images")
    private val currentUserUID = FirebaseAuth.getInstance().currentUser!!.uid
    private val TAG = "GroupRepository"


    fun getGroup(groupName: String, callback: Callback) {
        groupsRef.orderByChild("title").equalTo(groupName)
        groupsRef.orderByChild("title").equalTo(groupName)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    callback.onSuccess(snapshot)
                }

                override fun onCancelled(error: DatabaseError) {
                    callback.onFailure(error.message)
                }
            })
    }

    fun saveNewGroup(group: Group, user: User) {
        val groupRef = groupsRef.child(group.getTitle())
        val hashMapG = HashMap<String, Any?>()
        hashMapG["title"] = group.getTitle()
        if (group.getPicture() != null) {
            hashMapG["picture"] = group.getPicture()
        }
        hashMapG["totalMessages"] = group.getTotalMessages()
        hashMapG["color"] = group.getColor()
        hashMapG["groupId"] = group.getGroupId()
        groupRef.updateChildren(hashMapG)
        val hashMapU = HashMap<String, Any>()
        hashMapU["userdata"] = user
        val hashMessagesSeen = HashMap<String, Any>()
        hashMessagesSeen["messagesSeen"] = 0
        groupRef.child("users").child(FirebaseAuth.getInstance().currentUser!!.uid)
            .updateChildren(hashMapU)
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

    fun saveGroupImage(data: ByteArray, imageKey: String) {
        val imageRef = storageRef.child("$imageKey.jpg")
        var uploadTask = imageRef.putBytes(data)
        uploadTask.addOnSuccessListener {
            Log.i("$TAG/saveGroupImage", "Image saved to storage successfully")
        }.addOnFailureListener {
            Log.i("$TAG/saveGroupImage", "Error saving image to storage. ERROR:" + it.message)
        }
    }

    fun getCurrentUserInGroup(groupTitle: String, callback: Callback) {
        val userRef = groupsRef.child(groupTitle).child("users").child(currentUserUID)
        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                callback.onSuccess(snapshot)
            }

            override fun onCancelled(error: DatabaseError) {
                callback.onFailure(error.message)
            }
        })
    }


    fun saveCurrentUserInGroup(user: User, groupTitle: String) {
        val userRef = groupsRef.child(groupTitle).child("users").child(currentUserUID)
        val hashMap = HashMap<String, Any>()
        hashMap["userdata"] = user
        userRef.updateChildren(hashMap)
    }

}

interface Callback {
    fun onSuccess(snapshot: DataSnapshot)
    fun onFailure(message: String)
}