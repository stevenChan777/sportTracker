package com.example.myruns5

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import java.util.ArrayList

// Adapter for managing a collection of fragments in a ViewPager2 setup.
class MyFragmentStateAdapter(activity: FragmentActivity, var list: ArrayList<Fragment>)
    : FragmentStateAdapter(activity){

    // Creates and returns the fragment associated with the specified position.
    override fun createFragment(position: Int): Fragment {
        return list[position]
    }

    // Returns the total number of fragments in the list.
    override fun getItemCount(): Int {
        return list.size
    }
}