package com.example.guardiansafetyapp.onboarding

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewpager2.widget.ViewPager2
import com.example.guardiansafetyapp.R
import com.example.guardiansafetyapp.ViewPagerAdapter
import com.example.guardiansafetyapp.onboarding.screens.FirstScreenFragment
import com.example.guardiansafetyapp.onboarding.screens.SecondScreenFragment
import com.example.guardiansafetyapp.onboarding.screens.ThirdScreenFragment

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER

class ViewPagerFragment : Fragment() {


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view= inflater.inflate(R.layout.fragment_view_pager, container, false)

        val fragmentList= arrayListOf<Fragment>(
            FirstScreenFragment(),
            SecondScreenFragment(),
            ThirdScreenFragment()
        )

        val adapter=ViewPagerAdapter(fragmentList,requireActivity().supportFragmentManager,lifecycle)
        view.findViewById<ViewPager2>(R.id.viewPager).adapter=adapter

        return view
    }


}