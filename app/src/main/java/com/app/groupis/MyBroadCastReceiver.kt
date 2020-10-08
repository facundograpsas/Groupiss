package com.app.groupis

import android.app.NotificationManager
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.app.groupis.activities.main.TOPIC
import com.app.groupis.notifications.NotificationClear
import com.app.groupis.notifications.PushClearNotifications
import com.app.groupis.notifications.RetrofitInstance
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.iid.FirebaseInstanceId
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MyBroadCastReceiver : BroadcastReceiver() {

    private val TAG = "MyBroadCastReceiver"

    override fun onReceive(p0: Context?, p1: Intent?) {
        Log.i(TAG, "in broadcast receiver")
        val id = p1!!.getIntExtra("id", 0)
        val groupTitle = p1.getStringExtra("groupTitle")
        val notificationManager = p0!!.getSystemService(Service.NOTIFICATION_SERVICE) as NotificationManager
        Log.i(TAG, "$groupTitle")
        notificationManager.cancel(id)
        if(notificationManager.activeNotifications.size==1){
            notificationManager.cancelAll()
        }
        Log.i(TAG, "Active notifications: ${notificationManager.activeNotifications.size}")

        clearNotifications(groupTitle, id)
        updateUserMessagesSeen(groupTitle)
    }

    private fun updateUserMessagesSeen(groupTitle: String?) {
        FirebaseDatabase.getInstance().reference.child("Groups").child(groupTitle!!)
            .child("messages")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val seenHashMap = hashMapOf<String, Any>()
                    seenHashMap["messagesSeen"] = snapshot.childrenCount
                    FirebaseDatabase.getInstance().reference.child("Groups")
                        .child(groupTitle)
                        .child("users")
                        .child(FirebaseAuth.getInstance().currentUser!!.uid)
                        .updateChildren(seenHashMap)
                }

                override fun onCancelled(error: DatabaseError) {
                }
            })
    }

    private fun clearNotifications(groupTitle: String?, id : Int) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                RetrofitInstance.api.postClearNotifications(
                    data = PushClearNotifications(
                        NotificationClear("123123", FirebaseInstanceId.getInstance().id, id),
                        TOPIC + groupTitle!!.replace(" ", "f")
                    )
                )
            } catch (e: Exception) {
                Log.e(TAG, e.message!!)
            }
        }
    }
}