package com.example.groupis.activities.main

import android.app.ActivityOptions
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.viewpager.widget.ViewPager
import com.example.groupis.activities.profile.ProfileActivity
import com.example.groupis.R
import com.example.groupis.activities.main.adapters.SectionsPagerAdapter
import com.example.groupis.activities.main.fragments.NewPublicGroupDialog
import com.example.groupis.activities.profile.UsernameDialog
import com.example.groupis.activities.signIn.SignInActivity
import com.example.groupis.activities.username.SetUsernameActivity
import com.example.groupis.models.User
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.tabs.TabLayout
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import android.util.Pair as UtilPair


class MainActivity : AppCompatActivity() {


    private lateinit var fab : FloatingActionButton
    private lateinit var fabAddPublic : FloatingActionButton
    private lateinit var fabAddPrivate : FloatingActionButton
    private var isFabClicked = false
    private lateinit var goToProfile : RelativeLayout
    private lateinit var profileName : TextView
    private lateinit var profileImage : ImageView
    private lateinit var user : User
    private lateinit var myPrefs : SharedPreferences


    override fun onCreate(savedInstanceState: Bundle?) {


        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val sectionsPagerAdapter = SectionsPagerAdapter(this, supportFragmentManager)
        val viewPager: ViewPager = findViewById(R.id.view_pager)
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


        val viewModel : UserViewModel by viewModels()

        retrieveUser(viewModel)
        fabOnClick()
        onProfileClick()
        redirectNewUser()

        fabAddPublic.setOnClickListener {
            val newFragment = NewPublicGroupDialog()
            newFragment.show(supportFragmentManager, "New Public Group Dialog")
        }

    }

    private fun retrieveUser(viewModel: UserViewModel) {
        if (FirebaseAuth.getInstance().currentUser != null) {
            val ref = FirebaseDatabase.getInstance().reference.child("Users")
                .child(FirebaseAuth.getInstance().currentUser!!.uid)
            ref.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        user = snapshot.getValue(User::class.java)!!
                        viewModel.setUser(user)
                        viewModel.setUserLoaded(true)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                }
            })
            viewModel.user.observe(this, Observer { user ->
                profileName.text = user.getUsername()
            })
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
            startActivity(
                Intent(
                    this@MainActivity,
                    ProfileActivity::class.java
                ).putExtra("username", profileName.text), options.toBundle()
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
                }.withStartAction { fab.isClickable = false }
            } else {
                fab.animate().rotation(0F)
                fabAddPublic.animate().translationY(0F).setDuration(500).alpha(0F)
                fabAddPrivate.animate().translationY(0F).setDuration(500).alpha(0F).withEndAction {
                    isFabClicked = false
                    fab.isClickable = true
                }.withStartAction { fab.isClickable = false }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.menu_logout -> logout()
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
        if(FirebaseAuth.getInstance().currentUser==null){
            startActivity(Intent(this@MainActivity, SignInActivity::class.java))
            finish()
        }
        else if(FirebaseAuth.getInstance().currentUser!=null){
            val username = myPrefs.getString(FirebaseAuth.getInstance().currentUser!!.uid, null)
            if (username==null){
                startActivity(Intent(this@MainActivity, SetUsernameActivity::class.java))
                finish()
            }
            else{
                profileName.text = username
            }
        }
    }

    override fun onResume() {
        super.onResume()
        goToProfile.isClickable = true
    }
}