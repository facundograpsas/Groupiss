package com.example.groupis.notifications

import com.example.groupis.models.Group

data class PushNotificationMessage(val data : NotificationData, val to : String)