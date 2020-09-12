package com.example.groupis.activities.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.groupis.models.User

class UserProfileViewModel : ViewModel() {

        val username = MutableLiveData<String>()
        val isValid = MutableLiveData<String>()
    val user = MutableLiveData<User>()


        fun getUsername(): LiveData<String> {
            return username
        }

        fun setUsername(username : String){
            this.username.value = username
        }


        fun setValid(valid : String){
            this.isValid.value = valid
        }

        fun getUser(): LiveData<User>{
            return user
        }

        fun setUser(user : User){
            this.user.value = user
        }

    }