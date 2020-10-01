package com.example.groupis.notifications

import android.app.Notification
import android.app.Person
import android.os.Build
import androidx.annotation.RequiresApi

@RequiresApi(Build.VERSION_CODES.N)
class Asdasd : Notification.MessagingStyle("asd") {
    override fun addMessage(text: CharSequence, timestamp: Long, sender: Person?
    ): Notification.MessagingStyle {
        return super.addMessage(text, timestamp, sender)
    }
}