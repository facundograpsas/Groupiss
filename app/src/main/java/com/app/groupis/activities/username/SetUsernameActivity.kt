package com.app.groupis.activities.username

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.Observer
import com.app.groupis.R
import com.app.groupis.activities.main.MainActivity
import com.google.firebase.auth.FirebaseAuth


class SetUsernameActivity : AppCompatActivity() {

    private val TAG = "SetUsernameActivity"
    private lateinit var usernameEditText: EditText
    private lateinit var acceptButton : Button
    private lateinit var usernameAvailable : TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_set_username)
        val viewModel : UsernameViewModel by viewModels()
        viewModel.isValid.value = false

        usernameAvailable = findViewById(R.id.set_username_warning)
        usernameEditText = findViewById(R.id.username_activity_username)

        usernameValueAndValid(viewModel)
        saveUsernameToDB(viewModel)
        viewModel.checkUsernameAvailable(this@SetUsernameActivity)
        updateUIOnUsernameAvailable(viewModel)
        updateUIOnUsernameValid(viewModel)
    }

    private fun updateUIOnUsernameValid(viewModel: UsernameViewModel) {
        viewModel.isValid.observe(this, Observer { valid ->
            if (!valid) {
                usernameAvailable.visibility = View.VISIBLE
                usernameAvailable.text = "El nombre de usuario debe tener entre 4 y 16 caracteres. Los espacios no estan permitidos"
                usernameEditText.backgroundTintList =
                    ContextCompat.getColorStateList(this, R.color.colorRed)
            } else if (valid && viewModel.isFree.value == true) {
                usernameAvailable.visibility = View.INVISIBLE
                usernameEditText.backgroundTintList =
                    ContextCompat.getColorStateList(this, R.color.colorGreen)
            }
        })
    }

    private fun updateUIOnUsernameAvailable(viewModel: UsernameViewModel) {
        viewModel.isFree.observe(this, Observer { isFree ->
            if (!isFree) {
                usernameAvailable.visibility = View.VISIBLE
                usernameAvailable.text = "Ya hay un usuario registrado con ese nombre."
                usernameEditText.backgroundTintList =
                    ContextCompat.getColorStateList(this, R.color.colorRed)
            } else if (isFree && viewModel.isValid.value!!) {
                usernameAvailable.visibility = View.INVISIBLE
                usernameEditText.backgroundTintList =
                    ContextCompat.getColorStateList(this, R.color.colorGreen)
            }
        })
    }

    private fun usernameValueAndValid(viewModel: UsernameViewModel) {
        usernameEditText.doAfterTextChanged {
            viewModel.userName.value = it.toString()
            viewModel.isValid.value = !(it.toString().length < 4 || it.toString().length > 16 || it.toString().contains(" "))
        }
    }

    private fun saveUsernameToDB(viewModel: UsernameViewModel) {
        acceptButton = findViewById(R.id.set_username_button)
        acceptButton.setOnClickListener {
            if (viewModel.isFree.value!! && viewModel.isValid.value!!) {
                Log.e(TAG, "DENTRO DEDDDEDEDEDEDED")
                viewModel.saveUsernameToDB()
                val prefs = getSharedPreferences("prefs", MODE_PRIVATE)!!.edit()
                prefs.putString(FirebaseAuth.getInstance().currentUser!!.uid, viewModel.userName.value)
                prefs.apply()
                val intent = Intent(this@SetUsernameActivity, MainActivity::class.java)
                startActivity(intent)
                finish()
            }
        }
    }
}