package com.example.groupis.activities.chat

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.renderscript.Sampler
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.groupis.R
import com.example.groupis.models.Chat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


class ChatActivity : AppCompatActivity() {

    private lateinit var sendArrow : ImageView
    private lateinit var messageText : EditText
    private lateinit var groupTitle : String
    private lateinit var username : String
    private val viewModel : ChatViewModel by viewModels()
    private lateinit var chatList : ArrayList<Chat>
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter : ChatAdapter
    private lateinit var day : TextView
//    private lateinit var listener : ValueEventListener

    @SuppressLint("ClickableViewAccessibility")
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
        day = findViewById<TextView>(R.id.chat_activity_day)

        recyclerView = findViewById(R.id.chat_recycler_view)
        recyclerView.setHasFixedSize(true)
        val linearLayoutManager = LinearLayoutManager(this.applicationContext)
        linearLayoutManager.stackFromEnd = true
        recyclerView.layoutManager = linearLayoutManager


        setChatDayOnScroll()
        showDayOnTouchListener()
        onBackArrowImagePressed()
        onSendArrowPress()

        viewModel.message.observe(this, Observer {
            viewModel.sendMessage(groupTitle, messageText, username, this@ChatActivity)
        })
        viewModel.retrieveMessages(groupTitle, username, chatList, applicationContext, recyclerView)
        viewModel.setListener(groupTitle)
        viewModel.setDbRef(groupTitle)
    }

    private fun setChatDayOnScroll() {
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                val view = recyclerView.layoutManager!!.getChildAt(0)
                if(view!=null){
                    day.text = view.findViewById<TextView>(R.id.chat_day).text.toString()
                }
            }
        })
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun showDayOnTouchListener() {
        recyclerView.setOnTouchListener { view, motionEvent ->
            when (motionEvent.action) {
                1 -> {
                    day.animate().duration = 500
                    day.animate().alpha(0F)
                    day.animate().startDelay = 1500
                    day.animate().withEndAction {
                        day.alpha = 0F
                    }
                    day.animate().start()
                }
                0 -> {
                    day.animate().setListener(null)
                    day.alpha = 1F
                    day.animate().withEndAction {
                        day.alpha = 1F
                    }
                    day.animate().start()
                }
            }
            view.performClick()
        }
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
        viewModel.dbRefRemoveListener(viewModel.getListener())
        finish()
        super.onBackPressed()
    }

    override fun onStop() {
        viewModel.dbRefRemoveListener(viewModel.getListener())
        super.onStop()

    }

    override fun onStart() {
        super.onStart()
        viewModel.dbRefSetListener(viewModel.getListener())
    }
}