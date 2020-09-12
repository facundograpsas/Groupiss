package com.example.groupis.activities.chat

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.SystemClock
import android.text.format.DateUtils
import android.view.KeyEvent
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.viewModels
import androidx.core.view.KeyEventDispatcher
import androidx.emoji.bundled.BundledEmojiCompatConfig
import androidx.emoji.text.EmojiCompat
import androidx.emoji.text.EmojiMetadata
import androidx.emoji.widget.EmojiAppCompatButton
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.example.groupis.R
import com.example.groupis.models.Chat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.DateFormat
import java.text.SimpleDateFormat
import kotlin.time.hours

class ChatActivity : AppCompatActivity() {

    private lateinit var sendArrow : ImageView
    private lateinit var messageText : EditText
    private lateinit var groupTitle : String
    private lateinit var username : String
    private val viewModel : ChatViewModel by viewModels()
    private lateinit var chatList : ArrayList<Chat>
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter : ChatAdapter

    override fun dispatchKeyEvent(event: KeyEvent?): Boolean {
        return super.dispatchKeyEvent(event)
    }

    override fun onCreate(savedInstanceState: Bundle?) {


        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_chat)


        sendArrow = findViewById(R.id.chat_send_arrow)
        messageText = findViewById(R.id.chat_edit_text)
        val groupName = findViewById<TextView>(R.id.chat_toolbar_title)
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
            sendMessage()
        })

        retrieveMessages()

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

    private fun sendMessage(){
            val dbRef = FirebaseDatabase.getInstance().reference.child("Groups").child(groupTitle)
                .child("messages")
            dbRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val hashMap = hashMapOf<String, Any>()
                    hashMap["text"] = messageText.text.toString()
                    hashMap["username"] = username
                    hashMap["uid"] = FirebaseAuth.getInstance().currentUser!!.uid
                    hashMap["hour"] = DateUtils.formatDateTime(this@ChatActivity, System.currentTimeMillis(), DateUtils.FORMAT_SHOW_TIME)
                    dbRef.child(dbRef.push().key!!).updateChildren(hashMap)
//                    chatList.add(Chat(messageText.text.toString(), username))
                    messageText.text.clear()
//                    adapter = ChatAdapter(applicationContext, chatList)
//                    recyclerView.adapter = adapter
                }
                override fun onCancelled(error: DatabaseError) {
                    println("UYY QUE PASO XD")
                }
            })
        }

    private fun retrieveMessages(){
        val dbRef = FirebaseDatabase.getInstance().reference.child("Groups").child(groupTitle)
            .child("messages")
        dbRef.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                chatList.clear()
                for(p0 in snapshot.children){
                    val message = p0.getValue(Chat::class.java)
                    chatList.add(message!!)
                }
                adapter = ChatAdapter(applicationContext, chatList, username)
                recyclerView.adapter = adapter
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }

}