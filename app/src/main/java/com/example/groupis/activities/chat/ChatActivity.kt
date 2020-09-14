package com.example.groupis.activities.chat

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.groupis.R
import com.example.groupis.models.Chat


class ChatActivity : AppCompatActivity() {

    private lateinit var sendArrow : ImageView
    private lateinit var messageText : EditText
    private lateinit var groupTitle : String
    private lateinit var username : String
    private val viewModel : ChatViewModel by viewModels()
    private lateinit var chatList : ArrayList<Chat>
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter : ChatAdapter

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        sendArrow = findViewById(R.id.chat_send_arrow)
        messageText = findViewById(R.id.chat_edit_text)
        var groupName = findViewById<TextView>(R.id.chat_toolbar_title)
        groupName.text = intent.getStringExtra("groupName")
        groupTitle = intent.getStringExtra("groupName")!!
        username = intent.getStringExtra("username")!!
        chatList = arrayListOf()

        recyclerView = findViewById(R.id.chat_recycler_view)
        recyclerView.setHasFixedSize(true)
        val linearLayoutManager = LinearLayoutManager(this.applicationContext)
        linearLayoutManager.stackFromEnd = true
        recyclerView.layoutManager = linearLayoutManager

        onBackArrowImagePressed()
        onSendArrowPress()

        viewModel.message.observe(this, Observer {
            viewModel.sendMessage(groupTitle, messageText, username, this@ChatActivity)
        })

        viewModel.retrieveMessages(groupTitle, username, chatList, applicationContext, recyclerView)

    }

    private fun onSendArrowPress() {
        sendArrow.setOnClickListener {
            if (messageText.text.toString() != "") {
                viewModel.setMessage(messageText.text.toString())
            }
        }
    }

    private fun onBackArrowImagePressed() {
        val backArrow = findViewById<ImageView>(R.id.chat_back_arrow)
        backArrow.setOnClickListener {
            super.onBackPressed()
            finish()
        }
    }

//    private fun sendMessage(){
//            val dbRef = FirebaseDatabase.getInstance().reference.child("Groups").child(groupTitle)
//                .child("messages")
//            dbRef.addListenerForSingleValueEvent(object : ValueEventListener {
//                override fun onDataChange(snapshot: DataSnapshot) {
//                    val hashMap = hashMapOf<String, Any>()
//                    hashMap["text"] = messageText.text.toString()
//                    hashMap["username"] = username
//                    hashMap["uid"] = FirebaseAuth.getInstance().currentUser!!.uid
//                    hashMap["hour"] = DateUtils.formatDateTime(this@ChatActivity, System.currentTimeMillis(), DateUtils.FORMAT_SHOW_TIME)
//                    dbRef.child(dbRef.push().key!!).updateChildren(hashMap)
////                    chatList.add(Chat(messageText.text.toString(), username))
//                    messageText.text.clear()
////                    adapter = ChatAdapter(applicationContext, chatList)
////                    recyclerView.adapter = adapter
//                }
//                override fun onCancelled(error: DatabaseError) {
//                    println("UYY QUE PASO XD")
//                }
//            })
//        }

//    private fun retrieveMessages(){
//        val dbRef = FirebaseDatabase.getInstance().reference.child("Groups").child(groupTitle)
//            .child("messages")
//        dbRef.addValueEventListener(object : ValueEventListener{
//            override fun onDataChange(snapshot: DataSnapshot) {
//                chatList.clear()
//                for(p0 in snapshot.children){
//                    val message = p0.getValue(Chat::class.java)
//                    chatList.add(message!!)
//                }
//                adapter = ChatAdapter(applicationContext, chatList, username)
//                recyclerView.adapter = adapter
//            }
//
//            override fun onCancelled(error: DatabaseError) {
//                TODO("Not yet implemented")
//            }
//
//        })
//    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }

}