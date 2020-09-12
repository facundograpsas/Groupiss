package com.example.groupis.activities.signIn

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SignInViewModel : ViewModel() {

    val newUser: MutableLiveData<Boolean> by lazy {
        MutableLiveData<Boolean>()
    }
}