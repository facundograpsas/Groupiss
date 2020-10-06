package com.example.groupis.notifications

import android.annotation.SuppressLint
import android.app.*
import android.app.NotificationManager.IMPORTANCE_HIGH
import android.app.PendingIntent.*
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.graphics.drawable.Icon
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.example.groupis.Checking
import com.example.groupis.GlideApp
import com.example.groupis.MyBroadCastReceiver
import com.example.groupis.R
import com.example.groupis.activities.chat.ChatActivity
import com.example.groupis.models.Group
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.firebase.storage.FirebaseStorage
import com.google.gson.Gson
import kotlin.random.Random


private const val CHANNEL_ID = "my_channel"

@SuppressLint("MissingFirebaseInstanceTokenRefresh")
class MyFirebaseMessagingService : FirebaseMessagingService() {

    private var icon: Icon? = null
    private val TAG = "MyFirebaseMessagingService"

    @RequiresApi(28)

    private lateinit var notificationManager: NotificationManager
    private lateinit var messagesList: ArrayList<MessageNotification>
    private lateinit var style: Notification.MessagingStyle
    private var totalMessages: Int? = null
    private var totalGroups: Int? = null
    private var storageReference = FirebaseStorage.getInstance().reference.child("/users")

    @RequiresApi(28)
    override fun onCreate() {
        super.onCreate()

        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        messagesList = arrayListOf()
        style = Notification.MessagingStyle("user")

    }

    @RequiresApi(29)
    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        Log.e(TAG, "RECEIVED")

