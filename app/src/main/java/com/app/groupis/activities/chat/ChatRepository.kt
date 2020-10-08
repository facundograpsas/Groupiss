package com.app.groupis.activities.chat

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.app.groupis.models.User
import com.app.groupis.notifications.PushNotificationMessage
import com.app.groupis.notifications.RetrofitInstance
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class ChatRepository {

    private val firebaseAuth = FirebaseAuth.getInstance()
    private val groupsRef = FirebaseDatabase.getInstance().reference.child("Groups")
    private val TAG = "ChatRepository"

    @RequiresApi(Build.VERSION_CODES.O)
    fun saveMessage(messageText: String, user: User, groupTitle: String) {
        val hashMap = hashMapOf<String, Any>()
        val time = LocalTime.now()
        val timeFormatter = DateTimeFormatter.ofPattern("h:mm a")
        val timeFormatted = time.format(timeFormatter)
        val day = LocalDate.now()
        val dayFormatter = DateTimeFormatter.ofPattern("d MMM")
        val dayFormatted = day.format(dayFormatter)

        hashMap["text"] = messageText
        hashMap["username"] = user.getNameId()!!
        hashMap["uid"] = FirebaseAuth.getInstance().currentUser!!.uid
        hashMap["hour"] = timeFormatted
        hashMap["day"] = dayFormatted
        hashMap["timeInMillis"] = System.currentTimeMillis()
        groupsRef.child(groupTitle).child("messages").child(groupsRef.push().key!!)
            .updateChildren(hashMap)
    }

    fun updateTotalMessages(groupTitle: String) {
        groupsRef.child(groupTitle).child("messages")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val tmHashMap = hashMapOf<String, Any>()
                    tmHashMap["totalMessages"] = snapshot.childrenCount
                    groupsRef.child(groupTitle).updateChildren(tmHashMap)
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }

    fun saveLastMsgTime(groupTitle: String) {
        groupsRef.child(groupTitle)
        val hashMap = hashMapOf<String, Any>()
        hashMap["lastMsgTime"] = System.currentTimeMillis()
        groupsRef.child(groupTitle).updateChildren(
            hashMap
        )
    }

    fun saveWhoIsWriting(username: String, groupTitle: String) {
        val hashMap = HashMap<String, Any>()
        hashMap["isWriting"] = username
        groupsRef
            .child(groupTitle)
            .updateChildren(hashMap)
    }

    fun saveNoOneIsWriting(groupTitle: String) {
        val hashMap = HashMap<String, Any>()
        hashMap["isWriting"] = "None"
        groupsRef
            .child(groupTitle)
            .updateChildren(hashMap)
    }

    fun sendNotification(notificationMessage: PushNotificationMessage) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitInstance.api.postNotificationMessage(notificationMessage)
                if (response.isSuccessful) {
                    Log.d(TAG, "Response: ${Gson().toJson(response)}")
                } else {
                    Log.e(TAG, response.errorBody().toString())
                }
            } catch (e: Exception) {
                Log.e(TAG, e.toString())
            }
        }
    }

    fun saveMessagesSeen(groupTitle: String, snapshot: DataSnapshot) {
        val seenHashMap = hashMapOf<String, Any>()
        seenHashMap["messagesSeen"] = snapshot.childrenCount
        FirebaseDatabase.getInstance().reference.child("Groups")
            .child(groupTitle)
            .child("users")
            .child(FirebaseAuth.getInstance().currentUser!!.uid).updateChildren(seenHashMap)
    }
}