package com.example.groupis.activities.chat

import android.content.Context
import android.os.Build
import android.text.format.DateUtils
import android.util.Log
import android.widget.EditText
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.recyclerview.widget.RecyclerView
import com.example.groupis.activities.main.TOPIC
import com.example.groupis.models.Chat
import com.example.groupis.models.Group
import com.example.groupis.models.User
import com.example.groupis.notifications.NotificationData
import com.example.groupis.notifications.PushNotificationMessage
import com.example.groupis.notifications.RetrofitInstance
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.iid.FirebaseInstanceId
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.collections.HashMap

class ChatViewModel : ViewModel() {

    private val TAG: String? = "ChatViewModel"
    var message = MutableLiveData<String?>()
    private val messagesSeenDbRef = MutableLiveData<DatabaseReference>()
    private val messagesSeenListener = MutableLiveData<ValueEventListener>()
    private val messagesDbRef = MutableLiveData<DatabaseReference>()
    private val messagesListener = MutableLiveData<ChildEventListener>()
    val whoIsWritingListener = MutableLiveData<ChildEventListener>()


    @RequiresApi(Build.VERSION_CODES.P)
    fun sendMessage(
        group: Group,
        groupTitle: String,
        messageText: EditText,
        user: User,
        context: Context
    ) {
        val dbRef = FirebaseDatabase.getInstance().reference.child("Groups").child(groupTitle)
            .child("messages")
        val hashMap = hashMapOf<String, Any>()
        val text = messageText.text.toString()

        val date = LocalTime.now()
        val formatter = DateTimeFormatter.ofPattern("h:mm a")
        val hour = date.format(formatter)

        hashMap["text"] = messageText.text.toString()
        hashMap["username"] = user.getNameId()!!
        hashMap["uid"] = FirebaseAuth.getInstance().currentUser!!.uid
        hashMap["hour"] = hour
        hashMap["day"] = DateUtils.formatDateTime(
            context,
            System.currentTimeMillis(),
            DateUtils.FORMAT_ABBREV_TIME
        )
        hashMap["timeInMillis"] = System.currentTimeMillis()
        dbRef.child(dbRef.push().key!!).updateChildren(hashMap)
        messageText.text.clear()
        saveLastMessage(group.getTitle())
        updateGroupTotalMessages(dbRef, group.getTitle())
        PushNotificationMessage(
            NotificationData(
                group,
                text,
                user.getNameId()!!,
                FirebaseInstanceId.getInstance().id,
                user.getPictureRef()
            ),
            TOPIC + groupTitle.replace(
                " ",
                "f"
            )
        ).also {
            sendNotification(it)
        }
//        Log.e(TAG, timeText)

    }

    private fun updateGroupTotalMessages(dbRef: DatabaseReference, groupTitle: String) {
        dbRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val tmHashMap = hashMapOf<String, Any>()
                tmHashMap["totalMessages"] = snapshot.childrenCount
                FirebaseDatabase.getInstance().reference
                    .child("Groups")
                    .child(groupTitle)
                    .updateChildren(tmHashMap)
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun saveLastMessage(groupTitle: String) {
        FirebaseDatabase.getInstance().reference.child("Groups").child(groupTitle)
        val hashMap = hashMapOf<String, Any>()
        hashMap["lastMsgTime"] = System.currentTimeMillis()
        FirebaseDatabase.getInstance().reference.child("Groups").child(groupTitle).updateChildren(
            hashMap
        )
    }


    fun setMessage(message: String){
        this.message.value = message
    }

    fun setWhoIsWriting(groupTitle: String, username: String){
        val hashMap = HashMap<String, Any>()
        hashMap["isWriting"] = username
        FirebaseDatabase.getInstance().reference.child("Groups")
            .child(groupTitle)
            .updateChildren(hashMap)
    }

    fun writingEnded(groupTitle: String){
        val hashMap = HashMap<String, Any>()
        hashMap["isWriting"] = "None"
        FirebaseDatabase.getInstance().reference.child("Groups")
            .child(groupTitle)
            .updateChildren(hashMap)
    }

    private fun sendNotification(notificationMessage: PushNotificationMessage) =
        CoroutineScope(Dispatchers.IO).launch {
            try{
                val response = RetrofitInstance.api.postNotificationMessage(notificationMessage)
                if (response.isSuccessful) {
                    Log.d(
                        TAG, "Response: ${
                            Gson().toJson(response)
                        }"
                    )
                } else {
                    Log.e(TAG, response.errorBody().toString())
                }
            } catch (e: Exception) {
                Log.e(TAG, e.toString())
            }
        }


    fun setMessagesListener(
        chatList: ArrayList<Chat>,
        applicationContext: Context,
        recyclerView: RecyclerView
    ) {
        messagesListener.value = object : ChildEventListener {
            var adapter: ChatAdapter? = null
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val message = snapshot.getValue(Chat::class.java)
                Log.e(TAG, message!!.getText())
                chatList.add(message)
                adapter = ChatAdapter(applicationContext, chatList)
                recyclerView.adapter = adapter
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onChildRemoved(snapshot: DataSnapshot) {}
            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onCancelled(error: DatabaseError) {}
        }
    }


    fun setMessagesSeenListener(groupTitle: String){
        messagesSeenListener.value = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val seenHashMap = hashMapOf<String, Any>()
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

    fun setWhoIsWritingListener(
        writingText: TextView,
        myUser: User,
        funct: (name: String, key: String) -> Unit
    ) {
        whoIsWritingListener.value = object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                val name = snapshot.value.toString()
                val key = snapshot.key
                funct(name, key!!)
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {}
            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onCancelled(error: DatabaseError) {}
        }
    }


    fun addMessagesListenerToRef() {
        messagesDbRef.value!!.addChildEventListener(messagesListener.value!!)
    }

    fun addMessagesSeenListenerToRef(valueEventListener: ValueEventListener) {
        messagesSeenDbRef.value!!.addValueEventListener(messagesSeenListener.value!!)
    }

    fun removeMessagesListener(listener: ChildEventListener) {
        messagesDbRef.value!!.removeEventListener(listener)
    }

    fun removeMessagesSeenListener(removeEventListener: ValueEventListener) {
        messagesSeenDbRef.value!!.removeEventListener(removeEventListener)
    }

    fun setMessagesRef(groupTitle: String) {
        messagesDbRef.value = FirebaseDatabase.getInstance().reference.child("Groups").child(
            groupTitle
        )
            .child("messages")
    }

    fun setMessagesSeenRef(groupTitle: String) {
        messagesSeenDbRef.value = FirebaseDatabase.getInstance().reference.child("Groups").child(
            groupTitle
        ).child("messages")
    }

    fun getMessagesListeners(): ChildEventListener {
        return messagesListener.value!!
    }

    fun getMessagesSeenListeners(): ValueEventListener {
        return messagesSeenListener.value!!
    }
}