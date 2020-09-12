package com.example.groupis.activities.username

import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.Observer
import com.example.groupis.R
import com.example.groupis.activities.main.MainActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class SetUsernameActivity : AppCompatActivity() {

    private lateinit var usernameEditText: EditText
    private lateinit var acceptButton : Button
    private lateinit var usernameAvailable : TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_set_username)
        val viewModel : UsernameViewModel by viewModels()
        viewModel.isValid.value = false
//        viewModel.isFree.value = false


        usernameAvailable = findViewById(R.id.set_username_warning)
        usernameEditText = findViewById(R.id.username_activity_username)

        usernameValueAndValid(viewModel)
        saveUsernameToDB(viewModel)
        checkUsernameIsAvailable(viewModel)
        updateUIOnUsernameAvailable(viewModel)
        updateUIOnUsernameValid(viewModel)
    }

    private fun checkUsernameIsAvailable(viewModel: UsernameViewModel) {
        viewModel.userName.observe(this, Observer { username ->
            val refUsername =
                FirebaseDatabase.getInstance().reference.child("Users").orderByChild("username")
                    .equalTo(username)
            refUsername.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        viewModel.isFree.value = false
                        println("EXISTE")
                    } else {
                        viewModel.isFree.value = true
                        println("NO EXISTE")
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                }
            })
        })
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
        val refUser = FirebaseDatabase.getInstance().reference.child("Users")
            .child(FirebaseAuth.getInstance().currentUser!!.uid)
        acceptButton = findViewById(R.id.set_username_button)
        acceptButton.setOnClickListener {
            if (viewModel.isFree.value!! && viewModel.isValid.value!!) {
                refUser.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            val userHashMap = HashMap<String, Any>()
                            userHashMap["username"] = viewModel.userName.value!!
                            refUser.updateChildren(userHashMap)
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                    }
                })
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