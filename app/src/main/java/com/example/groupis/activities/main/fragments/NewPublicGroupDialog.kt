package com.example.groupis.activities.main.fragments

import android.app.Dialog
import android.os.Bundle
import android.widget.EditText
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.example.groupis.R
import com.example.groupis.activities.main.GroupViewModel
import com.example.groupis.activities.main.UserViewModel
import com.example.groupis.activities.profile.UsernameCallback
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class NewPublicGroupDialog : DialogFragment() {

    private lateinit var groupName : EditText


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it, R.style.ThemeOverlay_AppCompat_Dialog_Alert)
            val inflater = requireActivity().layoutInflater
            val dialogView = inflater.inflate(R.layout.dialog_new_group, null)
            groupName = dialogView.findViewById<EditText>(R.id.public_group_dialog_group_name_edit)
            val viewModel : GroupViewModel by activityViewModels()
            val userViewModel : UserViewModel by activityViewModels()

            println(it)
            builder.setView(dialogView)
                // Add action buttons
                .setPositiveButton("Crear Grupi") { _, _ ->
                    viewModel.addNewGroup(groupName.text.toString(),userViewModel,object : UsernameCallback{
                        override fun onCallback(value: String) {
                            when (value) {
                                "EXISTE" -> { viewModel.setGroupState(value) }
                                "LARGO INVALIDO" -> { viewModel.setGroupState(value) }
                                "NO EXISTE" -> { viewModel.setGroupState(value)
                                }
                            }
                        }
                    })
                    viewModel.setFill(true)
                }
                .setNegativeButton("Cancelar") { _, _ ->
                    dialog!!.cancel()
                }
            builder.create()

        } ?: throw IllegalStateException("Activity cannot be null")
    }
}