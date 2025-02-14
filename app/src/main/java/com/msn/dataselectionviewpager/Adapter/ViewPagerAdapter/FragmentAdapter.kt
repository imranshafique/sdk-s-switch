package com.msn.dataselectionviewpager.Adapter.ViewPagerAdapter

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
 import com.msn.dataselectionviewpager.Fragments.AppsFragment
import com.msn.dataselectionviewpager.Fragments.AudiosFragment
import com.msn.dataselectionviewpager.Fragments.ContactFragment
import com.msn.dataselectionviewpager.Fragments.DocumentsFragment
import com.msn.dataselectionviewpager.Fragments.ImagesFragment
import com.msn.dataselectionviewpager.Fragments.VideosFragment

class FragmentAdapter(activity: AppCompatActivity) : FragmentStateAdapter(activity) {

    override fun getItemCount(): Int = 6  // One fragment for each category

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> ImagesFragment()
            1 -> VideosFragment()
            2 -> AudiosFragment()
            3 -> DocumentsFragment()
            4 -> ContactFragment()
            5 -> AppsFragment()
            else -> throw IllegalStateException("Unexpected position: $position")
        }
    }
}
