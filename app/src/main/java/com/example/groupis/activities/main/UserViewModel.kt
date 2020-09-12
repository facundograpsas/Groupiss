package com.example.groupis.activities.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.groupis.models.User

class UserViewModel : ViewModel() {

    var _username = MutableLiveData<String>()
    val user = MutableLiveData<User>()
    val userLoaded = MutableLiveData<Boolean>()


    fun setUsername(username: String) {
        _username.value = username
    }

    fun getUserName(): String {
        return _username.value!!
    }


    fun getUser(): LiveData<User> {
        return user
    }

    fun setUser(user: User) {
        this.user.value = user
    }

    fun setUserLoaded(isLoad : Boolean){
        userLoaded.value = isLoad
    }
}