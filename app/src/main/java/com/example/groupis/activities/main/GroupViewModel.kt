package com.example.groupis.activities.main

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
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


     fun getLastMessage(callback: LastMessageCallback, groupName : String){
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
                }
                if(viewModel.getUser().value!=null) {
                    groupAdapter = MyGroupAdapter(mContext, myGroups, viewModel.getUser().value!!, groupViewModel)
                    recyclerView.adapter = groupAdapter
                }
            }
            override fun onCancelled(error: DatabaseError) {
                error.code
            }
        })
    }


    fun addNewGroup(groupName : String, viewModel : UserViewModel, callback: UsernameCallback){
        val dbRef = FirebaseDatabase.getInstance().reference.child("Groups").orderByChild("name").equalTo(groupName)
        dbRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()){
                    callback.onCallback("EXISTE")
                }else{
                    if(groupName.toString().length<4 || groupName.length>16){
                        callback.onCallback("LARGO INVALIDO")
                    }
                    else {
                        val groupRef = FirebaseDatabase.getInstance().reference.child("Groups").child(groupName)
                        val hashMapG = HashMap<String, Any?>()
                        hashMapG["title"] = groupName
                        hashMapG["picture"] = null
                        groupRef.updateChildren(hashMapG)
                        callback.onCallback("NO EXISTE")
                        val hashMapU = HashMap<String, Any>()
                        hashMapU["userdata"] = viewModel.getUser().value!!
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
                    intent.putExtra("username", user.getUsername())
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
                if(snapshot.exists()){
                    var intent = Intent(mContext, ChatActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    intent.putExtra("groupName", group.getTitle())
                    intent.putExtra("username", user.getUsername())
                    mContext.startActivity(intent)
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