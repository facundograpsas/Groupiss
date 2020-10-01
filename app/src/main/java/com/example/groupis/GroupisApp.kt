package com.example.groupis

import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ProcessLifecycleOwner
import com.example.groupis.Checking.isRunning
import com.example.groupis.activities.main.adapters.MyGroupAdapter
import com.example.groupis.models.Group
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


class GroupisApp : android.app.Application(), LifecycleObserver{

    val TAG = "GROUPAPP"

    override fun onCreate() {
        super.onCreate()

        FirebaseDatabase.getInstance().setPersistenceEnabled(true)
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)



        val messagesRef = FirebaseDatabase.getInstance().reference.child("Groups")
//        messagesRef.keepSynced(true)


        val myGroups = arrayListOf<Group>()
//
//        FirebaseDatabase.getInstance().reference.child("Groups").addListenerForSingleValueEvent(object :
//            ValueEventListener {
//            override fun onDataChange(snapshot: DataSnapshot) {
//                myGroups.clear()
//                for(p0 in snapshot.children) {
//                    val userList = p0.child("users")
//                    for(user in userList.children){
//                        if(user.key== FirebaseAuth.getInstance().currentUser!!.uid){
//                            val group = p0.getValue(Group::class.java)
//                            group!!.setUserSize(userList.childrenCount.toInt())
//                            Log.e(TAG, group.getTitle())
//                            myGroups.add(group)

//                        }
//                    }
////                    myGroups.sortByDescending { it.getLastMsgTime() }
//                }
////                if(viewModel.getUser().value!=null && first) {
////                    groupAdapter = MyGroupAdapter(mContext, myGroups, viewModel.getUser().value!!, groupViewModel)
////                    recyclerView.adapter = groupAdapter
////                    first = false
////                }
////                else if(!first){ groupAdapter.notifyDataSetChanged() }
//            }
//            override fun onCancelled(error: DatabaseError) {
//                error.code
//            }
//        })

    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onAppForegrounded() {

        isRunning = true
        Log.e(TAG, "************* foregrounded")
        Log.e(TAG, "************* ${"isInForeground()"}")
        // App in foreground
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onAppBackgrounded() {
        //App in background

        isRunning = false
        Log.e(TAG, "************* backgrounded")
        Log.e(TAG, "************* asdasdasddsasad")
    }

}

object Checking{
    var isRunning = false
}
