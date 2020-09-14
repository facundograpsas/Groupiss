package com.example.groupis.activities.chat

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.groupis.R
import com.example.groupis.models.Chat
import com.google.firebase.auth.FirebaseAuth

class ChatAdapter(var context: Context, private var chatList: ArrayList<Chat>, private var username : String) : RecyclerView.Adapter<ChatAdapter.ViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        return if(viewType==0){
            ViewHolder(LayoutInflater.from(context).inflate(R.layout.chat_sender_layout, parent, false))
        } else{
            ViewHolder(LayoutInflater.from(context).inflate(R.layout.chat_receiver_layout,parent, false))
        }

    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val message = chatList[position]
        holder.chatText.text = message.getText()
        holder.chatUsername.text = message.getUsername()
        holder.chatTime.text = message.getHour()
    }

    override fun getItemCount(): Int {
        return chatList.size
    }

    override fun getItemViewType(position: Int): Int {

        return if(chatList[position].getUid()==FirebaseAuth.getInstance().currentUser!!.uid){
            0
        } else{
            1
        }


    }

    class ViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView) {

        val chatText: TextView = itemView.findViewById<TextView>(R.id.chat_sender_text)
        val chatUsername : TextView = itemView.findViewById<TextView>(R.id.chat_receiver_username)
        val chatTime : TextView = itemView.findViewById<TextView>(R.id.chat_hour)

    }
}