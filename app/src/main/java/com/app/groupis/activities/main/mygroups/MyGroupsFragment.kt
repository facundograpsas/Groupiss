package com.app.groupis.activities.main.mygroups

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.groupis.R
import com.app.groupis.activities.main.UserViewModel
import com.app.groupis.models.Group

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class MyGroupsFragment : Fragment() {
    private var param1: String? = "Capo"
    private var param2: String? = "Capin"

    private lateinit var recyclerView : RecyclerView
    private lateinit var myGroups : ArrayList<Group>
    private lateinit var mContext : Context
    private lateinit var viewModel : UserViewModel
    private lateinit var myGroupViewModel: MyGroupViewModel

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
        myGroupViewModel = ViewModelProvider(requireActivity()).get(MyGroupViewModel::class.java)
        recyclerView = view.findViewById(R.id.group_mine_recycler_view)
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(context)
        myGroups = arrayListOf()

//        viewModel.userLoaded.observe(viewLifecycleOwner, Observer {
            retrieveMyGroups(viewModel, myGroupViewModel)
//        })

        return view
    }

    private fun retrieveMyGroups(userViewModel: UserViewModel, myGroupViewModel: MyGroupViewModel){
        myGroupViewModel.getMyGroupss(
            myGroups,
            userViewModel,
            mContext,
            recyclerView,
            myGroupViewModel
        )
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