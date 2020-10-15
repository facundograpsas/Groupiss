package com.app.groupis.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import com.app.groupis.R
import com.app.groupis.activities.main.MainActivity
import com.app.groupis.activities.signIn.SignInActivity
import com.app.groupis.activities.username.SetUsernameActivity
import com.app.groupis.models.User

class SplashActivity : AppCompatActivity() {

    private val TAG = "SplashViewModel"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val splashViewModel: SplashViewModel by viewModels()

        splashViewModel.checkIfUserIsLogged()
        checkIfUserIsLogged(splashViewModel)


    }

    private fun checkIfUserIsLogged(splashViewModel: SplashViewModel) {
        splashViewModel.userLogged.observe(this, Observer { isLogged ->
            if (isLogged) {
                splashViewModel.loadUser()
                splashViewModel.hasUsername.observe(this, Observer { hasUsername ->
                    if (hasUsername) {
                        Log.e(TAG, splashViewModel.getUser().getEmail()!!)
                        goToMainActivity(splashViewModel.getUser())
                    } else {
                        goToUsernameActivity(splashViewModel.getUser())
                    }
                })
            } else {
                goToSignInActivity()
            }
        })
    }

    private fun goToUsernameActivity(user: User) {
        startActivity(
            Intent(this@SplashActivity, SetUsernameActivity::class.java).putExtra(
                "user",
                user
            )
        )
        finish()
    }

    private fun goToMainActivity(user: User) {
        startActivity(Intent(this@SplashActivity, MainActivity::class.java).putExtra("user", user))
        this.overridePendingTransition(0, 0)
        finish()
    }

    private fun goToSignInActivity() {
        startActivity(Intent(this@SplashActivity, SignInActivity::class.java))
        finish()
    }
}