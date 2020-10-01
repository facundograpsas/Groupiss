package com.example.groupis.activities.newgroup


import android.animation.Animator
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.airbnb.lottie.LottieAnimationView
import com.example.groupis.R
import com.example.groupis.activities.main.GroupViewModel
import com.example.groupis.activities.main.MyGroupViewModel
import com.example.groupis.activities.main.UserViewModel
import com.example.groupis.activities.profile.UsernameCallback
import com.example.groupis.models.Group
import com.example.groupis.models.User
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.activity_new_group.*
import java.io.InputStream

private val GALLERY_REQUEST: Int = 23


class NewGroupActivity : AppCompatActivity() {

    private var bitmap: Bitmap? = null
    private lateinit var profileImageView : ImageView
    private lateinit var profileImageCircle : CircleImageView
    private lateinit var cropImage : CropImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_group)

        val user = intent.getSerializableExtra("user") as? User

        val groupViewModel : GroupViewModel by viewModels()
        val userViewModel : UserViewModel by viewModels()
        val myGroupViewModel : MyGroupViewModel by viewModels()
        val createGroupButton = findViewById<Button>(R.id.new_group_layout_create_group_button)
        val backButton = findViewById<Button>(R.id.new_group_layout_back_button)
        val groupName = findViewById<EditText>(R.id.new_group_layout_editText_groupName)
        val successAnimation = findViewById<LottieAnimationView>(R.id.new_group_layout_success_animation)
        val setPictureFab = findViewById<FloatingActionButton>(R.id.new_group_layout_set_picture_fab)
        profileImageView = findViewById<ImageView>(R.id.new_group_layout_profile_image)
        profileImageCircle = findViewById(R.id.new_group_layout_profile_image_circle)
        cropImage = findViewById(R.id.cropImageView)

        onBackButtonClick(backButton)
        onAnimationEnd(successAnimation, groupViewModel, myGroupViewModel, user!!)

        createGroupButton.setOnClickListener {
            onCreateGroupClick(groupViewModel, groupName, userViewModel, user)
        }


        groupViewModel.newGroupState.observe(this, Observer { result ->
            when (result) {
                "LARGO INVALIDO" -> Toast.makeText(
                    this@NewGroupActivity,
                    "El nombre del grupo debe tener entre 4 y 16 caracteres.",
                    Toast.LENGTH_LONG
                ).show()
                "EXISTE" -> Toast.makeText(
                    this@NewGroupActivity,
                    "Ya hay un grupo con ese nombre",
                    Toast.LENGTH_LONG
                ).show()
                "NO EXISTE" -> onGroupCreated(successAnimation)
            }
        })



        setPictureFab.setOnClickListener {
//            val intent = Intent(Intent.ACTION_PICK)
//            intent.type = "image/**"
//            startActivityForResult(intent, GALLERY_REQUEST)
            CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .start(this)
        }


    }

    private fun onAnimationEnd(
        successAnimation: LottieAnimationView,
        groupViewModel: GroupViewModel,
        myGroupViewModel: MyGroupViewModel,
        user: User
    ) {
        successAnimation.addAnimatorListener(object : Animator.AnimatorListener {
            override fun onAnimationRepeat(animation: Animator?) {}
            override fun onAnimationCancel(animation: Animator?) {}
            override fun onAnimationStart(animation: Animator?) {}
            override fun onAnimationEnd(animation: Animator?) {
                toChatActivity(groupViewModel, user, groupViewModel.getNewGroupName()!!, groupViewModel.getNewGroupId())
                finish()
            }
        })
    }

    private fun onCreateGroupClick(
        groupViewModel: GroupViewModel,
        groupName: EditText,
        userViewModel: UserViewModel,
        user: User
    ) {
            groupViewModel.setNewGroupName(groupName.text.toString())
            groupViewModel.addNewGroup(groupViewModel.getNewGroupName()!!, object :
                UsernameCallback {
                override fun onCallback(value: String) {
                    when (value) {
                        "EXISTE" -> {
                            groupViewModel.setGroupState(value)
                        }
                        "LARGO INVALIDO" -> {
                            groupViewModel.setGroupState(value)
                        }
                        "NO EXISTE" -> {
                            groupViewModel.setGroupState(value)
                        }
                    }
                }
            }, user, bitmap)
            groupViewModel.setFill(true)
        }

    private fun onBackButtonClick(backButton: Button){
        backButton.setOnClickListener {super.onBackPressed() }
    }

    private fun onGroupCreated(animation: LottieAnimationView){
        animation.playAnimation()
        Toast.makeText(this@NewGroupActivity, "Grupo creado exitosamente", Toast.LENGTH_LONG).show()
    }

    private fun toChatActivity(groupViewModel: GroupViewModel, user: User, groupName: String, groupId : Int){
        groupViewModel.goToNewGroup(this@NewGroupActivity, user)
    }


//    @RequiresApi(Build.VERSION_CODES.N)
//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
////        profileImageView = findViewById(R.id.new_group_layout_profile_image)
//        super.onActivityResult(requestCode, resultCode, data)
//        if(resultCode==RESULT_OK){
//            when(requestCode){
//                GALLERY_REQUEST -> {
//                    val selectedImage = data!!.data
//                    val orientation = getImageOrientation(selectedImage)
//                    var rotatedBitmap : Bitmap? = null
//                    try {
//                        bitmap = MediaStore.Images.Media.getBitmap(
//                            this.contentResolver,
//                            selectedImage
//                        )
//                        when (orientation) {
//                            ExifInterface.ORIENTATION_ROTATE_90 -> rotatedBitmap = rotateImage(bitmap!!, 90F)
//                        }
////                        profileImageView.setImageBitmap(bitmap)
//                        profileImageCircle.setImageBitmap(rotatedBitmap)
//                    } catch (e: Exception) {
//                        Log.i("New Group", e.message.toString())
//                    }
//                }
//            }
//        }
//    }

//    @RequiresApi(Build.VERSION_CODES.N)
//    private fun getImageOrientation(selectedImage: Uri?): Int {
//        var inputStream: InputStream? =
//            applicationContext.contentResolver!!.openInputStream(selectedImage!!)
//        val exif = ExifInterface(inputStream!!)
//        return exif.getAttributeInt(
//            ExifInterface.TAG_ORIENTATION,
//            ExifInterface.ORIENTATION_UNDEFINED
//        )
//    }

//    private fun rotateImage(source: Bitmap, angle: Float): Bitmap? {
//        val matrix = Matrix()
//        matrix.postRotate(angle)
//        return Bitmap.createBitmap(
//            source, 0, 0, source.width, source.height,
//            matrix, true
//        )
//    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            val result = CropImage.getActivityResult(data)
            if (resultCode == RESULT_OK) {
                val resultUri = result.uri
                cropImageView.setImageUriAsync(resultUri)
                profileImageCircle.setImageURI(resultUri)
                bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, resultUri)
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                val error = result.error
            }
        }
    }

}