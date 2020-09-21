package com.example.groupis.activities.profile

import android.app.Dialog
import android.os.Bundle
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.example.groupis.R


class UsernameDialog(private var username: String) : DialogFragment() {

    private lateinit var editText : EditText


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it, R.style.ThemeOverlay_AppCompat_Dialog_Alert)
            val inflater = requireActivity().layoutInflater
            val dialogView = inflater.inflate(R.layout.dialog_change_username, null)
            editText = dialogView.findViewById<EditText>(R.id.alert_dialog_username)
            val viewModel : UserProfileViewModel by activityViewModels()
            builder.setView(dialogView)
                // Add action buttons
                .setPositiveButton("Aceptar"
                ) { _, _ ->
                    viewModel.changeUsername(editText.text.toString(), object: UsernameCallback{
                        override fun onCallback(value: String) {
                            println(value)
                            viewModel.setValid(value)
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
}

interface UsernameCallback{
    fun onCallback(value : String)
}
