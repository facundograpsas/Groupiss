package com.app.groupis.activities.signIn

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import com.airbnb.lottie.LottieAnimationView
import com.app.groupis.R
import com.app.groupis.activities.main.MainActivity
import com.app.groupis.activities.username.SetUsernameActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.SignInButton
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class SignInActivity : AppCompatActivity() {

    private lateinit var signInButton: SignInButton
    private lateinit var mGoogleSignInClient: GoogleSignInClient
    private lateinit var progressBar: ProgressBar
    private lateinit var loadingAnimation: LottieAnimationView
    val TAG = "SigInActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)


        progressBar = findViewById(R.id.loadingProgress)
        loadingAnimation = findViewById(R.id.loading_animation)
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
//        progressBar.visibility = View.VISIBLE
        loadingAnimation.visibility = View.VISIBLE
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode==384){
            val task : Task<GoogleSignInAccount> = GoogleSignIn.getSignedInAccountFromIntent(data)
            loadingAnimation.visibility = View.VISIBLE
            if(task.isSuccessful) {
                Log.e(TAG, "Task successful")
                SignInWithGoogleHandler(task, this) { toMainActivity() }
            }
            else{
                loadingAnimation.visibility = View.INVISIBLE
            }
        }
    }

    private fun toMainActivity(){
        val refUser = FirebaseDatabase.getInstance().reference.child("Users").child(FirebaseAuth.getInstance().currentUser!!.uid).child("nameId")
        refUser.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                Log.e(TAG, snapshot.children.toString())
                Log.e(TAG, snapshot.value.toString())
                Log.e(TAG, snapshot.exists().toString())


                if (!snapshot.exists()) {
                    Log.e(TAG, "No ExistSxd")
                    startActivity(Intent(this@SignInActivity, SetUsernameActivity::class.java))
                    finish()
                } else {
                    Log.e(TAG, "Existteeeeeeeee")
                    startActivity(Intent(this@SignInActivity, MainActivity::class.java))
                    finish()
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }
}
