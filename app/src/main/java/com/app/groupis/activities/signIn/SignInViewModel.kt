package com.app.groupis.activities.signIn

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.app.groupis.activities.main.TOPIC
import com.app.groupis.models.Group
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.messaging.FirebaseMessaging

class SignInViewModel : ViewModel() {

    private val TAG = "SignInViewModel"

    fun subscribeToTopics() {
        FirebaseDatabase.getInstance().reference.child("Groups")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (snap in snapshot.children) {
                        val userRef = snap.child("users").child(FirebaseAuth.getInstance().uid!!)
                        if (userRef.exists()) {
                            Log.e(TAG, snap.value.toString())
                            val group = snap.getValue(Group::class.java)
                            val topic = TOPIC + group!!.getTitle().replace(" ", "f")
                            FirebaseMessaging.getInstance().subscribeToTopic(topic)
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                }
            })
    }

    val newUser: MutableLiveData<Boolean> by lazy {
        MutableLiveData<Boolean>()
    }

}