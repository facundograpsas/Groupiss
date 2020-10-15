package com.app.groupis.activities

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.app.groupis.models.User

class SplashViewModel : ViewModel() {

    lateinit var userLogged: LiveData<Boolean>
    var hasUsername = MutableLiveData<Boolean>()
    var mUser = MutableLiveData<User>()

    private val splashRepository = SplashRepository()

    fun checkIfUserIsLogged() {
        userLogged = splashRepository.checkIfUserIsLogged()
    }

    fun loadUser() {
        splashRepository.getUser(object : UserCallback {
            override fun onCallback(user: User) {
                mUser.value = user
                hasUsername.value = user.getNameId() != null
            }
        })
    }

    fun getUser(): User {
        return mUser.value!!
    }


}