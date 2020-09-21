package com.example.groupis.activities.signIn

import android.app.Activity
import android.util.Log
import android.widget.Toast
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.*
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class SignInWithGoogleHandler(task: Task<GoogleSignInAccount>, activity : Activity, toMainActivity: () -> Unit?){


    private lateinit var firebaseAuth : FirebaseAuth
    init {
        handleSignInResult(task, activity,toMainActivity)
    }

    private fun handleSignInResult(task: Task<GoogleSignInAccount>,  activity : Activity, toMainActivity: () -> Unit?) {
        try{
            firebaseAuth = FirebaseAuth.getInstance()
            firebaseAuthWithGoogle(task.result!!.idToken!!, activity, toMainActivity)
        }catch (e: ApiException){
            Log.w("Log in", "sigInResult: failed code = " + e.statusCode)
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String, activity : Activity, toMainActivity: () -> Unit?) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        firebaseAuth.signInWithCredential(credential)
            .addOnCompleteListener(activity) {task : Task<AuthResult> ->
                if (task.isSuccessful) {
                    Log.d("TAG", "signInWithCredential:success")
                    val user = firebaseAuth!!.currentUser
                    registerNewUser(user!!)
                    toMainActivity()
                } else {
                    Log.w("TAG", "signInWithCredential:failure", task.exception)
                    Toast.makeText(activity.applicationContext, "Conexion no disponible", Toast.LENGTH_LONG).show()
                }
            }
    }

    private fun registerNewUser(user : FirebaseUser){
        val refUser = FirebaseDatabase.getInstance().reference.child("Users").child(user.uid)
        refUser.addListenerForSingleValueEvent(object: ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    if(!snapshot.exists()){
                        val userHashMap = HashMap<String, Any>()
                        userHashMap["uid"] = user.uid
                        userHashMap["name"] = user.displayName!!
                        userHashMap["email"] = user.email!!
                        refUser.updateChildren(userHashMap)
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                }
        })
    }
}