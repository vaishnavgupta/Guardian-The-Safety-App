package com.example.guardiansafetyapp.onboarding.screens

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.example.guardiansafetyapp.R
import com.example.guardiansafetyapp.RegistrationsActivity


class ThirdScreenFragment : Fragment() {


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view= inflater.inflate(R.layout.fragment_third_screen, container, false)

        val btn=view.findViewById<TextView>(R.id.fnshScr3)
        btn.setOnClickListener {
            onBoardingFinished()
            val i=Intent(activity,RegistrationsActivity::class.java)
            startActivity(i)
            requireActivity().finish()
        }
        return view
    }

    private fun onBoardingFinished(){
        val sharedPref=requireActivity().getSharedPreferences("onBoarding",Context.MODE_PRIVATE)
        val editor=sharedPref.edit()
        editor.putBoolean("Finished",true)
        editor.apply()
    }

}