package com.example.groupis.activities.chat

import android.content.Context
import android.text.format.DateUtils
import android.widget.EditText
import android.widget.TextView
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.recyclerview.widget.RecyclerView
import com.example.groupis.models.Chat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ChatViewModel : ViewModel() {

    var message = MutableLiveData<String?>()

    fun setMessage(message: String){
        this.message.value = message
    }

    fun getMessage(): String?{
        return message.value
    }


    fun sendMessage(groupTitle : String, messageText : EditText, username : String, context : Context){
        val dbRef = FirebaseDatabase.getInstance().reference.child("Groups").child(groupTitle)
            .child("messages")
        dbRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val hashMap = hashMapOf<String, Any>()
                hashMap["text"] = messageText.text.toString()
                hashMap["username"] = username
                hashMap["uid"] = FirebaseAuth.getInstance().currentUser!!.uid
                hashMap["hour"] = DateUtils.formatDateTime(context, System.currentTimeMillis(), DateUtils.FORMAT_SHOW_TIME)
                dbRef.child(dbRef.push().key!!).updateChildren(hashMap)
                messageText.text.clear()
            }
            override fun onCancelled(error: DatabaseError) {
                println("UYY QUE PASO XD")
            }
        })
    }

    fun retrieveMessages(groupTitle: String, username: String, chatList : ArrayList<Chat>, applicationContext : Context, recyclerView: RecyclerView ){
        var adapter : ChatAdapter? = null
        var dbRef = FirebaseDatabase.getInstance().reference.child("Groups").child(groupTitle)
            .child("messages")
        dbRef.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                chatList.clear()
                for(p0 in snapshot.children){
                    var message = p0.getValue(Chat::class.java)
                    chatList.add(message!!)
                }
                adapter = ChatAdapter(applicationContext, chatList, username)
                recyclerView.adapter = adapter
            }
            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })

    }

}