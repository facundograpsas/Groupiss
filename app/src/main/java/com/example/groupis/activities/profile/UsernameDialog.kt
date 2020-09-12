package com.example.groupis.activities.profile

import android.app.Dialog
import android.content.DialogInterface
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.example.groupis.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class UsernameDialog(private var username: String) : DialogFragment() {

    private lateinit var editText : EditText


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it, R.style.ThemeOverlay_AppCompat_Dialog_Alert)
            val inflater = requireActivity().layoutInflater
            val dialogView = inflater.inflate(R.layout.dialog_change_username, null)
            editText = dialogView.findViewById<EditText>(R.id.alert_dialog_username)
            val viewModel : UserProfileViewModel by activityViewModels()
            println(it)
            builder.setView(dialogView)
                // Add action buttons
                .setPositiveButton("Aceptar"
                ) { _, _ ->
                    readData(object : UsernameCallback {
                        override fun onCallback(value: String) {
                            when (value) {
                                "EXISTE" -> { viewModel.setValid("EXISTE") }
                                "NO EXISTE" -> { viewModel.setValid("NO EXISTE") }
                                "LARGO INVALIDO" -> { viewModel.setValid("LARGO INVALIDO") }
                                "ESPACIO INVALIDO" -> { viewModel.setValid("ESPACIO INVALIDO")}
                            }
                        }
                    })
                    viewModel.setUsername(editText.text.toString())
                }
                .setNegativeButton("Cancelar"
                ) { _, _ ->
                    dialog!!.cancel()
                }
            editText.setText(username)
            builder.create()

        } ?: throw IllegalStateException("Activity cannot be null")
    }

    private fun readData(callback: UsernameCallback){

        val dbRef = FirebaseDatabase.getInstance().reference.child("Users").orderByChild("username").equalTo(editText.text.toString())
        dbRef.addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()){
                    callback.onCallback("EXISTE")
                }else{
                    if(editText.text.toString().length<4 || editText.text.toString().length>16){
                        callback.onCallback("LARGO INVALIDO")
                    }
                    else if(editText.text.toString().contains(" ")){
                        callback.onCallback("ESPACIO INVALIDO")
                    }
                    else {
                        val userRef = FirebaseDatabase.getInstance().reference.child("Users")
                            .child(FirebaseAuth.getInstance().currentUser!!.uid)
                        val hashMap = HashMap<String, Any>()
                        hashMap["username"] = editText.text.toString()
                        userRef.updateChildren(hashMap)
                        callback.onCallback("NO EXISTE")
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {

            }
        })
    }
}

interface UsernameCallback{
    fun onCallback(value : String)
}
