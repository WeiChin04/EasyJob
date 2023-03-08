package com.example.easyjob.user

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.example.easyjob.databinding.FragmentUserMyJobBinding

class UserMyJob : Fragment() {

    private var _binding: FragmentUserMyJobBinding? = null
    private val binding get() = _binding!!


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val tabs = binding.tabs
        val viewPager = binding.viewPager

        val pagerAdapter = MyJobPagerAdapter(childFragmentManager)
        pagerAdapter.addFragment(AppliedJobFragment(), "Applied Job")
        pagerAdapter.addFragment(FavouriteJobFragment(), "Favourite Job")
        viewPager.adapter = pagerAdapter

        tabs.setupWithViewPager(viewPager)

    }

    private class MyJobPagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT){

        private val fragmentList = mutableListOf<Fragment>()
        private val fragmentTitleList = mutableListOf<String>()

        override fun getCount(): Int {
            return fragmentList.size
        }

        override fun getItem(position: Int): Fragment {
            return fragmentList[position]
        }

        override fun getPageTitle(position: Int): CharSequence {
            return fragmentTitleList[position]
        }

        fun addFragment(fragment: Fragment, title: String) {
            fragmentList.add(fragment)
            fragmentTitleList.add(title)
        }

    }


}