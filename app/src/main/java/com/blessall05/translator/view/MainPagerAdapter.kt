package com.blessall05.translator.view

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.blessall05.translator.view.dictionary.DictionaryFragment
import com.blessall05.translator.view.home.HomeFragment
import com.blessall05.translator.view.translate.TranslateFragment

class MainPagerAdapter(fragmentManager: FragmentManager, lifecycle: Lifecycle) :
    FragmentStateAdapter(fragmentManager, lifecycle) {
    override fun getItemCount() = 3
    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> HomeFragment()
            1 -> DictionaryFragment()
            2 -> TranslateFragment()
            else -> HomeFragment()
        }
    }
}