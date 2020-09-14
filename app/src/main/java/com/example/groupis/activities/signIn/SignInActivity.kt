package com.example.groupis.activities.signIn

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import androidx.activity.viewModels
import com.example.groupis.R
import com.example.groupis.activities.main.MainActivity
import com.example.groupis.activities.username.SetUsernameActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.SignInButton
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class SignInActivity : AppCompatActivity() {

    private lateinit var signInButton : SignInButton
    private lateinit var mGoogleSignInClient : GoogleSignInClient
    private lateinit var progressBar : ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)

        progressBar = findViewById(R.id.loadingProgress)
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestIdToken(getString(R.string.default_web_client_id))
            .build()
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)
        signInButton = findViewById(R.id.sign_in_button)
        signInButton.setSize(1)
        signInButton.setOnClickListener{
            signIn()
        }

    }

    private fun signIn() {
        val signInIntent : Intent = mGoogleSignInClient.signInIntent
        startActivityForResult(signInIntent, 384)
        progressBar.visibility = View.VISIBLE
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode==384){
            val task : Task<GoogleSignInAccount> = GoogleSignIn.getSignedInAccountFromIntent(data)
            progressBar.visibility = View.VISIBLE
            if(task.isSuccessful) {
                SignInWithGoogleHandler(task, this) { toMainActivity() }
            }
            else{
                progressBar.visibility = View.INVISIBLE
            }
        }
    }

    private fun toMainActivity(){
        val refUser = FirebaseDatabase.getInstance().reference.child("Users").child(FirebaseAuth.getInstance().currentUser!!.uid).child("nameId")
        refUser.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if(!snapshot.exists()){
                    startActivity(Intent(this@SignInActivity, SetUsernameActivity::class.java))
                    finish()
                }
                else{
                    startActivity(Intent(this@SignInActivity, MainActivity::class.java))
                    finish()
                }
            }
            override fun onCancelled(error: DatabaseError) {
            }
        })
    }
}
