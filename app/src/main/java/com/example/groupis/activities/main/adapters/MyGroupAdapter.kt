package com.example.groupis.activities.main.adapters

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import com.example.groupis.R
import com.example.groupis.activities.chat.ChatActivity
import com.example.groupis.activities.main.GroupViewModel
import com.example.groupis.activities.main.LastMessageCallback
import com.example.groupis.models.Chat
import com.example.groupis.models.Group
import com.example.groupis.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MyGroupAdapter(private val mContext : Context, private val mGroupList : ArrayList<Group>, val user : User, private val groupViewModel: GroupViewModel) : RecyclerView.Adapter<MyGroupAdapter.ViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        return ViewHolder(LayoutInflater.from(mContext).inflate(R.layout.my_group_item_layout, parent, false))

    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val group = mGroupList[position]

        groupViewModel.getLastMessage(object : LastMessageCallback {
            override fun onCallback(value: Chat) {
                val username = value.getUsername()
                holder.groupLastMessage.text = username+":"+value.getText()
                holder.groupLastMessageTime.text = value.getHour()
            } }, group.getTitle())

        holder.groupTitle.text = group.getTitle()

        holder.itemView.setOnClickListener {

            it.isClickable = false
            val userUID = FirebaseAuth.getInstance().currentUser!!.uid
            groupViewModel.onMyGroupClick(mContext, user, userUID, group)
            CoroutineScope(Dispatchers.Main).launch {
                suspend {
//                    println("QUE ONDA VATO")
                    delay(1000)
                    it.isClickable=true
//                    println("asdasd")
                }.invoke()
            }
        }
    }

    override fun getItemCount(): Int {
        return mGroupList.size
    }


    class ViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView){

        val groupTitle = itemView.findViewById<TextView>(R.id.my_group_layout_title)
        val groupPicture = itemView.findViewById<ImageView>(R.id.my_group_layout_profile_picture)
        val groupLastMessage = itemView.findViewById<TextView>(R.id.my_group_layout_last_message)
        val groupLastMessageTime = itemView.findViewById<TextView>(R.id.my_group_layout_last_message_time)


    }

}