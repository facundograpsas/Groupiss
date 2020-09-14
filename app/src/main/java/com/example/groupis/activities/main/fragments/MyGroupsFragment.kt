package com.example.groupis.activities.main.fragments

import android.content.Context
import android.os.Bundle
import android.renderscript.Sampler
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.groupis.R
import com.example.groupis.activities.main.GroupViewModel
import com.example.groupis.activities.main.UserViewModel
import com.example.groupis.activities.main.adapters.GroupAdapter
import com.example.groupis.activities.main.adapters.MyGroupAdapter
import com.example.groupis.models.Group
import com.example.groupis.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class MyGroupsFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = "Capo"
    private var param2: String? = "Capin"

    private lateinit var recyclerView : RecyclerView
    private lateinit var groupAdapter : MyGroupAdapter
    private lateinit var myGroups : ArrayList<Group>
    private lateinit var mContext : Context


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        mContext = requireActivity().applicationContext
        val view = inflater.inflate(R.layout.fragment_my_groups, container, false)
        val viewModel : UserViewModel by activityViewModels()

        val groupViewModel : GroupViewModel by activityViewModels()

        recyclerView = view.findViewById(R.id.group_mine_recycler_view)
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(context)
        myGroups = arrayListOf()

        viewModel.userLoaded.observe(viewLifecycleOwner, Observer {
            retrieveMyGroups(viewModel, groupViewModel)
        })

        return view
    }

//    private fun retrieveMyGroups(viewModel : UserViewModel, groupViewModel: GroupViewModel) {
//        FirebaseDatabase.getInstance().reference.child("Groups").addValueEventListener(object : ValueEventListener{
//            override fun onDataChange(snapshot: DataSnapshot) {
//                myGroups.clear()
//                for(p0 in snapshot.children) {
//                    val userList = p0.child("users")
//                    for(user in userList.children){
//                        if(user.key==FirebaseAuth.getInstance().currentUser!!.uid){
//                            val group = p0.getValue(Group::class.java)
//                            group!!.setUserSize(userList.childrenCount.toInt())
//                            myGroups.add(group)
//                        }
//                    }
//                }
//                if(viewModel.getUser().value!=null) {
//                    println("User: "+viewModel.getUser().value)
//                    println(context)
//                    println(myGroups)
//                    groupAdapter = MyGroupAdapter(mContext, myGroups, viewModel.getUser().value!!, groupViewModel)
//                    recyclerView.adapter = groupAdapter
//                }
//            }
//
//            override fun onCancelled(error: DatabaseError) {
//                TODO("Not yet implemented")
//            }
//
//        })
//    }


    private fun retrieveMyGroups(userViewModel: UserViewModel, groupViewModel: GroupViewModel){
        groupViewModel.getMyGroups(myGroups, userViewModel, mContext, recyclerView, groupViewModel)
    }

    private fun getLastMessage(){
        FirebaseDatabase.getInstance().reference.child("Groups")
    }

    companion object {

        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            MyGroupsFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}