package com.example.groupis.activities.main.adapters

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.groupis.R
import com.example.groupis.activities.chat.ChatActivity
import com.example.groupis.activities.main.MainActivity
import com.example.groupis.activities.main.UserViewModel
import com.example.groupis.models.Group
import com.example.groupis.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.coroutines.Continuation
import kotlin.coroutines.coroutineContext
import kotlin.coroutines.startCoroutine

class GroupAdapter(mContext : Context, mGroupList : List<Group>, private val user: User) : RecyclerView.Adapter<GroupAdapter.ViewHolder>() {

    private val mContext : Context = mContext
    private val mGroupList : List<Group> = mGroupList

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view : View = LayoutInflater.from(mContext).inflate(R.layout.group_item_layout, viewGroup, false)
        return ViewHolder(view)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val group : Group? = mGroupList[position]
//        val membersText = group!!.getUsers().size.toString()+" miembros"
        holder.groupTitle.text = group!!.getTitle()
        holder.groupImage = group.getPicture()
        val members = group.getSize().toString()
        if(members=="1"){
            holder.groupMembers.text = "$members Miembro"
        }
        else{
            holder.groupMembers.text = "$members Miembros"
        }
//        holder.groupMembers.text = membersText
        holder.itemView.setOnClickListener {

            it.isClickable = false

            val userUID = FirebaseAuth.getInstance().currentUser!!.uid

            val ref = FirebaseDatabase.getInstance().reference.child("Groups").child(group.getTitle()).child("users").child(userUID)
            ref.addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    if(snapshot.exists()){
                        println("YENDO AL GROUP ACTIVITY CHAT")
                        var intent = Intent(mContext, ChatActivity::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        intent.putExtra("groupName", group.getTitle())
                        intent.putExtra("username", user.getUsername())
                        mContext.startActivity(intent)


                    }
                    else{
                        println("CTXT:"+mContext)
                        println("CTXT:"+mContext.applicationContext)
                        println("EXCELENTE PAPA")
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
        var  groupInfo : ImageView = itemView.findViewById(R.id.group_layout_info)

    }
}