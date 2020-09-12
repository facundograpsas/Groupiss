package com.example.groupis.activities.chat

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ChatViewModel : ViewModel() {

    var message = MutableLiveData<String?>()

    fun setMessage(message: String){
        this.message.value = message
    }

    fun getMessage(): String?{
        return message.value
    }

}