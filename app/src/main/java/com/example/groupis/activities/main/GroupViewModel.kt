package com.example.groupis.activities.main

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.renderscript.Sampler
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.recyclerview.widget.RecyclerView
import com.example.groupis.activities.chat.ChatActivity
import com.example.groupis.activities.main.adapters.GroupAdapter
import com.example.groupis.activities.main.adapters.MyGroupAdapter
import com.example.groupis.activities.profile.UsernameCallback
import com.example.groupis.models.Chat
import com.example.groupis.models.Group
import com.example.groupis.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlin.random.Random

class GroupViewModel : ViewModel() {

    val newGroupState = MutableLiveData<String>()
    private val fill = MutableLiveData<Boolean>()
    val lastMessage = MutableLiveData<String>()
    val newGroupName = MutableLiveData<String>()
    var colorList = arrayOf(Color.BLUE, Color.RED, Color.GREEN)



    fun setNewGroupName(groupName : String){
        this.newGroupName.value = groupName
    }

    fun getNewGroupName() : String?{
        return newGroupName.value
    }

    fun setGroupState(value : String){
        this.newGroupState.value = value
    }

    fun setFill(fill : Boolean){
        this.fill.value = fill
    }

    fun setLastMessage(lastMessage : String){
        this.lastMessage.value = lastMessage
    }


