package com.app.groupis.activities.main

import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.recyclerview.widget.RecyclerView
import com.app.groupis.activities.chat.ChatActivity
import com.app.groupis.activities.main.adapters.MyGroupAdapter
import com.app.groupis.models.Chat
import com.app.groupis.models.Group
import com.app.groupis.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class MyGroupViewModel : ViewModel() {


    fun getLastMessage(groupName : String, callback: LastMessageCallback) {
        FirebaseDatabase.getInstance().reference.child("Groups").child(groupName).child("messages")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val lastMessage = snapshot.children.last().getValue(Chat::class.java)
                        callback.onCallback(lastMessage!!)
                    }
                }
                override fun onCancelled(error: DatabaseError) {}
            })
        }

    fun getMyGroups(myGroups : ArrayList<Group>, viewModel : UserViewModel, mContext : Context, recyclerView : RecyclerView, groupViewModel: MyGroupViewModel){
        var first = true
        lateinit var groupAdapter : MyGroupAdapter
        FirebaseDatabase.getInstance().reference.child("Groups").addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                myGroups.clear()
                for(p0 in snapshot.children) {
                    val userList = p0.child("users")
                    for(user in userList.children){
                        if(user.key== FirebaseAuth.getInstance().currentUser!!.uid){
                            val group = p0.getValue(Group::class.java)
                            group!!.setUserSize(userList.childrenCount.toInt())
                            myGroups.add(group)
                        }
                    }
                    myGroups.sortByDescending { it.getLastMsgTime() }
                }
                if(viewModel.getUser().value!=null && first) {
                    groupAdapter = MyGroupAdapter(mContext, myGroups, viewModel.getUser().value!!, groupViewModel)
                    recyclerView.adapter = groupAdapter
                    first = false
                }
                else if(!first){ groupAdapter.notifyDataSetChanged() }
            }
            override fun onCancelled(error: DatabaseError) {
                error.code
            }
        })
    }

    fun onMyGroupClick(mContext: Context, user: User, userUID: String, group: Group){
        var intent = Intent(mContext, ChatActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.putExtra("groupName", group.getTitle())
        intent.putExtra("myUsername", user.getNameId())
        intent.putExtra("group", group)
        mContext.startActivity(intent)
    }

    fun unseenMessages(groupName: String, unseenMessagesCallback: UnseenMessagesCallback){
        FirebaseDatabase.getInstance().reference.child("Groups").child(groupName)
            .addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    val group = snapshot.getValue(Group::class.java)
                    val totalMessages = group!!.getTotalMessages()
                    val seenMessages = snapshot.child("users").child(FirebaseAuth.getInstance().currentUser!!.uid).child("messagesSeen").value
                    if(seenMessages != null && totalMessages != null) {
                        val unseenMessages = totalMessages!! - seenMessages.toString().toInt()
                        unseenMessagesCallback.onCallback(unseenMessages.toString())
                    }
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }
}

interface LastMessageCallback{
    fun onCallback(value : Chat)
}

interface UnseenMessagesCallback{
    fun onCallback(unseenMessages : String)
}
