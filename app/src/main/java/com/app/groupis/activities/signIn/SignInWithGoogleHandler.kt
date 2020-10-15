package com.app.groupis.activities.signIn

import android.app.Activity
import android.util.Log
import android.widget.Toast
import com.app.groupis.activities.main.lobby.Callback
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.*
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class SignInWithGoogleHandler(
    task: Task<GoogleSignInAccount>,
    activity: Activity,
    val viewModel: SignInViewModel
) {


    private lateinit var firebaseAuth: FirebaseAuth

    init {
        handleSignInResult(task, activity)
    }

    private fun handleSignInResult(task: Task<GoogleSignInAccount>, activity: Activity) {
        try {
            firebaseAuth = FirebaseAuth.getInstance()
            firebaseAuthWithGoogle(task.result!!.idToken!!, activity)
        } catch (e: ApiException) {
            Log.w("Log in", "sigInResult: failed code = " + e.statusCode)
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String, activity: Activity) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        firebaseAuth.signInWithCredential(credential)
            .addOnCompleteListener(activity) { task: Task<AuthResult> ->
                if (task.isSuccessful) {
                    Log.e("TAG", "signInWithCredential:success")
                    val user = firebaseAuth.currentUser
//                    FirebaseDatabase.getInstance().reference.child("Users").child(user!!.uid)
//                        .addListenerForSingleValueEvent(object : ValueEventListener{
//                            override fun onDataChange(snapshot: DataSnapshot) {
//
//                            }
//
//                            override fun onCancelled(error: DatabaseError) {
//                            }
//
//                        })
                    registerNewUser(user!!)
                } else {
                    Log.w("TAG", "signInWithCredential:failure", task.exception)
                    Toast.makeText(activity.applicationContext, "Conexion no disponible", Toast.LENGTH_LONG).show()
                }
            }
    }

    private fun registerNewUser(user: FirebaseUser) {
        val refUser = FirebaseDatabase.getInstance().reference.child("Users").child(user.uid)
        val userHashMap = HashMap<String, Any>()
        userHashMap["uid"] = user.uid
        userHashMap["name"] = user.displayName!!
        userHashMap["email"] = user.email!!
        refUser.updateChildren(userHashMap)
        viewModel.newUser.value = true
    }

}

