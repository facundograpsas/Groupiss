package com.example.groupis.activities.username

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat.startActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import com.example.groupis.activities.main.MainActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class UsernameViewModel : ViewModel() {

    val userName: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }

    val isValid: MutableLiveData<Boolean> by lazy {
        MutableLiveData<Boolean>()
    }

    val isFree: MutableLiveData<Boolean> by lazy {
        MutableLiveData<Boolean>()
    }

    fun checkUsernameAvailable(activity : SetUsernameActivity){
        userName.observe(activity, Observer { username ->
            val refUsername =
                FirebaseDatabase.getInstance().reference.child("Users").orderByChild("username")
                    .equalTo(username)
            refUsername.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        isFree.value = false
                        println("EXISTE")
                    } else {
                        isFree.value = true
                        println("NO EXISTE")
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                }
            })
        })
    }

    fun saveUsernameToDB(){
        val refUser = FirebaseDatabase.getInstance().reference.child("Users")
            .child(FirebaseAuth.getInstance().currentUser!!.uid)
        refUser.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val userHashMap = HashMap<String, Any>()
                    userHashMap["nameId"] = userName.value!!
                    refUser.updateChildren(userHashMap)
                }
            }
            override fun onCancelled(error: DatabaseError) {
            }
        })
    }
}

