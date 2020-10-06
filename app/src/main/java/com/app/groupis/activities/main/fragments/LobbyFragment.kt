package com.app.groupis.activities.main.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.groupis.R
import com.app.groupis.activities.main.GroupViewModel
import com.app.groupis.activities.main.UserViewModel
import com.app.groupis.activities.main.adapters.GroupAdapter
import com.app.groupis.models.Group


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
        val groupViewModel : GroupViewModel by activityViewModels()
        mContext = requireActivity()
        val view = inflater.inflate(R.layout.fragment_lobby, container, false)
        groups = arrayListOf<Group>()
        recyclerGroupList = view.findViewById(R.id.group_lobby_recycler_view)
        recyclerGroupList.setHasFixedSize(true)
        recyclerGroupList.layoutManager = LinearLayoutManager(context)

        createPublicGroupResult()

        viewModel.userLoaded.observe(viewLifecycleOwner, Observer {
            retrievePublicGroups(groupViewModel, viewModel)
        })

        return view
    }

    private fun retrievePublicGroups(groupViewModel: GroupViewModel, userViewModel: UserViewModel){
        groupViewModel.getPublicGroups(groups, userViewModel, mContext, recyclerGroupList, groupViewModel)
    }

    private fun createPublicGroupResult() {
        val viewModel = GroupViewModel()
        viewModel.newGroupState.observe(viewLifecycleOwner, Observer { result ->
            when (result) {
                "LARGO INVALIDO" -> Toast.makeText(context, "El nombre del grupo debe tener entre 4 y 16 caracteres.", Toast.LENGTH_LONG).show()
                "EXISTE" -> Toast.makeText(context, "Ya hay un grupo con ese nombre", Toast.LENGTH_LONG).show()
                "NO EXISTE" -> Toast.makeText(context, "Grupo creado exitosamente", Toast.LENGTH_LONG).show()
            }
        })
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