     fun getLastMessage(groupName : String, callback: LastMessageCallback){
         val dbRef = FirebaseDatabase.getInstance().reference.child("Groups").child(groupName).child("messages")
         FirebaseDatabase.getInstance().reference.child("Groups").child(groupName).child("messages")
            .addListenerForSingleValueEvent(object : ValueEventListener{
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

    fun getPublicGroups(groups : ArrayList<Group>, viewModel : UserViewModel, mContext : Context, recyclerGroupList : RecyclerView, groupViewModel: GroupViewModel){
        lateinit var groupAdapter :GroupAdapter
        FirebaseDatabase.getInstance().reference.child("Groups").addValueEventListener(
            object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    groups.clear()
                    for (p0 in snapshot.children) {
                        val userList = p0.child("users")
                        val group = p0.getValue(Group::class.java)
                        group!!.setUserSize(userList.childrenCount.toInt())
                        groups.add(group!!)
                    }
                    if (viewModel.getUser().value != null) {
                        groupAdapter = GroupAdapter(mContext, groups, viewModel.getUser().value!!, groupViewModel)
                        recyclerGroupList.adapter = groupAdapter
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    println(error.message)
                }
            })
    }

    fun getMyGroups(myGroups : ArrayList<Group>, viewModel : UserViewModel, mContext : Context, recyclerView : RecyclerView, groupViewModel: GroupViewModel){
        var first = true
        lateinit var groupAdapter : MyGroupAdapter
        FirebaseDatabase.getInstance().reference.child("Groups").addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                myGroups.clear()
                for(p0 in snapshot.children) {
                    val userList = p0.child("users")
                    for(user in userList.children){
                        if(user.key==FirebaseAuth.getInstance().currentUser!!.uid){
                            val group = p0.getValue(Group::class.java)
                            group!!.setUserSize(userList.childrenCount.toInt())
                            myGroups.add(group)

                        }
                    }
                    myGroups.sortByDescending {
                        it.getLastMsgTime()
                    }
                }
                if(viewModel.getUser().value!=null && first) {
                    groupAdapter = MyGroupAdapter(mContext, myGroups, viewModel.getUser().value!!, groupViewModel)
                    recyclerView.adapter = groupAdapter
                    first = false
                }
                else if(!first){
                    groupAdapter.notifyDataSetChanged()
                }
            }
            override fun onCancelled(error: DatabaseError) {
                error.code
            }
        })
    }

    fun refreshGroups(viewModel : UserViewModel, mContext : Context, recyclerView : RecyclerView, groupViewModel: GroupViewModel){
        lateinit var groupAdapter : MyGroupAdapter
        var groups = arrayListOf<Group>()
        FirebaseDatabase.getInstance().reference.child("Groups").addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                for(p0 in snapshot.children) {
                    val userList = p0.child("users")
                    for(user in userList.children){
                        if(user.key==FirebaseAuth.getInstance().currentUser!!.uid){
                            val group = p0.getValue(Group::class.java)
                            group!!.setUserSize(userList.childrenCount.toInt())
                            groups.add(group)
                        }
                    }
                    groups.sortByDescending {
                        it.getLastMsgTime()
                    }
                }
                if(viewModel.getUser().value!=null) {
                    groupAdapter = MyGroupAdapter(mContext, groups, viewModel.getUser().value!!, groupViewModel)
                    recyclerView.adapter = groupAdapter
                }
            }
            override fun onCancelled(error: DatabaseError) {
                error.code
            }
        })
    }


    fun addNewGroup(groupName : String, viewModel : UserViewModel, callback: UsernameCallback, user : User){
        val dbRef = FirebaseDatabase.getInstance().reference.child("Groups").orderByChild("title").equalTo(groupName)
        dbRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                Log.d("INFO", groupName)
                if(snapshot.exists()){
                    callback.onCallback("EXISTE")
                }else{
                    if(groupName.length<4 || groupName.length>16){
                        callback.onCallback("LARGO INVALIDO")
                    }
                    else {
                        val groupRef = FirebaseDatabase.getInstance().reference.child("Groups").child(groupName)
                        val hashMapG = HashMap<String, Any?>()
                        hashMapG["title"] = groupName
                        hashMapG["picture"] = null
                        hashMapG["totalMessages"] = 0
                        hashMapG["color"] = Random.nextInt(1,6)
                        groupRef.updateChildren(hashMapG)
                        callback.onCallback("NO EXISTE")
                        val hashMapU = HashMap<String, Any>()
                        hashMapU["userdata"] = user
                        val hashMessagesSeen = HashMap<String, Any>()
                        hashMessagesSeen["messagesSeen"] = 0
                        groupRef.child("users").child(FirebaseAuth.getInstance().currentUser!!.uid).updateChildren(hashMapU)
                        groupRef.child("users").child(FirebaseAuth.getInstance().currentUser!!.uid).updateChildren(hashMapU)
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {

            }
        })
    }


    fun onPublicGroupClick(mContext : Context, user : User, userUID : String, group : Group){
        val ref = FirebaseDatabase.getInstance().reference.child("Groups").child(group.getTitle()).child("users").child(userUID)
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()){
                    var intent = Intent(mContext, ChatActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    intent.putExtra("groupName", group.getTitle())
                    intent.putExtra("username", user.getNameId())
                    mContext.startActivity(intent)
                }
                else{
                    AlertDialog.Builder(mContext).setTitle("Deseas unirte a el grupo \"${group.getTitle()}\"?")
                        .setPositiveButton("Unirse al grupo"){_, _ ->
                            val hashMap = HashMap<String, Any>()
                            hashMap["userdata"] = user!!
                            ref.updateChildren(hashMap)
                            Toast.makeText(mContext, "Te has unido a \"${group.getTitle()}\"", Toast.LENGTH_LONG).show()
                        }.setNegativeButton("Cancelar"){dialog, _ ->
                            dialog.cancel()
                        }.show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }

    fun onMyGroupClick(mContext: Context, user: User, userUID: String, group: Group){
        val ref = FirebaseDatabase.getInstance().reference.child("Groups").child(group.getTitle()).child("users").child(userUID)
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                println("AFUERITA")
                println("asd"+group.getTitle())
                if(snapshot.exists()){
                    println("HASTA AQUI GREAT")
                    var intent = Intent(mContext, ChatActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    intent.putExtra("groupName", group.getTitle())
                    intent.putExtra("username", user.getNameId())
                    mContext.startActivity(intent)
                }
            }
            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }

    fun unseenMessages(groupName: String, unseenMessagesCallback: UnseenMessagesCallback){
        FirebaseDatabase.getInstance().reference.child("Groups").child(groupName)
            .addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    val group = snapshot.getValue(Group::class.java)
                    val totalMessages = group!!.getTotalMessages()
                    val seenMessages = snapshot.child("users").child(FirebaseAuth.getInstance().currentUser!!.uid).child("messagesSeen").value
                    if(seenMessages != null && totalMessages != null) {
                        val unseenMessages = totalMessages!! - seenMessages.toString().toInt()
                        unseenMessagesCallback.onCallback(unseenMessages.toString())
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })
    }


    fun sortMyGroupsByLastMessage(groupName: String, myGroups: ArrayList<Group>, mContext: Context, viewModel: UserViewModel, groupViewModel: GroupViewModel) {
        lateinit var groupAdapter : MyGroupAdapter
        FirebaseDatabase.getInstance().reference.child("Groups").child(groupName).child("lastMsgTime")
            .addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    FirebaseDatabase.getInstance().reference.child("Groups")
                        .addListenerForSingleValueEvent(object: ValueEventListener{
                            override fun onDataChange(snapshot: DataSnapshot) {
                                myGroups.sortByDescending {
                                    it.getLastMsgTime()
                                }

                                groupAdapter = MyGroupAdapter(mContext, myGroups, viewModel.getUser().value!!, groupViewModel)
                                groupAdapter.notifyDataSetChanged()
                            }

                            override fun onCancelled(error: DatabaseError) {
                                "adsd"
                            }

                        })
                }

                override fun onCancelled(error: DatabaseError) {
                    "asd"
                }
            })
    }

}

interface LastMessageCallback{
    fun onCallback(value : Chat)
}

interface UnseenMessagesCallback{
    fun onCallback(unseenMessages : String)
}