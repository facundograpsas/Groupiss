package com.example.groupis.activities.main

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.groupis.models.Chat
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class GroupViewModel : ViewModel() {

    val newGroupState = MutableLiveData<String>()
    private val fill = MutableLiveData<Boolean>()

    val lastMessage = MutableLiveData<String>()

    fun setGroupState(value : String){
        this.newGroupState.value = value
    }

    fun setFill(fill : Boolean){
        this.fill.value = fill
    }

    fun setLastMessage(lastMessage : String){
        this.lastMessage.value = lastMessage
    }


     fun readData(callback: LastMessageCallback, groupName : String){
         val dbRef = FirebaseDatabase.getInstance().reference.child("Groups").child(groupName).child("messages")
         FirebaseDatabase.getInstance().reference.child("Groups").child(groupName).child("messages")
            .addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    if(snapshot.exists()) {
                        val lastMessage = snapshot.children.last().getValue(Chat::class.java)
                        callback.onCallback(lastMessage!!)
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
            })
    }

}

interface LastMessageCallback{
    fun onCallback(value : Chat)
}