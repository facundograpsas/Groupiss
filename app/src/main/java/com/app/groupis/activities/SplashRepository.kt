package com.app.groupis.activities

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.app.groupis.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class SplashRepository {

    val TAG = "SplashRepository"

    private val isUserLogged = MutableLiveData<Boolean>()

    fun checkIfUserIsLogged(): MutableLiveData<Boolean> {
        isUserLogged.value = FirebaseAuth.getInstance().currentUser != null
        return isUserLogged
    }

//    fun checkIfUserHasUsername(callback: UserCallback){
//        FirebaseDatabase.getInstance().reference.child("Users")
//            .child(FirebaseAuth.getInstance().currentUser!!.uid)
//            .child("nameId")
//            .addListenerForSingleValueEvent(object : ValueEventListener{
//                override fun onDataChange(snapshot: DataSnapshot) {
//                    if(snapshot.value!=null){
//                        callback.hasUsername(true)
//                    }
//                    else{
//                        callback.hasUsername(false)
//                    }
//                }
//                override fun onCancelled(error: DatabaseError) {
//                    Log.e(TAG, error.message)
//                }
//
//            })
//    }

    fun getUser(userCallback: UserCallback) {
        FirebaseDatabase.getInstance().reference.child("Users")
            .child(FirebaseAuth.getInstance().currentUser!!.uid)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val user = snapshot.getValue(User::class.java)
                    userCallback.onCallback(user!!)
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e(TAG, error.message)
                }
            })
    }


    fun getUser(): MutableLiveData<User> {
        val user = MutableLiveData<User>()
        FirebaseDatabase.getInstance().reference.child("Users")
            .child(FirebaseAuth.getInstance().currentUser!!.uid)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    user.value = snapshot.getValue(User::class.java)!!

                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e(TAG, error.message)
                }
            })
        return user
    }
}


interface UserCallback {
    fun onCallback(user: User)
}