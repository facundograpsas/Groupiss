package com.example.groupis.activities.main.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.groupis.R
import com.example.groupis.activities.main.GroupViewModel
import com.example.groupis.activities.main.UnseenMessagesCallback
import com.example.groupis.models.Group
import com.example.groupis.models.User
import com.google.firebase.auth.FirebaseAuth
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class GroupAdapter(mContext : Context, mGroupList : List<Group>, private val user: User, groupViewModel: GroupViewModel) : RecyclerView.Adapter<GroupAdapter.ViewHolder>() {

    private val mContext : Context = mContext
    private val mGroupList : List<Group> = mGroupList
    private val groupViewModel = groupViewModel

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view : View = LayoutInflater.from(mContext).inflate(R.layout.group_item_layout, viewGroup, false)
        return ViewHolder(view)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {


        val group : Group? = mGroupList[position]
        holder.groupTitle.text = group!!.getTitle()
        holder.groupImage = group.getPicture()

        when(group.getColor()){
            1 -> holder.groupBackgroundColor.setImageResource(R.drawable.default_group_logo_green)
            2 -> holder.groupBackgroundColor.setImageResource(R.drawable.default_group_logo)
            3 -> holder.groupBackgroundColor.setImageResource(R.drawable.default_group_logo_green_yellowish)
            4 -> holder.groupBackgroundColor.setImageResource(R.drawable.default_group_logo_pink)
            5 -> holder.groupBackgroundColor.setImageResource(R.drawable.default_group_logo_red)
        }


//        groupViewModel.unseenMessages(group!!.getTitle(), object : UnseenMessagesCallback {
//            override fun onCallback(unseenMessages: String) {
//                println(unseenMessages)
//            }
//        })


        val members = group.getSize().toString()
        if(members=="1"){
            holder.groupMembers.text = "$members Miembro"
        }
        else{
            holder.groupMembers.text = "$members Miembros"
        }
        holder.itemView.setOnClickListener {

            it.isClickable = false
            val userUID = FirebaseAuth.getInstance().currentUser!!.uid
            groupViewModel.onPublicGroupClick(mContext, user, userUID, group)
            CoroutineScope(Dispatchers.Main).launch {
                suspend {
                    println("QUE ONDA VATO")
                    delay(1000)
                    it.isClickable=true
                    println("asdasd")
                }.invoke()
            }

        }

    }

    override fun getItemCount(): Int {
        return mGroupList.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        var groupTitle : TextView = itemView.findViewById(R.id.group_layout_title)
        var groupImage : CircleImageView? = itemView.findViewById(R.id.group_layout_profile_picture)
        var groupMembers : TextView = itemView.findViewById(R.id.group_layout_members)
        var groupInfo : ImageView = itemView.findViewById(R.id.group_layout_info)
        var groupBackgroundColor : CircleImageView = itemView.findViewById(R.id.group_layout_profile_picture)

    }
}