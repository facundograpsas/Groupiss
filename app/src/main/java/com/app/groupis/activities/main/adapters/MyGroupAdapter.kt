package com.app.groupis.activities.main.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.app.groupis.GlideApp
import com.app.groupis.R
import com.app.groupis.activities.main.LastMessageCallback
import com.app.groupis.activities.main.MyGroupViewModel
import com.app.groupis.activities.main.UnseenMessagesCallback
import com.app.groupis.models.Chat
import com.app.groupis.models.Group
import com.app.groupis.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MyGroupAdapter(private val mContext : Context, private val mGroupList : ArrayList<Group>, val user : User, private val myGroupViewModel: MyGroupViewModel) : RecyclerView.Adapter<MyGroupAdapter.ViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        return ViewHolder(LayoutInflater.from(mContext).inflate(R.layout.my_group_item_layout, parent, false))

    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val group = mGroupList[position]
        setDefaultLogoColor(group, holder)
        groupImageHolder(group, holder)
        setUnseenMessagesView(group, holder)
        setWhoIsWritingHolder(group, holder)

        holder.groupTitle.text = group.getTitle()
        holder.itemView.setOnClickListener {
            onItemClicked(it, group)
        }
    }

    private fun setWhoIsWritingHolder(
        group: Group,
        holder: ViewHolder
    ) {
        if (group.getIsWriting() == "None") {
            myGroupViewModel.getLastMessage(group.getTitle(), object : LastMessageCallback {
                override fun onCallback(value: Chat) {
                    val username = value.getUsername()
                    holder.groupWhoIsWriting.visibility = View.INVISIBLE
                    holder.groupLastMessage.visibility = View.VISIBLE
                    holder.groupLastMessage.text = username + ":" + value.getText()
                    holder.groupLastMessageTime.text = value.getHour()
                }
            })
        } else {
            holder.groupLastMessage.visibility = View.INVISIBLE
            holder.groupWhoIsWriting.visibility = View.VISIBLE
            holder.groupWhoIsWriting.text = group.getIsWriting() + " esta escribiendo un mensaje..."
        }
    }

    private fun onItemClicked(it: View, group: Group) {
        it.isClickable = false
        val userUID = FirebaseAuth.getInstance().currentUser!!.uid
        myGroupViewModel.onMyGroupClick(mContext, user, userUID, group)
        CoroutineScope(Dispatchers.Main).launch {
            suspend {
                delay(1000)
                it.isClickable = true
            }.invoke()
        }
    }

    private fun groupImageHolder(
        group: Group,
        holder: MyGroupAdapter.ViewHolder
    ) {
        if(group.getPicture()!=null){
            val storageRef = FirebaseStorage.getInstance().reference.child("/images")
            val imageRef = group.getPicture()?.let { storageRef.child(it) }
            GlideApp.with(mContext)
                .load(imageRef)
                .into(holder.groupImage)
        }
    }

    private fun setUnseenMessagesView(
        group: Group,
        holder: ViewHolder
    ) {
        if (mGroupList.size >= 1) {
            myGroupViewModel.unseenMessages(group.getTitle(), object : UnseenMessagesCallback {
                override fun onCallback(unseenMessages: String) {
                    holder.groupUnseenMessages.text = unseenMessages
                    if (holder.groupUnseenMessages.text == "0") {
                        holder.groupUnseenMessagesCircle.visibility = View.GONE
                    } else {
                        holder.groupUnseenMessagesCircle.visibility = View.VISIBLE
                    }
                }
            })
        }
    }

    private fun setDefaultLogoColor(
        group: Group,
        holder: ViewHolder
    ) {
        if(group.getPicture()==null){
            when (group.getColor()) {
                1 -> holder.groupImage.setImageResource(R.drawable.default_group_logo_green)
                2 -> holder.groupImage.setImageResource(R.drawable.default_group_logo)
                3 -> holder.groupImage.setImageResource(R.drawable.default_group_logo_green_yellowish)
                4 -> holder.groupImage.setImageResource(R.drawable.default_group_logo_pink)
                5 -> holder.groupImage.setImageResource(R.drawable.default_group_logo_red)
            }
        }
    }

    override fun getItemCount(): Int {
        return mGroupList.size
    }

    class ViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView){
        val groupTitle: TextView = itemView.findViewById(R.id.my_group_layout_title)
        val groupLastMessage: TextView = itemView.findViewById(R.id.my_group_layout_last_message)
        val groupLastMessageTime: TextView = itemView.findViewById(R.id.my_group_layout_last_message_time)
        val groupUnseenMessages: TextView = itemView.findViewById(R.id.my_group_layout_unseen_messages)
        var groupImage : CircleImageView = itemView.findViewById(R.id.my_group_layout_profile_picture)
        val groupUnseenMessagesCircle: FrameLayout = itemView.findViewById(R.id.asdasd)
        val groupWhoIsWriting : TextView = itemView.findViewById(R.id.my_group_layout_who_is_writing)
    }
}