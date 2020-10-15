package com.app.groupis.activities.profile

import android.app.Instrumentation
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.graphics.*
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.scale
import androidx.lifecycle.Observer
import com.app.groupis.GlideApp
import com.app.groupis.R
import com.app.groupis.models.User
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import de.hdodenhof.circleimageview.CircleImageView
import java.io.ByteArrayOutputStream

class ProfileActivity : AppCompatActivity() {

    private lateinit var storageRef: StorageReference
    private lateinit var profileName: TextView
    private lateinit var editUsername: RelativeLayout
    private lateinit var profileImage: ImageView
    private lateinit var user: User
    private lateinit var myPrefs: SharedPreferences
    private lateinit var changeProfileImageFab: FloatingActionButton
    private lateinit var profileImageCircle: CircleImageView
    private lateinit var imageRef: StorageReference
    private val TAG = "ProfileActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        val viewModel: UserProfileViewModel by viewModels()
        myPrefs = getSharedPreferences("prefs", MODE_PRIVATE)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        profileName = findViewById(R.id.profile_name_text)
        profileName.text = intent.getStringExtra("username")
        changeProfileImageFab = findViewById(R.id.profile_activity_camera_button)
        profileImageCircle = findViewById(R.id.profile_activity_profile_picture)


        user = intent.getSerializableExtra("user") as User
        user.getNameId()?.let { Log.e(TAG, it) }
        user.getEmail()?.let { Log.e(TAG, it) }
        user.getUid()?.let { Log.e(TAG, it) }

        changeProfileImageFab.setOnClickListener {
            CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .setCropShape(CropImageView.CropShape.OVAL)
                .setFixAspectRatio(true)
                .setAspectRatio(1, 1)
                .start(this)
        }

        if (profileName.text == null) {
            viewModel.retrieveUserr()
        }


        storageRef = FirebaseStorage.getInstance().reference.child("/users")

        editUsername = findViewById(R.id.edit_username)
        profileImage = findViewById(R.id.profile_image_profile)
        editUsername.setOnClickListener {
            changeUsernameDialog()
        }

        changeUsernameResult(viewModel)
        loadProfilePicture()

    }

    private fun loadProfilePicture() {
        FirebaseDatabase.getInstance().reference.child("Users")
            .child(FirebaseAuth.getInstance().currentUser!!.uid)
            .orderByChild("hasPicture")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val user = snapshot.getValue(User::class.java)
                    if (user!!.getPictureRef() != null) {
                        GlideApp.with(this@ProfileActivity)
                            .load(storageRef.child(user.getPictureRef()!! + ".jpg"))
                            .into(profileImageCircle)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                }
            })
    }

    private fun changeUsernameResult(viewModel: UserProfileViewModel) {
        viewModel.isValid.observe(this, Observer { valid ->
            when (valid) {
                "NO EXISTE" -> {
                    profileName.text = viewModel.getUsername().value
                    myPrefs.edit().putString(
                        FirebaseAuth.getInstance().currentUser!!.uid,
                        viewModel.getUsername().value
                    ).apply()
                    Toast.makeText(
                        this,
                        "El nombre de usuario se ha cambiado correctamente.",
                        Toast.LENGTH_LONG
                    ).show()
                }
                "EXISTE" -> {
                    Toast.makeText(this, "Ya hay un usuario con ese nombre UwU", Toast.LENGTH_LONG)
                        .show()
                }
                "LARGO INVALIDO" -> {
                    Toast.makeText(
                        this,
                        "El nombre de usuario debe tener entre 4 y 16 caracteres.",
                        Toast.LENGTH_LONG
                    ).show()
                }
                "ESPACIO INVALIDO" -> {
                    Toast.makeText(
                        this,
                        "El nombre de usuario debe tener entre 4 y 16 caracteres.",
                        Toast.LENGTH_LONG
                    ).show()
                }

            }
        })
    }

    private fun changeUsernameDialog() {
        val newFragment = UsernameDialog(profileName.text.toString())
        newFragment.show(supportFragmentManager, "NoticeDialogFragment")
    }

    override fun onResume() {
        super.onResume()
        if (this.resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
            profileImage.scaleX = 1.0F
            profileImage.scaleY = 1.0F
        } else if (this.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            profileImage.scaleX = 0.5F
            profileImage.scaleY = 0.5F
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            val result = CropImage.getActivityResult(data)
            if (resultCode == RESULT_OK) {
                val key = FirebaseDatabase.getInstance().reference.push().key!!
                if (user.getPictureRef() != null) {
                    val oldImageRef = storageRef.child(user.getPictureRef()!! + ".jpg")
                    oldImageRef.delete()
                }

                imageRef = storageRef.child("$key.jpg")
                val (imageUri, uploadTask) = saveImageToStorage(result)

                uploadTask.addOnSuccessListener {
                    Log.i("IMAGE UPLOAD", "Image saved to storage successfully")
                    GlideApp.with(this@ProfileActivity)
                        .load(imageRef)
                        .into(profileImageCircle)

                    val intent = Intent()
                    intent.putExtra("image", imageUri.toString())
                    setResult(123, intent)
                    user.setPictureRef(key)
                    saveImageReferenceToDB(key)
                }.addOnFailureListener {
                    Log.i("IMAGE UPLOAD", "Error saving image to storage. ERROR:" + it.message)
                }
            }
        }
    }

    private fun saveImageReferenceToDB(key: String) {
        val hashMap = HashMap<String, Any>()
        hashMap["pictureRef"] = key
        FirebaseDatabase.getInstance().reference.child("Users").child(
            FirebaseAuth.getInstance().currentUser!!.uid
        ).updateChildren(hashMap)
    }

    private fun saveImageToStorage(result: CropImage.ActivityResult): Pair<Uri, UploadTask> {
        val imageUri = result.uri
        val baos = ByteArrayOutputStream()
        val bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, imageUri).scale(
            500,
            500
        )
//        val croppedBitmap = getCroppedBitmap(bitmap)!!.scale(500,500)
        bitmap!!.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val dataA = baos.toByteArray()
        val uploadTask = imageRef.putBytes(dataA)
        return Pair(imageUri, uploadTask)
    }

    override fun onStop() {
        Instrumentation().callActivityOnSaveInstanceState(this, Bundle())
        super.onStop()
    }

//    fun getCroppedBitmap(bitmap: Bitmap): Bitmap? {
//        val output = Bitmap.createBitmap(
//            bitmap.width,
//            bitmap.height, Bitmap.Config.ARGB_8888
//        )
//        val canvas = Canvas(output)
//        val color = -0xbdbdbe
//        val paint = Paint()
//        val rect = Rect(0, 0, bitmap.width, bitmap.height)
//        paint.isAntiAlias = true
//        canvas.drawARGB(0, 0, 0, 0)
//        paint.color = color
//        // canvas.drawRoundRect(rectF, roundPx, roundPx, paint);
//        canvas.drawCircle(
//            (bitmap.width / 2).toFloat(), (bitmap.height / 2).toFloat(),
//            (bitmap.width / 2).toFloat(), paint
//        )
//        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
//        canvas.drawBitmap(bitmap, rect, rect, paint)
//        //Bitmap _bmp = Bitmap.createScaledBitmap(output, 60, 60, false);
//        //return _bmp;
//        return output
//    }

}