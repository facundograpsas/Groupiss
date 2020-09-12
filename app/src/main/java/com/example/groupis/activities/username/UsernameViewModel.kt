package com.example.groupis.activities.username

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class UsernameViewModel : ViewModel() {

    val userName: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }

    val isValid: MutableLiveData<Boolean> by lazy {
        MutableLiveData<Boolean>()
    }

    val isFree: MutableLiveData<Boolean> by lazy {
        MutableLiveData<Boolean>()
    }
}

