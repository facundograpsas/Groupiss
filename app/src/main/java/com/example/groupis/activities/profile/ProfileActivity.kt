package com.example.groupis.activities.profile

import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.*
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import com.example.groupis.R
import com.example.groupis.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ProfileActivity : AppCompatActivity() {

    private lateinit var profileName : TextView
    private lateinit var editUsername : RelativeLayout
    private lateinit var profileImage : ImageView
    private lateinit var user : User
    private lateinit var myPrefs : SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        myPrefs = getSharedPreferences("prefs", MODE_PRIVATE)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        profileName = findViewById(R.id.profile_name_text)
        profileName.text = intent.getStringExtra("username")
        if(profileName.text==null){
            FirebaseDatabase.getInstance().reference.child("Users").child(FirebaseAuth.getInstance().currentUser!!.uid)
                .addListenerForSingleValueEvent(object : ValueEventListener{
                    override fun onDataChange(snapshot: DataSnapshot) {
                        user = snapshot.getValue(User::class.java)!!
                    }
                    override fun onCancelled(error: DatabaseError) {
                    }
                })
            profileName.text = user.getUsername()
        }

        editUsername = findViewById(R.id.edit_username)
        profileImage = findViewById(R.id.profile_image_profile)
        editUsername.setOnClickListener {
            changeUsernameDialog() }

        val viewModel : UserProfileViewModel by viewModels()
        changeUsernameResult(viewModel)
    }

    private fun changeUsernameResult(viewModel: UserProfileViewModel) {
        viewModel.isValid.observe(this, Observer { valid ->
            when (valid) {
                "NO EXISTE" -> {
                    profileName.text = viewModel.getUsername().value
                    myPrefs.edit().putString(FirebaseAuth.getInstance().currentUser!!.uid, viewModel.getUsername().value).apply()
                    Toast.makeText(this, "El nombre de usuario se ha cambiado correctamente.", Toast.LENGTH_LONG).show()
                }
                "EXISTE" -> { Toast.makeText(this, "Ya hay un usuario con ese nombre UwU", Toast.LENGTH_LONG).show()}
                "LARGO INVALIDO" -> { Toast.makeText(this, "El nombre de usuario debe tener entre 4 y 16 caracteres.", Toast.LENGTH_LONG).show() }
                "ESPACIO INVALIDO" -> { Toast.makeText(this, "El nombre de usuario debe tener entre 4 y 16 caracteres.", Toast.LENGTH_LONG).show() }

            }
        })
    }

    private fun changeUsernameDialog() {
        val newFragment = UsernameDialog(profileName.text.toString())
        newFragment.show(supportFragmentManager, "NoticeDialogFragment")
    }

    override fun onResume() {
        super.onResume()
        if(this.resources.configuration.orientation==Configuration.ORIENTATION_PORTRAIT){
            profileImage.scaleX = 1.0F
            profileImage.scaleY = 1.0F

        }
        else if(this.resources.configuration.orientation==Configuration.ORIENTATION_LANDSCAPE){
            profileImage.scaleX = 0.5F
            profileImage.scaleY = 0.5F
        }
    }
}