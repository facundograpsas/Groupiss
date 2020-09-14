package com.example.groupis.activities.main

import android.content.Context
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.recyclerview.widget.RecyclerView
import com.example.groupis.activities.main.adapters.GroupAdapter
import com.example.groupis.activities.main.adapters.MyGroupAdapter
import com.example.groupis.activities.profile.UsernameCallback
import com.example.groupis.models.Chat
import com.example.groupis.models.Group
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

    fun getPublicGroups(groups : ArrayList<Group>, viewModel : UserViewModel, mContext : Context, recyclerGroupList : RecyclerView){
        lateinit var groupAdapter :GroupAdapter
        val dbRef = FirebaseDatabase.getInstance().reference.child("Groups").addValueEventListener(
            object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    Log.d("DEBUG", "EJECUTATRES")
                    groups.clear()
                    for (p0 in snapshot.children) {
                        val userList = p0.child("users")
                        val group = p0.getValue(Group::class.java)
                        group!!.setUserSize(userList.childrenCount.toInt())
                        groups.add(group!!)
                        Log.d("DEBUG", "IN FOR")
                    }
                    Log.d("DEBUG", "POST FOR")
                    if (viewModel.getUser().value != null) {
                        println("User: " + viewModel.getUser().value)
//                        println(context)
                        println(mContext)
                        println(groups)
                        groupAdapter = GroupAdapter(mContext, groups, viewModel.getUser().value!!)
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
                    println("User: "+viewModel.getUser().value)
//                    println(context)
                    println(myGroups)
                    groupAdapter = MyGroupAdapter(mContext, myGroups, viewModel.getUser().value!!, groupViewModel)
                    recyclerView.adapter = groupAdapter
                }
            }
            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
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

}

interface LastMessageCallback{
    fun onCallback(value : Chat)
}