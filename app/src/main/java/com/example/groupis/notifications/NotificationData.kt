package com.example.groupis.notifications

import com.example.groupis.models.Group

data class NotificationData(val group : Group, val message : String, val username : String, val token : String)

data class NotificationClear(val clearCode : String, val token : String, val groupId : Int)