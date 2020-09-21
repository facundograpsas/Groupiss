package com.example.groupis.activities.newgroup


import android.animation.Animator
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import com.airbnb.lottie.LottieAnimationView
import com.example.groupis.R
import com.example.groupis.activities.main.GroupViewModel
import com.example.groupis.activities.main.UserViewModel
import com.example.groupis.activities.profile.UsernameCallback
import com.example.groupis.models.Group
import com.example.groupis.models.User
import com.google.firebase.auth.FirebaseAuth

class NewGroupActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_group)

        val user = intent.getSerializableExtra("user") as? User

        val groupViewModel : GroupViewModel by viewModels()
        val userViewModel : UserViewModel by viewModels()
        val createGroupButton = findViewById<Button>(R.id.new_group_layout_create_group_button)
        val backButton = findViewById<Button>(R.id.new_group_layout_back_button)
        val groupName = findViewById<EditText>(R.id.new_group_layout_editText_groupName)
        val successAnimation = findViewById<LottieAnimationView>(R.id.new_group_layout_success_animation)

        onBackButtonClick(backButton)
        onAnimationEnd(successAnimation, groupViewModel, user!!)

        createGroupButton.setOnClickListener {
            onCreateGroupClick(groupViewModel, groupName, userViewModel, user)
        }


        groupViewModel.newGroupState.observe(this, Observer { result ->
            when (result) {
                "LARGO INVALIDO" -> Toast.makeText(this@NewGroupActivity, "El nombre del grupo debe tener entre 4 y 16 caracteres.", Toast.LENGTH_LONG).show()
                "EXISTE" -> Toast.makeText(this@NewGroupActivity, "Ya hay un grupo con ese nombre", Toast.LENGTH_LONG).show()
                "NO EXISTE" -> onGroupCreated(successAnimation)
            }
        })
    }

    private fun onAnimationEnd(successAnimation: LottieAnimationView, groupViewModel: GroupViewModel, user : User) {
        successAnimation.addAnimatorListener(object : Animator.AnimatorListener {
            override fun onAnimationRepeat(animation: Animator?) {}
            override fun onAnimationCancel(animation: Animator?) {}
            override fun onAnimationStart(animation: Animator?) {}
            override fun onAnimationEnd(animation: Animator?) {
                toChatActivity(groupViewModel, user, groupViewModel.getNewGroupName()!!)
                finish()
           }
        })
    }

    private fun onCreateGroupClick(groupViewModel: GroupViewModel, groupName: EditText, userViewModel: UserViewModel, user : User) {
            groupViewModel.setNewGroupName(groupName.text.toString())
            groupViewModel.addNewGroup(groupViewModel.getNewGroupName()!!, userViewModel, object :
                UsernameCallback {
                override fun onCallback(value: String) {
                    when (value) {
                        "EXISTE" -> { groupViewModel.setGroupState(value) }
                        "LARGO INVALIDO" -> { groupViewModel.setGroupState(value) }
                        "NO EXISTE" -> { groupViewModel.setGroupState(value) }
                    }
                }
            }, user)
            groupViewModel.setFill(true)
        }

    private fun onBackButtonClick(backButton : Button){
        backButton.setOnClickListener {super.onBackPressed() }
    }

    private fun onGroupCreated(animation : LottieAnimationView){
        animation.playAnimation()
        Toast.makeText(this@NewGroupActivity, "Grupo creado exitosamente", Toast.LENGTH_LONG).show()
    }

    private fun toChatActivity(groupViewModel: GroupViewModel, user : User, groupName: String){
        groupViewModel.onMyGroupClick(this@NewGroupActivity, user, FirebaseAuth.getInstance().currentUser!!.uid, Group(groupName))
    }

}