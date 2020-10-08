package com.app.groupis.notifications

import com.app.groupis.models.Group

data class NotificationData(
    val group: Group,
    val message: String,
    val username: String,
    val token: String,
    val pictureRef: String?
)

data class NotificationClear(val clearCode : String, val token : String, val groupId : Int)