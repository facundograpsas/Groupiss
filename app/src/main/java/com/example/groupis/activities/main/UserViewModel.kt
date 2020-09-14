package com.example.groupis.activities.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import com.example.groupis.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class UserViewModel : ViewModel() {

    var _username = MutableLiveData<String>()
    val user = MutableLiveData<User>()
    val userLoaded = MutableLiveData<Boolean>()


    fun setUsername(username: String) {
        _username.value = username
    }

    fun getUserName(): String {
        return _username.value!!
    }


    fun getUser(): LiveData<User> {
        return user
    }

    fun setUser(user: User) {
        this.user.value = user
    }

    fun setUserLoaded(isLoad : Boolean){
        userLoaded.value = isLoad
    }


    fun retrieveUser(){
        lateinit var user : User
            val ref = FirebaseDatabase.getInstance().reference.child("Users")
                .child(FirebaseAuth.getInstance().currentUser!!.uid)
            ref.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        user = snapshot.getValue(User::class.java)!!
                        setUser(user)
                        setUserLoaded(true)
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                }
            })

        }
}