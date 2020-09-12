package com.example.groupis.activities.main.fragments

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.forEach
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.groupis.R
import com.example.groupis.activities.main.GroupViewModel
import com.example.groupis.activities.main.UserViewModel
import com.example.groupis.activities.main.adapters.GroupAdapter
import com.example.groupis.models.Group
import com.example.groupis.models.User
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class LobbyFragment : Fragment() {
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var recyclerGroupList : RecyclerView
    private lateinit var groupAdapter : GroupAdapter
    private lateinit var groups : ArrayList<Group>
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

        val viewModel : UserViewModel by activityViewModels()

        mContext = requireActivity()


        val view = inflater.inflate(R.layout.fragment_lobby, container, false)

        var users = arrayListOf<User>()
//        users.add(User("Pepe", "asd","ASD","asd"))
//        users.add(User("Pepe", "asd","ASD","asd"))
//        users.add(User("Pepe", "asd","ASD","asd"))


//        var group1 = Group(users,"grupo 1",null)
//        var group2 = Group(users,"grupo 2",null)

          groups = arrayListOf<Group>()
//        groups.add(group1)
//        groups.add(group2)

        recyclerGroupList = view.findViewById(R.id.group_lobby_recycler_view)
        recyclerGroupList.setHasFixedSize(true)
        recyclerGroupList.layoutManager = LinearLayoutManager(context)


        createPublicGroupResult()

        viewModel.userLoaded.observe(viewLifecycleOwner, Observer {
            retrievePublicGroups(viewModel)
        })

        // Inflate the layout for this fragment
        return view
    }

    private fun retrievePublicGroups(viewModel: UserViewModel) {
        val dbRef = FirebaseDatabase.getInstance().reference.child("Groups").addValueEventListener(
            object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    Log.d("DEBUG", "EJECUTATRES")
                    groups.clear()
                    for (p0 in snapshot.children) {
                        val userList = p0.child("users")
                        val group = p0.getValue(Group::class.java)
                        group!!.setUserSize(userList.childrenCount.toInt())
                        groups.add(group!!)
                        Log.d("DEBUG", "IN FOR")
                    }
                    Log.d("DEBUG", "POST FOR")
                    if (viewModel.getUser().value != null) {
                        println("User: " + viewModel.getUser().value)
                        println(context)
                        println(mContext)
                        println(groups)
                        groupAdapter = GroupAdapter(mContext, groups, viewModel.getUser().value!!)
                        recyclerGroupList.adapter = groupAdapter
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    println(error.message)
                }

            })

    }

    private fun createPublicGroupResult() {
        val viewModel = GroupViewModel()
        viewModel.newGroupState.observe(viewLifecycleOwner, Observer { result ->
            when (result) {
                "LARGO INVALIDO" -> Toast.makeText(
                    context,
                    "El nombre del grupo debe tener entre 4 y 16 caracteres.",
                    Toast.LENGTH_LONG
                ).show()
                "EXISTE" -> Toast.makeText(
                    context,
                    "Ya hay un grupo con ese nombre",
                    Toast.LENGTH_LONG
                ).show()
                "NO EXISTE" -> Toast.makeText(
                    context,
                    "Grupo creado exitosamente",
                    Toast.LENGTH_LONG
                ).show()
            }
        })
    }


    override fun onResume() {
        super.onResume()


    }

    companion object {

        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            LobbyFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}