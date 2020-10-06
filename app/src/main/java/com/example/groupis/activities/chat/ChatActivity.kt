package com.example.groupis.activities.chat

import android.annotation.SuppressLint
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import com.example.groupis.GlideApp
import com.example.groupis.R
import com.example.groupis.activities.main.TOPIC
import com.example.groupis.activities.main.UserViewModel
import com.example.groupis.models.Chat
import com.example.groupis.models.Group
import com.example.groupis.models.User
import com.example.groupis.notifications.NotificationClear
import com.example.groupis.notifications.PushClearNotifications
import com.example.groupis.notifications.RetrofitInstance
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.Query
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.storage.FirebaseStorage
import com.google.gson.Gson
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*


class ChatActivity : AppCompatActivity() {

    private lateinit var isWritingRef: Query
    private lateinit var whoIsWritingListener: ChildEventListener
    private lateinit var sendArrow: ImageView
    private lateinit var messageText: EditText
    private lateinit var groupTitle: String
    private lateinit var username: String
    private val chatViewModel: ChatViewModel by viewModels()
    private val userViewModel: UserViewModel by viewModels()
    private lateinit var chatList: ArrayList<Chat>
    private lateinit var recyclerView: RecyclerView
    private lateinit var day: TextView
    private var timer: Timer? = null
    private lateinit var groupPicture: CircleImageView
    private lateinit var myUsername: String
    private var myUser: User? = null
    private lateinit var group: Group
    private lateinit var writingAnimation: LottieAnimationView
    private lateinit var writingText: TextView

    private val TAG = "ChatActivity"


    @RequiresApi(Build.VERSION_CODES.P)
    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        group = intent.getSerializableExtra("group") as Group
        chatList = arrayListOf()

        setContentView(R.layout.activity_chat)
        recyclerView = findViewById(R.id.chat_recycler_view)

        if (FirebaseAuth.getInstance().currentUser != null) {
            userViewModel.retrieveUser()
        }

        writingText = findViewById(R.id.chat_activity_writing_text)

        isWritingRef =
            FirebaseDatabase.getInstance().reference.child("Groups").child(group.getTitle())
                .orderByChild("isWriting")


        userViewModel.user.observe(this, Observer { user ->
            myUser = user
            chatViewModel.setWhoIsWritingListener(writingText, myUser!!) { name, key ->
                if (name != "None" && name != myUser!!.getNameId() && key == "isWriting") {
                    writingText.text = "$name esta escribiendo un mensaje..."
                    writingText.visibility = View.VISIBLE
                    Log.e(TAG, "SNAPSHOT VALUE : $name")
                } else {
                    writingText.visibility = View.INVISIBLE
                }
            }

            isWritingRef.addChildEventListener(chatViewModel.whoIsWritingListener.value!!)

        })

        sendArrow = findViewById(R.id.chat_send_arrow)
        messageText = findViewById(R.id.chat_edit_text)
        var groupName = findViewById<TextView>(R.id.chat_activity_layout_group_name)
        if(intent.getStringExtra("username")!=null){
            username = intent.getStringExtra("username")!!
        }

        groupName.text = group.getTitle()

        Log.e(TAG, "Group info:" +
        group.getTitle()+"\n"+
                group.getLastMsgTime()+"\n"+
                group.getColor()+"\n"+
                group.getGroupId()+"\n"+
                group.getIsWriting()+"\n"+
                group.getLastMsgTime()+"\n"+
                group.getPicture()+"\n"+
                group.getTotalMessages()+"\n"+
                group.getSize())

        writingAnimation = findViewById(R.id.chat_activity_writing_animation)
        groupPicture = findViewById(R.id.chat_activity_layout_group_image)
        day = findViewById<TextView>(R.id.chat_activity_day)

        recyclerView.setHasFixedSize(true)
        val linearLayoutManager = LinearLayoutManager(this.applicationContext)
        linearLayoutManager.stackFromEnd = true
        recyclerView.layoutManager = linearLayoutManager

        setGroupPicture(group)
        setChatDayOnScroll()
        showDayOnTouchListener()
        onBackArrowImagePressed()
        onSendArrowPress()

        chatViewModel.message.observe(this, Observer {
            chatViewModel.sendMessage(
                group,
                group.getTitle(),
                messageText,
                myUser!!,
                this@ChatActivity
            )
        })

        chatViewModel.setMessagesRef(group.getTitle())
        chatViewModel.setMessagesListener(chatList, this@ChatActivity, recyclerView)

        chatViewModel.setMessagesSeenListener(group.getTitle())
        chatViewModel.setMessagesSeenRef(group.getTitle())

        messageText.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if(myUser!=null){
                    if (timer!=null){ timer!!.cancel() }
                    chatViewModel.setWhoIsWriting(group.getTitle(), myUser!!.getNameId()!!)
                }
            }

            override fun afterTextChanged(p0: Editable?) {
                timer = Timer()
                timer!!.schedule(object : TimerTask(){
                    override fun run() {
                        chatViewModel.writingEnded(group.getTitle())
                    } }, 600)
            }
        })
    }

    private fun setGroupPicture(group: Group) {
        if (group.getPicture() != null) {
            val storageRef = FirebaseStorage.getInstance().reference.child("/images")
            val imageRef = group.getPicture()?.let { storageRef.child(it) }
            GlideApp.with(this@ChatActivity)
                .load(imageRef)
                .into(groupPicture)
        } else {
            when (group.getColor()) {
                1 -> groupPicture.setImageResource(R.drawable.default_group_logo_green)
                2 -> groupPicture.setImageResource(R.drawable.default_group_logo)
                3 -> groupPicture.setImageResource(R.drawable.default_group_logo_green_yellowish)
                4 -> groupPicture.setImageResource(R.drawable.default_group_logo_pink)
                5 -> groupPicture.setImageResource(R.drawable.default_group_logo_red)
            }
        }
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
                chatViewModel.setMessage(messageText.text.toString())
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
        chatViewModel.removeMessagesListener(chatViewModel.getMessagesListeners())
        chatViewModel.removeMessagesSeenListener(chatViewModel.getMessagesSeenListeners())
        isWritingRef.removeEventListener(chatViewModel.whoIsWritingListener.value!!)

        finish()
        super.onBackPressed()
    }

    override fun onStop() {
        chatViewModel.removeMessagesListener(chatViewModel.getMessagesListeners())
        chatViewModel.removeMessagesSeenListener(chatViewModel.getMessagesSeenListeners())
        isWritingRef.removeEventListener(chatViewModel.whoIsWritingListener.value!!)
        super.onStop()

    }

    override fun onStart() {
        super.onStart()
        chatViewModel.addMessagesSeenListenerToRef(chatViewModel.getMessagesSeenListeners())
        chatViewModel.addMessagesListenerToRef()

    }

    override fun onResume() {
        super.onResume()
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(group.getGroupId())

        PushClearNotifications(
            NotificationClear("123123", FirebaseInstanceId.getInstance().id, group.getGroupId()), TOPIC+group.getTitle()).also {
            clearNotifications(it)
        }
    }

    private fun clearNotifications(notificationClear: PushClearNotifications) =
        CoroutineScope(Dispatchers.IO).launch {
            try{
                val response = RetrofitInstance.api.postClearNotifications(notificationClear)
                if(response.isSuccessful){
                    Log.d(
                        TAG, "Response: ${
                            Gson().toJson(response)
                        }"
                    )
                }else{
                    Log.e(TAG, response.errorBody().toString())
                }
            }catch (e: Exception){
                Log.e(TAG, e.toString())
            }
        }
}


