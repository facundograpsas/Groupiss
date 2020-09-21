package com.example.groupis.activities.main.adapters

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.groupis.R
import com.example.groupis.activities.main.GroupViewModel
import com.example.groupis.activities.main.LastMessageCallback
import com.example.groupis.activities.main.UnseenMessagesCallback
import com.example.groupis.models.Chat
import com.example.groupis.models.Group
import com.example.groupis.models.User
import com.google.firebase.auth.FirebaseAuth
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.w3c.dom.Text

class MyGroupAdapter(private val mContext : Context, private val mGroupList : ArrayList<Group>, val user : User, private val groupViewModel: GroupViewModel) : RecyclerView.Adapter<MyGroupAdapter.ViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        return ViewHolder(LayoutInflater.from(mContext).inflate(R.layout.my_group_item_layout, parent, false))

    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val group = mGroupList[position]


        println(group.getColor())
        when(group.getColor()){
            1 -> holder.groupProfilePicture.setImageResource(R.drawable.default_group_logo_green)
            2 -> holder.groupProfilePicture.setImageResource(R.drawable.default_group_logo)
            3 -> holder.groupProfilePicture.setImageResource(R.drawable.default_group_logo_green_yellowish)
            4 -> holder.groupProfilePicture.setImageResource(R.drawable.default_group_logo_pink)
            5 -> holder.groupProfilePicture.setImageResource(R.drawable.default_group_logo_red)
        }


        if(mGroupList.size>=1){
            groupViewModel.unseenMessages(group.getTitle(), object : UnseenMessagesCallback {
                override fun onCallback(unseenMessages: String) {
                    holder.groupUnseenMessages.text = unseenMessages
                    if(holder.groupUnseenMessages.text == "0"){
                        holder.groupUnseenMessagesCircle.visibility = View.GONE
                    }
                    else{
                        holder.groupUnseenMessagesCircle.visibility = View.VISIBLE
                    }
                }
            })
        }


        groupViewModel.getLastMessage(group.getTitle(), object : LastMessageCallback {
            override fun onCallback(value: Chat) {
                val username = value.getUsername()
                holder.groupLastMessage.text = username+":"+value.getText()
                holder.groupLastMessageTime.text = value.getHour()
            }})

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
        val groupUnseenMessages = itemView.findViewById<TextView>(R.id.my_group_layout_unseen_messages)
        var groupProfilePicture : CircleImageView = itemView.findViewById(R.id.my_group_layout_profile_picture)
        val groupUnseenMessagesCircle = itemView.findViewById<FrameLayout>(R.id.asdasd)


    }

}