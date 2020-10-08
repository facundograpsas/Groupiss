package com.app.groupis.activities.main

import android.annotation.SuppressLint
import android.app.ActivityOptions
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.Observer
import androidx.viewpager.widget.ViewPager
import com.app.groupis.GlideApp
import com.app.groupis.R
import com.app.groupis.activities.main.adapters.SectionsPagerAdapter
import com.app.groupis.activities.newgroup.NewGroupActivity
import com.app.groupis.activities.profile.ProfileActivity
import com.app.groupis.activities.signIn.SignInActivity
import com.app.groupis.activities.username.SetUsernameActivity
import com.app.groupis.models.User
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.tabs.TabLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import de.hdodenhof.circleimageview.CircleImageView
import android.util.Pair as UtilPair


var userProfileImageRef: StorageReference? = null

const val TOPIC = "/topics/"

class MainActivity : AppCompatActivity() {


    private lateinit var fab: FloatingActionButton
    private lateinit var fabAddPublic: FloatingActionButton
    private lateinit var fabAddPrivate: FloatingActionButton
    private var isFabClicked = false
    private lateinit var goToProfile: RelativeLayout
    private lateinit var profileName: TextView
    private lateinit var profileImage: CircleImageView
    private lateinit var user: User
    private lateinit var myPrefs: SharedPreferences
    private lateinit var viewPager: ViewPager

    val TAG = "MainActivity"


    @SuppressLint("ResourceType")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val sectionsPagerAdapter = SectionsPagerAdapter(this, supportFragmentManager)
        viewPager = findViewById(R.id.view_pager)
        viewPager.adapter = sectionsPagerAdapter
        val tabs: TabLayout = findViewById(R.id.tabs)
        tabs.setupWithViewPager(viewPager)
        val toolbar : Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        fab = findViewById(R.id.fab_add)
        goToProfile = findViewById<RelativeLayout>(R.id.go_to_profile)
        fabAddPublic = findViewById(R.id.fab_add_public)
        fabAddPrivate = findViewById(R.id.fab_add_private)
        profileName = findViewById(R.id.main_activity_profile_name)
        profileImage = findViewById(R.id.profile_image)
        myPrefs = getSharedPreferences("prefs", MODE_PRIVATE)

        val viewModel: UserViewModel by viewModels()

        val storageRef = FirebaseStorage.getInstance().reference.child("/users")


        if (FirebaseAuth.getInstance().currentUser != null) {
            viewModel.retrieveUser(object : RetrieveUserCallback {
                override fun onCallback(userP: User) {
                    user = userP
                    if (user.getPictureRef() != null) {
                        GlideApp.with(this@MainActivity)
                            .load(storageRef.child(user.getPictureRef()!! + ".jpg"))
                            .into(profileImage)
                    }
                }
            })
        }


        retrieveUser(viewModel)
        fabOnClick()
        onProfileClick()
        redirectNewUser()

        onAddPublicGroupClick(viewModel)

        Log.e(TAG, "CREATED VIEW")

    }

    private fun onAddPublicGroupClick(viewModel: UserViewModel) {

        fabAddPublic.setOnClickListener {
            fabAddPublic.isClickable = false
            val options = ActivityOptions.makeSceneTransitionAnimation(this)
            val intent = Intent(this@MainActivity, NewGroupActivity::class.java)
                .apply { putExtra("user", viewModel.getUser().value) }
            startActivity(intent, options.toBundle())
        }

        viewModel.user.observe(this, Observer { user ->
            profileName.text = user.getNameId()
        })
    }

    private fun retrieveUser(viewModel: UserViewModel) {
        if (FirebaseAuth.getInstance().currentUser != null) {
            viewModel.retrieveUser()
        }
    }

    private fun onProfileClick() {
        goToProfile.setOnClickListener {
            goToProfile.isClickable = false
            var options = ActivityOptions.makeSceneTransitionAnimation(
                this,
                UtilPair.create(profileImage as View, "profile_image_transition"),
                UtilPair.create(profileName as View, "profile_name_transition")
            )
            startActivityForResult(
                Intent(
                    this@MainActivity,
                    ProfileActivity::class.java
                ).putExtra("username", profileName.text).putExtra("user", user),
                123,
                options.toBundle()
            )
        }

    }

    private fun fabOnClick() {
        fab.setOnClickListener {
            if (!isFabClicked) {
                fab.animate().rotation(50F)
                fabAddPublic.animate().translationY(-200F).setDuration(500).alpha(1F)
                fabAddPrivate.animate().translationY(-400F).setDuration(500).alpha(1F).withEndAction {
                    isFabClicked = true
                    fab.isClickable = true
                    fabAddPublic.isClickable = true
                }.withStartAction { fab.isClickable = false
                                    fabAddPublic.isClickable = false}
            } else {
                fab.animate().rotation(0F)
                fabAddPublic.animate().translationY(0F).setDuration(500).alpha(0F)
                fabAddPrivate.animate().translationY(0F).setDuration(500).alpha(0F).withEndAction {
                    isFabClicked = false
                    fab.isClickable = true
                    fabAddPublic.isClickable = true
                }.withStartAction { fab.isClickable = false
                                    fabAddPublic.isClickable = false}
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.menu_logout -> {
                logout()
            }
            R.id.menu_settings -> println("Settings")
        }
        return true
    }

    private fun logout() {
        GoogleSignIn.getClient(this, GoogleSignInOptions.DEFAULT_SIGN_IN).signOut()
        FirebaseAuth.getInstance().signOut()
        finish()
        startActivity(Intent(this@MainActivity, SignInActivity::class.java))
    }

    private fun redirectNewUser(){
        if (FirebaseAuth.getInstance().currentUser == null) {
            startActivity(Intent(this@MainActivity, SignInActivity::class.java))
            finish()
        } else if (FirebaseAuth.getInstance().currentUser != null) {
            val refUser = FirebaseDatabase.getInstance().reference.child("Users")
                .child(FirebaseAuth.getInstance().currentUser!!.uid).child("nameId")
            refUser.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    Log.e(TAG, snapshot.children.toString())
                    Log.e(TAG, snapshot.value.toString())
                    Log.e(TAG, snapshot.exists().toString())


                    if (!snapshot.exists()) {
                        Log.e(TAG, "No ExistSxd")
                        startActivity(Intent(this@MainActivity, SetUsernameActivity::class.java))
                        finish()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                }
            })
        }
    }

    override fun onResume() {
        goToProfile.isClickable = true
        fabAddPublic.isClickable = true
        super.onResume()
    }


    override fun onBackPressed() {
        if (viewPager.currentItem == 0) {
            super.onBackPressed()
        } else {
            viewPager.setCurrentItem(0, true)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 123) {
            if (resultCode == 123) {
                val uriString = data!!.getStringExtra("image")
                if (uriString != null) {
                    val uri = Uri.parse(uriString)
//                    profileImage.setImageURI(uri)
                    GlideApp.with(this@MainActivity)
                        .load(uri)
                        .into(profileImage)
                }
            }
        }
    }

}
