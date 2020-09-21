package com.example.groupis.activities.chat

import android.content.Context
import android.media.VolumeAutomation
import android.text.format.DateUtils
import android.util.Log
import android.widget.EditText
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.recyclerview.widget.RecyclerView
import com.example.groupis.models.Chat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class ChatViewModel : ViewModel() {

    var message = MutableLiveData<String?>()
    private val dbReference = MutableLiveData<DatabaseReference>()
    private val listener = MutableLiveData<ValueEventListener>()


    fun setListener(groupTitle: String){
        listener.value = object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                val seenHashMap = hashMapOf<String,Any>()
                seenHashMap["messagesSeen"] = snapshot.childrenCount
                FirebaseDatabase.getInstance().reference.child("Groups")
                    .child(groupTitle)
                    .child("users")
                    .child(FirebaseAuth.getInstance().currentUser!!.uid).updateChildren(seenHashMap)
                println(FirebaseAuth.getInstance().currentUser!!.uid)
            }
            override fun onCancelled(error: DatabaseError) {}
        }
    }

    fun getListener() : ValueEventListener{
        return listener.value!!
    }

    fun setMessage(message: String){
        this.message.value = message
    }
    fun getMessage(): String?{
        return message.value
    }


    fun setDbRef(groupTitle: String){
        dbReference.value = FirebaseDatabase.getInstance().reference.child("Groups").child(groupTitle).child("messages")
    }

    fun getDbRef() : DatabaseReference {
        return dbReference.value!!
    }


    fun sendMessage(groupTitle : String, messageText : EditText, username : String, context : Context){
        val dbRef = FirebaseDatabase.getInstance().reference.child("Groups").child(groupTitle)
            .child("messages")
        val hashMap = hashMapOf<String, Any>()
        hashMap["text"] = messageText.text.toString()
        hashMap["username"] = username
        hashMap["uid"] = FirebaseAuth.getInstance().currentUser!!.uid
        hashMap["hour"] = DateUtils.formatDateTime(context, System.currentTimeMillis(), DateUtils.FORMAT_SHOW_TIME)
        hashMap["day"] = DateUtils.formatDateTime(context, System.currentTimeMillis(), DateUtils.FORMAT_ABBREV_TIME)
        hashMap["timeInMillis"] = System.currentTimeMillis()
        dbRef.child(dbRef.push().key!!).updateChildren(hashMap)
        messageText.text.clear()
        saveLastMessage(groupTitle)

        dbRef.addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                val tmHashMap = hashMapOf<String, Any>()
                tmHashMap["totalMessages"] = snapshot.childrenCount
                FirebaseDatabase.getInstance().reference.child("Groups").child(groupTitle)
                    .updateChildren(tmHashMap)
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun saveLastMessage(groupTitle: String) {
        FirebaseDatabase.getInstance().reference.child("Groups").child(groupTitle)
            .addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    val hashMap = hashMapOf<String, Any>()
                    hashMap["lastMsgTime"] = System.currentTimeMillis()
                    FirebaseDatabase.getInstance().reference.child("Groups").child(groupTitle).updateChildren(hashMap)
                }
                override fun onCancelled(error: DatabaseError) {
                    Log.e("Error", error.message)
                }
            })
    }

    fun retrieveMessages(groupTitle: String, username: String, chatList : ArrayList<Chat>, applicationContext : Context, recyclerView: RecyclerView ){
        var adapter : ChatAdapter? = null
        var dbRef = FirebaseDatabase.getInstance().reference.child("Groups").child(groupTitle)
            .child("messages")
        dbRef.addChildEventListener(object : ChildEventListener{
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val message = snapshot.getValue(Chat::class.java)
                println(message!!.getText())
                chatList.add(message)
                adapter = ChatAdapter(applicationContext, chatList, username,groupTitle)
                recyclerView.adapter = adapter
            }
            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onChildRemoved(snapshot: DataSnapshot) {}
            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    fun dbRefSetListener(valueEventListener: ValueEventListener) {
        dbReference.value!!.addValueEventListener(valueEventListener)
    }

    fun dbRefRemoveListener(removeEventListener: ValueEventListener){
        dbReference.value!!.removeEventListener(removeEventListener)
    }
}