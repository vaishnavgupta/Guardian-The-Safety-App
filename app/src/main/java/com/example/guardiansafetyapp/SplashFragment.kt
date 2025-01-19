package com.example.guardiansafetyapp

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController


class SplashFragment : Fragment() {


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        Handler().postDelayed({
            if(checkOnBoardingStatus()){
                val i=Intent(activity,RegistrationsActivity::class.java)
                startActivity(i)
                requireActivity().finish()
            }
            else{
                findNavController().navigate(R.id.action_splashFragment_to_viewPagerFragment)
            }
        },3000)
        return inflater.inflate(R.layout.fragment_splash, container, false)
    }

    private fun checkOnBoardingStatus():Boolean{
        val sharedPrefs=requireActivity().getSharedPreferences("onBoarding",Context.MODE_PRIVATE)
        return sharedPrefs.getBoolean("Finished",false)   //def value is false
    }


}