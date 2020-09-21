package com.example.groupis.activities.main.fragments

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.groupis.R
import com.example.groupis.activities.main.GroupViewModel
import com.example.groupis.activities.main.MainActivity
import com.example.groupis.activities.main.UserViewModel
import com.example.groupis.models.Group
import com.example.groupis.models.User

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class MyGroupsFragment : Fragment() {
    private var param1: String? = "Capo"
    private var param2: String? = "Capin"

    private lateinit var recyclerView : RecyclerView
    private lateinit var myGroups : ArrayList<Group>
    private lateinit var mContext : Context
    private lateinit var viewModel : UserViewModel
    private lateinit var groupViewModel: GroupViewModel

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
        viewModel = ViewModelProvider(requireActivity()).get(UserViewModel::class.java)
        groupViewModel = ViewModelProvider(requireActivity()).get(GroupViewModel::class.java)

        recyclerView = view.findViewById(R.id.group_mine_recycler_view)
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(context)
        myGroups = arrayListOf()

        viewModel.userLoaded.observe(viewLifecycleOwner, Observer {
            retrieveMyGroups(viewModel, groupViewModel)
        })

//        if(myGroups.size>0){
//            groupViewModel.refreshGroups(myGroups, viewModel, mContext, recyclerView, groupViewModel)
//        }

//        groupViewModel.sortMyGroupsByLastMessage(myGroups)


        return view
    }

    private fun retrieveMyGroups(userViewModel: UserViewModel, groupViewModel: GroupViewModel){
        groupViewModel.getMyGroups(myGroups, userViewModel, mContext, recyclerView, groupViewModel)
    }

    companion object {

        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            MyGroupsFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)


//                    if(view!=null){
//                        viewModel.userLoaded.observe(viewLifecycleOwner, Observer {
//                            retrieveMyGroups(viewModel, groupViewModel)
//                        })
//                    }

                }
            }
    }

//    override fun onResume() {
//        super.onResume()
//        retrieveMyGroups(viewModel, groupViewModel)
//    }
}