package com.app.groupis.notifications

import com.app.groupis.notifications.Constants.Companion.CONTENT_TYPE
import com.app.groupis.notifications.Constants.Companion.SERVER_KEY
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface NotificationAPI {

    @Headers("Authorization: key=$SERVER_KEY", "Content-Type:$CONTENT_TYPE")
    @POST("fcm/send")
    suspend fun postNotificationMessage(
        @Body data : PushNotificationMessage
    ): Response<ResponseBody>


    @Headers("Authorization: key=$SERVER_KEY", "Content-Type:$CONTENT_TYPE")
    @POST("fcm/send")
    suspend fun postClearNotifications(
        @Body data : PushClearNotifications
    ): Response<ResponseBody>
}