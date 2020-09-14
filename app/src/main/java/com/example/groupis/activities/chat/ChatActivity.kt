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

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }

}