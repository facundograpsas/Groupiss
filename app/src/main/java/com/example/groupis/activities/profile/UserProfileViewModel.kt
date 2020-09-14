package com.example.groupis.activities.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.groupis.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class UserProfileViewModel : ViewModel() {

        val username = MutableLiveData<String>()
        val isValid = MutableLiveData<String>()
    val user = MutableLiveData<User>()


        fun getUsername(): LiveData<String> {
            return username
        }

        fun setUsername(username : String){
            this.username.value = username
        }


        fun setValid(valid : String){
            this.isValid.value = valid
        }

        fun getUser(): LiveData<User>{
            return user
        }

        fun setUser(user : User){
            this.user.value = user
        }

        fun retrieveUser(){
            FirebaseDatabase.getInstance().reference.child("Users").child(FirebaseAuth.getInstance().currentUser!!.uid)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val user = snapshot.getValue(User::class.java)!!
                        setUser(user)
                    }
                    override fun onCancelled(error: DatabaseError) {
                    }
                })
        }

         fun changeUsername(username : String, callback: UsernameCallback){
             FirebaseDatabase.getInstance().reference.child("Users").orderByChild("nameId").equalTo(username)
                 .addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    if(snapshot.exists()){
                        callback.onCallback("EXISTE")
                    }else{
                        if(username.length<4 || username.length>16){
                            callback.onCallback("LARGO INVALIDO")
                        }
                        else if(username.contains(" ")){
                            callback.onCallback("ESPACIO INVALIDO")
                        }
                        else {
                            val userRef = FirebaseDatabase.getInstance().reference.child("Users")
                                .child(FirebaseAuth.getInstance().currentUser!!.uid)
                            val hashMap = HashMap<String, Any>()
                            hashMap["nameId"] = username
                            userRef.updateChildren(hashMap)
                            callback.onCallback("NO EXISTE")
                        }
                    }
                }
                override fun onCancelled(error: DatabaseError) {

                }
            })
        }
    }