        if(message.data["clearCode"]=="123123") {
            clearNotifications(message)
            if (messagesList.size > 1) {
                notificationManager.notify(
                    1,
                    createSummaryNotification(totalMessages!!, totalGroups!!)
                )
            } else {
                if (message.data["token"].toString() == FirebaseInstanceId.getInstance().id) {
                    notificationManager.cancelAll()
                }
            }
        } else {
            val group = Gson().fromJson<Group>(message.data["group"]!!, Group::class.java)
            val username = message.data["username"]
            val text = message.data["message"]

            if (message.data["token"].toString() != FirebaseInstanceId.getInstance().id && !Checking.isRunning) {

                val pictureRef = message.data["pictureRef"]
                if (pictureRef != null) {
                    Log.e(TAG, pictureRef)
                    GlideApp.with(this)
                        .asBitmap()
                        .load(storageReference.child(pictureRef + ".jpg"))
                        .circleCrop()
//                        .skipMemoryCache(true)
//                        .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                        .centerInside()
                        .into(object : CustomTarget<Bitmap>() {
                            override fun onResourceReady(
                                resource: Bitmap,
                                transition: Transition<in Bitmap>?
                            ) {
                                icon = Icon.createWithAdaptiveBitmap(resource)
                                val user = Person.Builder().setName(username)
                                    .setUri(group.getGroupId().toString()).setIcon(icon).build()
                                updateNotification(text, group, user)

                                notificationManager.notify(
                                    1,
                                    createSummaryNotification(totalMessages!!, totalGroups!!)
                                )
                                notificationManager.notify(
                                    group.getGroupId(),
                                    createNotification(
                                        contentIntent(group, username),
                                        messagesSeenActionIntent(group)
                                    )
                                )
                            }

                            override fun onLoadCleared(placeholder: Drawable?) {
                            }
                        })
                } else {
                    val user =
                        Person.Builder().setName(username).setUri(group.getGroupId().toString())
                            .build()
                    updateNotification(text, group, user)

                    notificationManager.notify(
                        1,
                        createSummaryNotification(totalMessages!!, totalGroups!!)
                    )
                    notificationManager.notify(
                        group.getGroupId(),
                        createNotification(
                            contentIntent(group, username),
                            messagesSeenActionIntent(group)
                        )
                    )
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    createNotificationChannel(notificationManager)
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.P)
    private fun updateNotification(
        text: String?,
        group: Group,
        user: Person
    ) {
        messagesList.add(MessageNotification(text!!, group.getGroupId(), user))
        style.isGroupConversation = true
        style.conversationTitle = group.getTitle()
        updateStyle(style, messagesList, group.getTitle(), user, group.getGroupId())
        totalMessages = messagesList.size
        totalGroups = messagesList.distinctBy {
            it.group
        }.size
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createSummaryNotification(totalMessages: Int, totalGroups: Int): Notification {
        return Notification.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setStyle(
                Notification.InboxStyle()
                    .setSummaryText("$totalMessages mensajes de $totalGroups grupos")
            )
            .setGroup("Groups")
            .setGroupSummary(true)
            .setShowWhen(false)
            .setGroupAlertBehavior(Notification.GROUP_ALERT_CHILDREN)
            .setVisibility(Notification.VISIBILITY_PUBLIC)
            .setOngoing(true)
            .build()
    }

    private fun messagesSeenActionIntent(group: Group): PendingIntent? {
        val seenIntent = Intent(this, MyBroadCastReceiver::class.java)
        seenIntent.putExtra("id", group.getGroupId())
        seenIntent.putExtra("groupTitle", group.getTitle())
        return getBroadcast(this, Random.nextInt(), seenIntent, FLAG_ONE_SHOT)
    }

    private fun contentIntent(
        group: Group?,
        username: String?
    ): PendingIntent? {
        val intent = Intent(this, ChatActivity::class.java)
        intent.putExtra("group", group)
        intent.putExtra("username", username)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        return getActivity(this, Random.nextInt(), intent, FLAG_IMMUTABLE)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotification(
        pendingIntent: PendingIntent?,
        seenPendingIntent: PendingIntent?
    ): Notification {
        Log.e(TAG, "Messages:" + style.messages.size.toString())
        return Notification.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setStyle(style)
            .setShowWhen(true)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setGroup("Groups")
            .setOnlyAlertOnce(true)
            .addAction(R.drawable.default_group_logo, "Marcar como leido", seenPendingIntent)
            .setNumber(style.messages.size)
            .setColor(Color.argb(90,87,142,230))
            .setVisibility(Notification.VISIBILITY_PUBLIC)
            .setOngoing(true)
            .build()


    }



    @RequiresApi(28)
    private fun clearNotifications(message: RemoteMessage) {
        if (message.data["token"] == FirebaseInstanceId.getInstance().id) {
            messagesList.removeIf {
                it.group.toString() == message.data["groupId"]
            }
            style.messages.removeIf {
                it.senderPerson!!.uri == message.data["groupId"]
            }
            totalMessages = messagesList.size
            totalGroups = messagesList.distinctBy {it.group}.size
        }
    }

    @RequiresApi(28)
    private fun createNotificationChannel(notificationManager: NotificationManager){
        val channelName = "de grupos"
        val channel = NotificationChannel(CHANNEL_ID, channelName, IMPORTANCE_HIGH).apply {
            description = "My channel description"
            enableLights(true)
            lightColor = Color.WHITE
            lockscreenVisibility = Notification.VISIBILITY_PUBLIC

        }
        notificationManager.createNotificationChannel(channel)
    }


    @RequiresApi(28)
    private fun updateStyle(style: Notification.MessagingStyle, listOfMessages : ArrayList<MessageNotification>, groupTitle : String,  user : Person, groupId : Int){
        val tempList = listOfMessages.filter {
            it.group == groupId
        }
        if(style.messages.isNotEmpty()) {
            if (style.messages.last().senderPerson!!.uri != groupId.toString()) {
                style.messages.clear()
                for (msg in tempList) {
                    style.addMessage(msg.message, System.currentTimeMillis(), msg.user)
                }
            }
            else{
                style.addMessage(tempList.last().message, System.currentTimeMillis(), tempList.last().user)
            }
        }
        else{
            style.addMessage(tempList.last().message, System.currentTimeMillis(), tempList.last().user)
        }
     }
}

