package com.app.groupis.activities.main

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.app.groupis.R
import com.app.groupis.activities.main.lobby.LobbyFragment
import com.app.groupis.activities.main.mygroups.MyGroupsFragment

private val TAB_TITLES = arrayOf(
        R.string.my_groups,
        R.string.lobby
)

class SectionsPagerAdapter(private val context: Context, fm: FragmentManager)
    : FragmentPagerAdapter(fm) {

    override fun getItem(position: Int): Fragment {

        return when(position){
            0 -> MyGroupsFragment.newInstance("1","2")
            1 -> LobbyFragment.newInstance("1", "2")

            else -> MyGroupsFragment.newInstance("1","2")
        }
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return context.resources.getString(TAB_TITLES[position])
    }

    override fun getCount(): Int {
        return 2
    }
}