package com.app.groupis.activities.chat

import android.content.Context
import android.os.Build
import android.util.Log
import android.widget.EditText
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.recyclerview.widget.RecyclerView
import com.app.groupis.activities.main.TOPIC
import com.app.groupis.models.Chat
import com.app.groupis.models.Group
import com.app.groupis.models.User
import com.app.groupis.notifications.NotificationData
import com.app.groupis.notifications.PushNotificationMessage
import com.app.groupis.notifications.RetrofitInstance
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.iid.FirebaseInstanceId
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
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


    private var groupsRef = FirebaseDatabase.getInstance().reference.child("Groups")
    private var chatRepository: ChatRepository = ChatRepository()

    @RequiresApi(Build.VERSION_CODES.P)
    fun sendMessage(
        group: Group,
        groupTitle: String,
        messageText: EditText,
        user: User
    ) {
        val text = messageText.text.toString()
        chatRepository.saveMessage(messageText.text.toString(), user, group.getTitle())
        messageText.text.clear()
        saveLastMsgTime(group.getTitle())
        updateGroupTotalMessages(group.getTitle())
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
    }

    private fun updateGroupTotalMessages(groupTitle: String) {
        chatRepository.updateTotalMessages(groupTitle)
    }

    private fun saveLastMsgTime(groupTitle: String) {
        chatRepository.saveLastMsgTime(groupTitle)
    }

    fun setMessage(message: String) {
        this.message.value = message
    }

    fun setWhoIsWriting(username: String, groupTitle: String) {
        chatRepository.saveWhoIsWriting(username, groupTitle)
    }

    fun writingEnded(groupTitle: String) {
        chatRepository.saveNoOneIsWriting(groupTitle)
    }

    private fun sendNotification(notificationMessage: PushNotificationMessage) {
        chatRepository.sendNotification(notificationMessage)
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
                chatList.add(message!!)
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
                chatRepository.saveMessagesSeen(groupTitle, snapshot)
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
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {}
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
        messagesDbRef.value = groupsRef.child(groupTitle).child("messages")
    }

    fun setMessagesSeenRef(groupTitle: String) {
        messagesSeenDbRef.value = groupsRef.child(groupTitle).child("messages")
    }

    fun getMessagesListeners(): ChildEventListener {
        return messagesListener.value!!
    }

    fun getMessagesSeenListeners(): ValueEventListener {
        return messagesSeenListener.value!!
    }
}