package com.app.groupis.activities.main.lobby

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.recyclerview.widget.RecyclerView
import com.app.groupis.activities.chat.ChatActivity
import com.app.groupis.activities.main.TOPIC
import com.app.groupis.activities.main.UserViewModel
import com.app.groupis.models.Group
import com.app.groupis.models.User
import com.google.firebase.database.*
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.ByteArrayOutputStream
import kotlin.random.Random

private val storageReference: StorageReference
    get() {
        val storageRef = FirebaseStorage.getInstance().reference.child("/images")
        return storageRef.child(System.currentTimeMillis().toString() + ".jpg")
    }

class GroupViewModel : ViewModel() {

    val TAG = "GroupViewModel"

    val newGroupState = MutableLiveData<String>()
    private val fill = MutableLiveData<Boolean>()
    private val lastMessage = MutableLiveData<String>()
    private val newGroupName = MutableLiveData<String>()
    private val newGroupId = MutableLiveData<Int>()
    private val newGroup = MutableLiveData<Group>()

    private val groupRepository = GroupRepository()

    private fun setNewGroup(group: Group) {
        newGroup.value = group
    }

    private fun getNewGroup(): Group {
        return newGroup.value!!
    }

    fun setNewGroupName(groupName: String) {
        this.newGroupName.value = groupName
    }

    fun getNewGroupName(): String? {
        return newGroupName.value
    }

    private fun setNewGroupId(newGroupId: Int) {
        this.newGroupId.value = newGroupId
    }

    fun getNewGroupId(): Int {
        return newGroupId.value!!
    }

    fun setGroupState(value: String) {
        this.newGroupState.value = value
    }

    fun setFill(fill: Boolean) {
        this.fill.value = fill
    }


    fun getPublicGroups(
        groups: ArrayList<Group>,
        userViewModel: UserViewModel,
        mContext: Context,
        recyclerGroupList: RecyclerView,
        groupViewModel: GroupViewModel
    ) {
        lateinit var lobbyGroupAdapter: LobbyGroupAdapter
        groupRepository.getAllGroups(object : Callback {
            override fun onSuccess(snapshot: DataSnapshot) {
                groups.clear()
                for (p0 in snapshot.children) {
                    val userList = p0.child("users")
                    val group = p0.getValue(Group::class.java)
                    group!!.setUserSize(userList.childrenCount.toInt())
                    groups.add(group)
                }
                if (userViewModel.getUser().value != null) {
                    lobbyGroupAdapter =
                        LobbyGroupAdapter(
                            mContext,
                            groups,
                            userViewModel.getUser().value!!,
                            groupViewModel
                        )
                    recyclerGroupList.adapter = lobbyGroupAdapter
                }
            }

            override fun onFailure(message: String) {
                Log.e(TAG, message)
            }

        })
    }

    fun validGroup(groupName: String, user: User, bitmap: Bitmap?, callback: NewGroupCallback) {
        if (groupName.length < 4 || groupName.length > 20) {
            callback.onCallback("invalid length")
        } else {
            groupRepository.getGroup(groupName, object : Callback {
                override fun onSuccess(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        callback.onCallback("invalid")
                    } else {
                        createGroup(groupName, user, bitmap)
                        val topic = TOPIC + groupName.replace(" ", "f")
                        FirebaseMessaging.getInstance().subscribeToTopic(topic)
                        callback.onCallback("valid")
                    }
                }

                override fun onFailure(message: String) {
                    Log.e(TAG, message)
                }
            })
        }
    }

    private fun createGroup(groupTitle: String, user: User, bitmap: Bitmap?) {
        val imageKey = FirebaseDatabase.getInstance().reference.push().key
        val group = Group()
        group.setTitle(groupTitle)
        if (bitmap != null) {
            saveImageGroup(bitmap, imageKey!!)
            group.setPicture("$imageKey.jpg")
        }
        group.setTotalMessages(0)
        group.setColor(Random.nextInt(1, 6))
        val groupId = Random.nextInt()
        group.setGroupId(groupId)
        setNewGroupId(groupId)
        setNewGroup(group)
        groupRepository.saveNewGroup(group, user)
    }

    private fun saveImageGroup(bitmap: Bitmap?, imageKey: String) {
        val baos = ByteArrayOutputStream()
        bitmap!!.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val data = baos.toByteArray()
        groupRepository.saveGroupImage(data, imageKey)
    }


    fun onPublicGroupClick(mContext: Context, user: User, userUID: String, group: Group) {
        groupRepository.getCurrentUserInGroup(group.getTitle(), object : Callback {
            override fun onSuccess(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    goToClickedGroup(mContext, group, user)
                } else {
                    AlertDialog.Builder(mContext)
                        .setTitle("Deseas unirte a el grupo \"${group.getTitle()}\"?")
                        .setPositiveButton("Unirse al grupo") { _, _ ->
                            saveUserToGroup(user, group.getTitle())
                            val topic = TOPIC + group.getTitle().replace(" ", "f")
                            Log.e(TAG, topic)
                            FirebaseMessaging.getInstance().subscribeToTopic(topic)
                                .addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        Log.e(TAG, "SUCCESS")
                                    } else {
                                        Log.e(TAG, "ERROR")
                                    }
                                }
                            Toast.makeText(
                                mContext,
                                "Te has unido a \"${group.getTitle()}\"",
                                Toast.LENGTH_LONG
                            ).show()
                            goToClickedGroup(mContext, group, user)
                        }.setNegativeButton("Cancelar") { dialog, _ ->
                            dialog.cancel()
                        }.show()
                }
            }

            override fun onFailure(message: String) {
                Log.e(TAG, message)
            }

        })
    }


    private fun saveUserToGroup(user: User, groupTitle: String) {
        groupRepository.saveCurrentUserInGroup(user, groupTitle)
    }

    fun goToNewGroup(mContext: Context, user: User) {
        val intent = Intent(mContext, ChatActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.putExtra("group", getNewGroup())
        intent.putExtra("username", user.getNameId())
        mContext.startActivity(intent)

    }

    private fun goToClickedGroup(mContext: Context, group: Group, user: User) {
        val intent = Intent(mContext, ChatActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.putExtra("groupName", group.getTitle())
        intent.putExtra("username", user.getNameId())
        intent.putExtra("group", group)
        mContext.startActivity(intent)
    }
}

interface NewGroupCallback {
    fun onCallback(value: String)
}
